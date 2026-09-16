# AR metric measurement reference

調査時点: 2026-09-16

## 結論

初期MVPでは独自のSLAM / VIO / SfM / dense depthを実装せず、AndroidのARCoreが提供するmetric world coordinates、plane detection、hit test、anchorを利用する。

主要な未知量は「3D復元方式を新規実装できるか」ではなく、**ARCoreのworld scaleとtracking stabilityが対象用途で十分か**である。

したがって検証順序は次とする。

`ARCore baseline → real-device measurement → root-cause classification → evidence-backed correction only`

## 代表的な先行技術

### MonoSLAM (2007)
単眼カメラからカメラ軌跡と3Dランドマークをリアルタイム推定する代表的初期研究。

- https://pubmed.ncbi.nlm.nih.gov/17431302/

### PTAM (2007)
trackingとmappingを分離し、keyframe + bundle adjustmentを用いる。

- https://www.robots.ox.ac.uk/~lav/Publications/klein_murray_ismar2007/klein_murray_ismar2007.html

### MSCKF (2007)
camera + IMUを融合するVisual-Inertial navigationの代表的基礎技術。

- https://cir.nii.ac.jp/crid/1362262945674186752

### 携帯電話写真測量 (2009)
携帯電話カメラを用いた3D写真測量の精度評価例。

- https://www.jstage.jst.go.jp/article/jsprs/48/5/48_5_299/_article/-char/ja

### Moving single-camera stereo (2012)
スマホを移動させ、単一カメラの撮影位置差とIMU情報から距離推定する方式。

- https://www.researchgate.net/publication/261247338_Measuring_Distance_with_Mobile_Phones_Using_Single-Camera_Stereo_Vision

### ORB-SLAM (2015)
特徴点、loop closure、relocalization、bundle adjustmentを統合した代表的SLAM。

- https://doi.org/10.1109/TRO.2015.2463671

### VINS-Mono (2017–2018)
単眼カメラと低価格IMUを用いたmetric 6DoF推定。

- https://arxiv.org/abs/1708.03852

### Depth from Motion for Smartphone AR (Google, 2018)
handheld AR向けのmotion + visual informationによるdense depth。

- https://research.google/pubs/depth-from-motion-for-smartphone-ar/

## AR Ground Gridへの含意

### アプリ側で責任を持つ範囲

- metric grid geometry
- horizontal-plane placement condition
- anchor lifecycle
- tracking-state UX
- reset / reposition
- real-device validation
- 必要性が実証された場合だけquality gate / calibration

### ARCore側に委ねる範囲

- VIO
- camera / IMU sensor fusion
- world tracking
- plane estimation
- anchor tracking

### 初期MVPで実装しないもの

- custom SLAM / VIO / SfM
- custom dense depth
- arbitrary two-point measurement
- automatic dimension extraction
- manual calibration
- external metric anchor

## 実測で分離すべき誤差

### Geometry correctness
pure Kotlinの座標生成が0.1 m / 1.0 m / 4.0 mという仕様通りか。JVM testで検証する。

### Placement error
hit test、plane pose、anchor placementによって床から浮く・傾く・ずれる問題。

### World-scale error
AR座標上では正しい長さでも、実世界の既知長と系統的にずれる問題。

### Stability / drift
時間経過、端末移動、tracking loss、relocalization後に位置が変わる問題。

この4種類を混ぜずに記録する。

## Decision rules

1. 既存プラットフォームAPIで成立性を測る前に独自アルゴリズムを追加しない。
2. 計測不能・tracking failureを誤った数値へ変換しない。
3. 平均値だけでなく試行別分布とfailure rateを保存する。
4. 系統誤差とランダム誤差、placement errorとworld-scale errorを分離する。
5. 補正は複数距離・複数条件で改善が再現した場合のみ採用する。

## Related

- #2 Android MVP implementation
- #3 prior-art research
- #4 real-device validation plan
- #7 ARCore accuracy / drift evaluation
