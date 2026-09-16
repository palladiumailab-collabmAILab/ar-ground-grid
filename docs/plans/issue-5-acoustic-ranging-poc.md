# Issue #5 implementation plan: Android acoustic ranging PoC

## Status

**Conditional / blocked.**

このPoCはAR Ground Grid MVPの前提条件ではない。#7 のARCore実機評価で、追加のmetric anchorが必要と判断された場合のみ実装を開始する。

Dependencies:

- Parent: #1
- Existing Android MVP: #2
- ARCore validation: #7
- Research: #5

## Goal

一般的なAndroidスマホのspeaker + microphoneだけで、外部タグなしに壁等のpassive surfaceまでの距離を推定できるかを検証する。

成立した場合でも、このPoC単独で任意2点間の長さを測ることは目的としない。ARCoreへのscale correction / sensor fusionは別Issueで扱う。

## Activation condition

以下のいずれかを #7 の実測で確認した場合に限り開始する。

- ARCoreのworld scaleに用途上無視できない系統誤差がある
- tracking quality gateだけでは不足する
- 外部デバイスなしの独立した実距離anchorを比較対象として必要とする

#7でARCore単体が用途上十分なら、このPoCは実装しない。

## Acceptance criteria

実Android端末で以下を満たした場合のみ技術的に有望と判断する。

- range: 0.3–3.0 m
- external responder / tagなし
- P90 absolute error <= 5 cm
- repeated static measurementsから分散を算出できる
- 推定不能を誤った距離へ丸めず、failureとして観測できる

CIやemulatorの結果だけでは精度条件を満たしたことにしない。

## Baseline design

### Signal

最初は1方式に限定する。

- sample rate: 48 kHz
- linear chirp: 8–16 kHz
- PCM 16-bit monoを第一候補
- one-shot transmit / record

15–20 kHz、96 kHz、複雑なFMCW/phase processingはbaseline失敗後の候補とする。

### Algorithm

1. deterministic chirpを生成
2. `AudioTrack`で再生
3. `AudioRecord`で録音
4. matched filter / cross-correlation
5. direct-path/reference peakを特定
6. 0.3–3.0 m相当のlag範囲からreflection peakを探索
7. `distance = delta_t * sound_speed / 2` で距離化
8. distance + diagnostics、または明示的failureを返す

baseline計測前にbeamforming、learned peak classification、native DSP、独自SLAM等を追加しない。

## Integration with existing app

#2 のAndroid project / Gradle / GitHub Actionsを再利用する。新しいAndroid skeleton、別build system、別CIは作らない。

追加責務だけを分離する。

```text
app/
  audio/
    AudioDuplexEngine
    AudioCapabilityProbe
  ranging/
    ChirpGenerator
    CrossCorrelator
    PeakDetector
    RangeEstimator
    RangingResult
  measurement/
    MeasurementRunner
    MeasurementRecord
```

- DSPはAndroid API非依存のpure Kotlinにする
- `AudioTrack` / `AudioRecord`はaudio層に閉じ込める
- DI framework、repository/service層、native/C++は導入しない
- 既存AR画面との融合はこのPoCでは行わない

## Device diagnostics

各測定で最低限記録する。

- requested / actual sample rate
- audio source
- AGC / noise suppression / AECの利用可否・状態（取得可能な範囲）
- device model / Android version
- expected distance
- estimated distance または failure reason
- peak lag / score等の診断値

raw PCM保存はdebug用途の任意機能とし、通常測定の必須条件にしない。

## Evaluation protocol

Baseline distances:

- 0.3 m
- 0.5 m
- 1.0 m
- 2.0 m
- 3.0 m

最初は1つの平坦で反射性の高い壁、固定端末姿勢で測る。

算出:

- MAE
- P50 absolute error
- P90 absolute error
- max absolute error
- failure rate
- static repeated measurementのstandard deviation

材質、角度、騒音、端末姿勢、near-ultrasonic、96 kHzはbaseline成立後の追加実験とする。

## Implementation sequence

### Slice 1 — deterministic DSP

- `ChirpGenerator`
- `CrossCorrelator`
- `PeakDetector`
- `RangeEstimator`
- synthetic delayed-signal unit tests

Exit: known delayをpure Kotlin testで復元でき、既存CIが通る。

### Slice 2 — Android audio I/O

- microphone permission
- `AudioDuplexEngine`
- unsupported audio configurationの明示
- one-shot measurement
- result / diagnostics表示

Exit: 実機で1回の測定cycleを完了できる。

### Slice 3 — measurement log / evaluation

- expected distance入力
- compact record export
- MAE / P50 / P90 / max / failure rate / stddev算出
- baseline procedure文書化

Exit: Go/No-Goを実測値で判断できる。

### Slice 4 — evidence-backed refinement only

P90 <= 5 cmを満たさない場合だけ、実測原因に対応する1実験ずつを追加する。

候補:

- chirp band / duration変更
- audio preprocessing条件変更
- direct-path cancellation / peak selection改善
- 96 kHz（対応端末のみ）
- FMCW dechirp / phase processing

## Non-goals

- arbitrary two-point length measurement
- ARCore fusion / scale correction
- room mapping
- UWB / Bluetooth Channel Sounding / Wi-Fi RTT / BLE / GNSS / barometer実装
- cloud backend / account / database
- production analytics
- custom native/C++ DSP before Kotlin performance is proven insufficient

## Done

- [ ] existing Android app / CIを再利用してbuildできる
- [ ] deterministic DSP testsが通る
- [ ] physical deviceで測定とfailure diagnosticsを記録できる
- [ ] 0.3 / 0.5 / 1 / 2 / 3 mのbaseline dataがある
- [ ] MAE / P50 / P90 / max / failure rate / stddevを報告する
- [ ] P90 <= 5 cmを明示的に判定する
- [ ] 結果と次の判断を`docs/decisions.md`へ記録する
