# AR ground grid measurement plan

Related: #3, PR #2

## Goal

Android AR地面グリッドMVPについて、スマホのカメラ＋IMUを用いた3次元計測の先行技術を踏まえ、独自3D復元を増やさずに実寸表示の成立性を検証する。

初期MVPの目的は「測量器を作ること」ではなく、ARCoreのメートル座標系に置いたグリッドが、家具配置・DIY・撮影・現場確認で使える程度に安定して見えるかを実測で判断することとする。

## Prior-art implications

Issue #3で確認した以下は既知技術として扱う。

- 単眼SLAM / SfMによる3D復元と自己位置推定
- カメラ＋IMUのVisual-Inertial推定
- moving monocular stereo
- 画面上の点指定による長さ計測
- 距離・高さ・面積・体積の算出
- camera calibration / undistortion / stereo matching / triangulation / error estimation

そのため、MVPでは以下を方針とする。

1. 独自SLAM、独自VIO、独自SfMは実装しない。
2. AndroidではARCoreのpose / plane / hit test / anchorを計測基盤として利用する。
3. ARCoreのworld scaleが用途上十分かを先に実測する。
4. 精度不足が確認された場合のみ、原因を分解して補正層を追加する。
5. custom depth-from-motionや測定点のdepth補正へ進む場合は、実装前に現役特許のclaim-level FTO確認を別タスクとする。

## Architecture boundary

```text
Camera + IMU
    |
    v
ARCore tracking / plane / anchor
    |
    v
Metric world coordinates [m]
    |
    +--> Grid geometry (platform-independent specification)
    |
    +--> Rendering adapter (SceneView / Filament)
    |
    v
Displayed AR ground grid
```

ARCore内部のVIO、depth-from-motion、sensor fusionをアプリ側で再実装しない。

アプリ側で責任を持つ範囲は以下とする。

- グリッド幾何
- anchor配置条件
- tracking状態の表示
- reset / reposition
- 実測評価
- 必要になった場合の補正係数・品質判定

## Phase 1 — Existing MVP validation

PR #2のAndroid MVPを対象に、まず「幾何が正しい」と「実世界で正しく見える」を分離して検証する。

### 1. Geometry validation

自動テスト対象:

- 10 cm間隔
- 1 m主線
- 4 m × 4 m範囲
- X/Z座標
- 原点対称性
- line count

この層ではARCore精度を評価しない。

### 2. Device validation

実機で以下を確認する。

- 水平面を検出できる
- plane polygon内だけで配置される
- anchor位置にグリッドが固定される
- tracking loss時に状態が表示される
- reset後に再配置できる

## Phase 2 — Metric accuracy experiment

### Reference

既知長の物理基準を床上に置き、ARグリッドとの対応を記録する。

最低限:

- 1 m
- 3 m

追加評価:

- 5 m相当のworld-scale確認が可能な環境では5 mも記録する

4 m固定グリッドを越える評価は、グリッド表示範囲ではなくARCore world-scale / anchor間距離の検証として扱う。

### Metrics

各試行で以下を保存する。

- reference length [m]
- observed AR length [m]
- absolute error [m]
- relative error [%]
- tracking state
- plane tracking state
- device model
- test condition

計算:

`absolute_error = observed - reference`

`relative_error[%] = 100 * absolute_error / reference`

平均値だけでなく、試行ごとの分布を残す。

### Conditions

最低限、以下を分けて測る。

| Condition | Purpose |
|---|---|
| 室内・十分な照明・テクスチャあり | baseline |
| feature-poorな床 | plane / VIO弱条件 |
| 暗所 | camera tracking弱条件 |
| 光沢床 | reflection影響 |
| 屋外 | illumination / scale安定性 |

同一条件で複数回配置し、再配置によるばらつきを確認する。

## Phase 3 — Stability / drift experiment

同じanchorを一定時間観察し、実物基準からのずれを記録する。

確認項目:

- 静止時の見かけの揺れ
- 端末を移動して元位置へ戻した後のずれ
- 一時的なtracking loss後の復帰
- plane更新後のグリッド位置

ここでは「瞬間的な長さ誤差」と「時間経過・再局在化による位置ずれ」を分離する。

## Phase 4 — Root-cause decision tree

実測で問題が出た場合は、先に原因を分類する。

### A. Grid geometry bug

症状:
- AR座標上でも0.1 m / 1 mになっていない

対応:
- 純粋Kotlin幾何を修正

### B. Anchor / plane placement error

症状:
- scaleは正しいが床面から浮く・傾く・位置が不安定

対応候補:
- hit test条件
- plane orientation
- placement UX
- tracking quality gate

### C. ARCore world-scale error / drift

症状:
- 幾何は正しいが実世界基準と系統的にずれる

対応順序:
1. 端末・環境依存性を確認
2. ARCore tracking qualityによる除外条件を検討
3. 必要なら既知長によるscale calibrationをPoC
4. 補正が再現性を持つ場合のみ製品機能化

### D. Depth-related placement error

症状:
- 2Dタップと実表面の対応が不安定で、plane hitだけでは不足

対応:
- まずARCore提供APIの範囲で改善可能か確認
- custom dense depth / depth-from-motionを独自実装する前にFTO確認

## Calibration policy

初期MVPでは手動キャリブレーションを実装しない。

実測結果で端末ごとに再現性のあるscale biasが確認された場合のみ、既知長を使うscale calibrationを実験する。

採用判断に必要な条件:

- 補正前後の誤差分布が記録されている
- 同一端末で再現する
- 複数距離で改善する
- 別環境で過補正を起こさない

単一条件だけ改善する補正は採用しない。

## Patent / FTO boundary

Issue #3で確認した失効・放棄特許は技術資料として参照する。ただし、`Expired - Fee Related`等の表示だけを商用FTOの最終判断には使わない。

特に以下の独自実装を追加する場合は、別Issueでclaim-level確認を行う。

- monocular depth-from-motion
- VIO poseとkeyframe stereoを組み合わせたdense depth
- depthによる測定点自動補正
- 独自の3D測長UIと補正ロジック

MVPでARCore公開APIを通常利用する範囲では、アプリ側で同等アルゴリズムを再実装しない。

## Decision gates

### Gate 1 — MVP works

- Android実機で配置・再配置できる
- geometry unit testsが通る
- tracking状態をユーザーが判別できる

### Gate 2 — Metric characteristics known

- 1 m / 3 mで複数試行の誤差が記録されている
- baselineと弱条件を分けて結果が残っている
- drift / tracking loss後の挙動が記録されている

### Gate 3 — Decide next step

結果に応じて次のいずれかを選ぶ。

- A: ARCoreそのままで用途上十分 → UI/UXへ進む
- B: tracking quality gateで十分改善 → quality判定を実装
- C: 系統的scale biasあり → calibration PoC
- D: depth不足が主因 → ARCore Depth等の利用可否を検証
- E: 精度が用途を満たさない → 計測用途を縮小し、可視化用途に限定

精度閾値は、実測データを得る前に恣意的に固定しない。まず誤差分布を取得し、対象用途ごとの許容値を別途定義する。

## Deliverables

- [ ] PR #2のAndroid MVPを実機起動
- [ ] geometry unit test結果
- [ ] 1 m / 3 mのaccuracy table
- [ ] 条件別accuracy table
- [ ] drift / tracking-loss観察結果
- [ ] 既知のfailure conditions
- [ ] calibration要否の判断
- [ ] custom depth実装要否の判断
- [ ] 必要ならFTO追加Issue

## Non-goals

この計画では以下を実装しない。

- 独自SLAM
- 独自VIO
- 独自SfM
- custom dense depth
- iOS対応
- 測量器としての精度保証
- 家具3Dモデル配置
