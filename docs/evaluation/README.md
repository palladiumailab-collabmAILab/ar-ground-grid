# ARCore real-device evaluation

Related: #7

## Purpose

AR Ground Gridのgeometry correctnessと、実世界でのmetric accuracy / stabilityを分離して評価する。

## Preconditions

- Android Lint / unit test / debug buildが通る
- 実機でAR画面が起動する
- 水平面へgridを配置・reset・再配置できる
- tracking状態を画面で識別できる

## Baseline setup

既知長の物理基準を床上に置き、同一条件で複数回gridを配置する。

最低限:

- 1 m
- 3 m

最初は「室内・十分な照明・テクスチャあり」をbaselineとする。baseline成立後にfeature-poor、暗所、光沢床、屋外を追加する。

## One trial

1. `trial_id`を採番する。
2. gridをresetし、新しく水平面へ配置する。
3. reference lengthとAR gridの対応を観察する。
4. `observed_ar_length_m`を記録する。推定不能なら数値を捏造せず空欄とし、`failure_reason`を記録する。
5. tracking / plane tracking / condition / notesを記録する。
6. 次のtrialでは再配置して独立試行とする。

## Metrics

trialごと:

- `absolute_error_m = observed_ar_length_m - reference_length_m`
- `relative_error_pct = 100 * absolute_error_m / reference_length_m`

集計:

- MAE
- median absolute error
- P90 absolute error
- max absolute error
- failure rate
- 条件別分布

平均値だけで判断しない。

## Stability observations

accuracy trialとは分けて以下を記録する。

- stationary jitter
- 端末を移動して元位置へ戻した後のずれ
- tracking loss後の復帰
- plane update後のgrid位置

## Data file

`arcore-baseline-template.csv`をコピーして試行を記録する。Gitで追跡する場合は個人情報や不要な位置情報を入れない。

## Decision gate

結果に基づき、以下から次の1つを選ぶ。

- ARCore baseline sufficient
- tracking quality gate
- calibration PoC
- ARCore Depth evaluation
- acoustic ranging #6
- measurement scope reduction

実測前に補正方式を先回り実装しない。
