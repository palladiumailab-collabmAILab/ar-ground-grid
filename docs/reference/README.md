# Technical reference

AR Ground Grid の技術調査結果を、Issue/PRの一時的な議論から切り離して参照可能な形で保持する。

## Documents

- `ar-metric-measurement.md` — カメラ/IMU/ARによる実寸スケール推定の基礎・先行技術・設計上の含意
- `japan-fto-guardrails.md` — 日本FTO一次調査から導いた実装ガードレールと再調査トリガー
- `alternative-ranging.md` — 音響、UWB、Bluetooth、Wi-Fi、GNSS等の代替測距方式

## Usage

- 現行仕様の正本は `docs/specs/`。referenceは仕様そのものではない。
- 実装判断は、reference → `docs/decisions.md` → `docs/specs/` の順で反映する。
- 調査時点の技術・法的状態を固定記録する。外部仕様、SDK、特許状態は製品化前に再確認する。
- 特許資料は技術設計上の一次スクリーニングであり、法的な無侵害鑑定ではない。

## Provenance

主な整理元:

- Issue #3: スマホのカメラ/IMU等による3D実寸計測、先行技術、日本FTO一次調査
- Issue #5: カメラ/IMU以外の実寸距離・長さ推定技術
- PR #4: AR実機検証計画

Referenceに移した後も、原Issueは調査履歴として保持する。