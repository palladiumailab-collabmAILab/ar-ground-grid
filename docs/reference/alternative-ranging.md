# Alternative ranging reference

調査時点: 2026-09-16

カメラ / IMU以外で実距離・長さ推定を補助する方式の整理。現時点ではMVPの依存条件ではなく研究バックログ。

## 結論

外部tagなしで一般的なpassive wallまで数mのrangeを直接取得する条件では、スマホspeaker + microphoneを使うactive acoustic ranging / acoustic echolocationが最も直接的な候補。

ただしAR Ground Gridでは平面姿勢・grid orientationは別途必要であり、音響range単独でAR配置は決まらない。

## 比較

| 方式 | 外部機器 | passive wallを直接測れる | 主用途 | 現時点の扱い |
|---|---:|---:|---|---|
| Acoustic echo / FMCW | 不要 | Yes | 数mの反射面range | 条件付きPoC #6 |
| UWB | responder/tag必要 | No | device/tag間ranging | backlog |
| Bluetooth Channel Sounding | peer必要 | No | peer間fine ranging | backlog |
| Wi-Fi RTT | AP/peer必要 | No | indoor positioning | backlog |
| BLE RSSI | peer/tag必要 | No | proximity | 優先度低 |
| Raw GNSS / ADR | 衛星・場合により補正系 | No | 屋外長距離 | scope外 |
| Barometer | 不要 | No | 鉛直相対変化 | scope外 |
| Proximity sensor | 不要 | 数cmのみ | 近接判定 | backlog |
| Magnetometer + magnet | 磁石必要 | No | 短距離marker | backlog |
| Ambient light + known LED | 既知光源必要 | No | infrastructure positioning | backlog |

## Acoustic ranging

### 原理

speakerからchirp等を送信し、microphoneでdirect pathとreflectionを取得する。

`distance = sound_speed × round_trip_time / 2`

matched filter / cross-correlation、またはFMCW処理で遅延を推定する。

### 既存研究

#### BatMapper / BatMapper-Plus
市販スマホのspeaker + microphonesを用いたwall ranging / mapping。

- https://www.ece.stonybrook.edu/~fanye/papers/mobisys17-batmapper.pdf
- https://www.mdpi.com/2075-1702/11/2/205

#### SAMS
smartphone acoustic indoor-space mapping。

- https://doi.org/10.1145/3214278

#### PD-FMCW
phase differenceを用いた微小相対変位推定。絶対wall-range精度とは区別する。

- https://doi.org/10.1109/TMC.2022.3162631

#### BeepBeep
2端末間acoustic ranging。passive wall echoとは異なる。

- https://www.microsoft.com/en-us/research/publication/beepbeep-a-high-accuracy-acoustic-ranging-system-using-cots-mobile-devices/

### 制約

- multipath
- direct speaker-to-mic coupling
- surface material / angle
- device-specific frequency response
- AGC / noise suppression / AEC
- audible-band UX
- sound speedの温度依存
- reflection peak selection

### Activation rule

#6は #7 のARCore実機評価で以下のいずれかが確認された場合だけ開始する。

- world scaleに用途上無視できない系統誤差がある
- tracking quality gateだけでは不足する
- 外部deviceなしの独立したmetric anchorを比較対象として必要とする

ARCore単体が用途上十分なら実装しない。

## UWB

Androidにranging APIがあるが、対応peer / tagが必要で、任意の壁・家具そのものを直接測れない。

- https://developer.android.com/develop/connectivity/uwb
- https://developer.android.com/develop/connectivity/ranging

## Bluetooth Channel Sounding

Bluetooth Core 6.0系のfine-ranging方式。対応peer / hardwareが必要。

- https://www.bluetooth.com/learn-about-bluetooth/feature-enhancements/channel-sounding/
- https://developer.android.com/develop/connectivity/ranging

## Wi-Fi RTT

対応APまたはpeerまでの距離取得。家具・DIYのcm級scale anchor用途には直接適合しない。

- https://developer.android.com/develop/connectivity/wifi/wifi-rtt

## BLE RSSI

環境、遮蔽、姿勢等の影響が大きく、実寸scale基準には使わない。

## Raw GNSS / ADR

屋外・長距離用途の候補。室内家具配置等のMVP用途からは外す。

- https://developer.android.com/develop/sensors-and-location/sensors/gnss

## Barometer

鉛直方向の相対変化検出には使えるが、家具等のcm級寸法基準には使わない。

## Proximity / magnetometer / ambient light

Issue #5に追加調査候補として保持する。AR Ground Grid本線で先回り実装しない。

## Related

- #5 alternative ranging research backlog
- #6 conditional acoustic ranging PoC
- #7 ARCore baseline evaluation
