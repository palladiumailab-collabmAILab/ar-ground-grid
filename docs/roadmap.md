# AR Ground Grid roadmap

Updated: 2026-09-21

## Objective

Androidで実寸スケールの地面グリッドMVPを成立させ、実機データを基に「そのまま使う / quality gate / calibration / depth / 外部測距」のどこへ進むか判断する。

## Current work map

| Work | Role | Status / gate |
|---|---|---|
| #2 Android MVP | 実装本線 | main向けMVP PRのCI success、merge後に実機起動が次のgate |
| #3 prior art / Japan FTO | 技術調査 | completed |
| #4 measurement plan | 検証計画 | docs PR |
| #5 alternative ranging | 研究バックログ | 本線から分離 |
| #6 acoustic ranging | 条件付きPoC | Draft, blocked by #7 |
| #7 real-device evaluation | 実測 | blocked until #2 is merged into main |

## Phase A — Documentation baseline

目的: Issue本文・コメントに埋もれた調査結果を永続リファレンスへ移す。

- [x] `docs/reference/ar-metric-measurement.md`
- [x] `docs/reference/japan-fto-guardrails.md`
- [x] `docs/reference/alternative-ranging.md`
- [x] #4のvalidation planからreferenceを参照
- [x] #1のwork mapとdependencyをこのroadmapへ同期

Exit:

- 技術背景を理解するためにIssue履歴を全読する必要がない
- 実装者がFTO再調査トリガーを確認できる

## Phase B — Android MVP quality gate

対象: #2

### Required

- [x] Android Lint
- [x] JVM unit tests
- [x] debug build
- [x] Gradle Wrapperをrepositoryへ追加
- [x] CI / Docker / READMEの標準コマンドを`./gradlew`へ統一
- [x] Gradle distribution checksumを固定
- [ ] 実Android端末で起動
- [x] camera permission / ARCore unavailable時のfailureが観測可能

### Architecture boundary

維持する分離:

- `grid/`: pure Kotlin metric geometry
- `ui/`: Compose + ARCore / SceneView adapter

追加しない:

- repository/service/DI framework
- custom SLAM / VIO / SfM
- custom depth
- acoustic ranging

実装PRのCIは上記ゲートを通過済み。mainへのmerge後に実機起動を確認してPhase Bを完了する。

Exit:

- 同一commitをローカル、Docker、GitHub Actionsで同じGradle entrypointから検証できる
- 実機でplacement/reset/tracking-state表示が成立する

## Phase C — Baseline real-device evaluation

対象: #7

### Measurements

最低限:

- 1 m
- 3 m

条件:

- indoor / good lighting / textured
- feature-poor floor
- low light
- glossy floor
- outdoor if feasible

記録:

- reference length [m]
- observed AR length [m]
- absolute error [m]
- relative error [%]
- device model
- tracking state
- plane tracking state
- condition
- trial id

### Stability

- stationary jitter
- move-away / return
- tracking-loss recovery
- plane update後の位置

### Evidence retention

実測結果は集計値だけでなくtrial-level dataを残す。最初の形式はCSV等の単純な表形式でよく、DBやanalytics基盤は導入しない。

Exit:

- error distribution
- failure rate
- known failure conditions
- drift / recovery observations
- 次のdecisionが`docs/decisions.md`へ記録される

## Phase D — Evidence-based branch

#7の結果に応じて**1つだけ**選ぶ。

### D1 — Baseline sufficient

ARCoreそのままで用途上十分。

次:

- placement UX
- readability
- grid visibility
- small usability fixes

### D2 — Tracking quality issue

world scaleそのものよりtracking状態が問題。

次:

- quality gate
- placement enable/disable condition
- failure reason UX

### D3 — Systematic scale bias

複数試行で再現性あるscale biasが確認された場合のみcalibration PoC。

採用条件:

- 同一端末で再現
- 複数距離で改善
- 別条件で過補正しない

### D4 — Depth / surface correspondence issue

ARCore公開APIで改善可能かを先に確認する。custom depthへ進む前にFTOを再確認する。

### D5 — Independent metric anchor needed

#6 acoustic rangingをactivateする。

最初のbaseline:

- 48 kHz
- 8–16 kHz chirp
- `AudioTrack` + `AudioRecord`
- matched filter / cross-correlation
- 0.3 / 0.5 / 1 / 2 / 3 m
- P90 absolute error <= 5 cmを暫定Go/No-Go

## Phase E — Productization gate

Phase Dで技術方式が確定した後に初めて検討する。

- supported-device policy
- app lifecycle / error recovery
- accessibility / visual contrast
- release build
- dependency/license inventory
- privacy/data-handling confirmation
- Play distribution requirements
- final FTO recheck for implemented feature set

MVP成立前にanalytics、backend、accounts、cloud sync、general-purpose measurement suiteへ広げない。

## Non-goals until evidence requires them

- iOS implementation
- arbitrary two-point measurement
- furniture 3D placement
- room scanning
- cloud/backend/account
- custom CV/SLAM/VIO
- custom dense depth
- native/C++ DSP
- generalized sensor-fusion framework

## Definition of a good next PR

次のPRは以下を満たす。

1. 1つのdecision / risk / acceptance criterionに対応する。
2. #7の前に不要な補正アルゴリズムを追加しない。
3. CIで検証可能なものと実機でしか検証できないものを分離する。
4. 新規測定UI / depth / 3D fittingの場合はFTO guardrailを確認する。
5. 結果が変わらない抽象化・framework追加をしない。
