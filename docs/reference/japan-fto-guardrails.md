# Japan FTO guardrails

調査時点: 2026-09-16

Issue #3の日本FTO一次調査を、実装時に参照できるガードレールへ整理したもの。

> これは技術設計上の一次スクリーニングであり、法的な無侵害鑑定ではない。商用公開前はJ-PlatPatの登録・経過情報、対象実装、均等論等を含め再確認する。

## 当面避けるUI / 処理

- anchor候補へ近づくと外観が変わる測定点作成reticle + tap確定
- カメラ距離に応じて測定scale markerを追加・削除するLOD
- annotation / 測定結果を距離閾値で自動削除する処理
- 実在対象にAR markを付け、写真・メモ・属性をmarkへ紐付けて保存する構成
- 可変3D直方体を対象物へ自動fitし、その形状から寸法を出す方式
- 直方体の面・辺操作を寸法測定そのものに使うUI
- camera optical axisを使った対象面→box面の自動fit
- 人体・足等の特徴点から床面を推定するfallback
- 外部照合画像DBのscale metadataを実寸基準に使う方式

## 再FTOトリガー

以下を実装する前にclaim-levelで再確認する。

- 2点 / 多点寸法計測
- snapping / reticle / anchor候補表示
- camera-distance-dependent scale marker / LOD
- depthによる測定点自動補正
- 3D box fitting、幅・高さ・奥行き自動推定
- AR markへの属性・写真・メモ保存
- 人体を使う床推定
- image databaseを使うobject recognition + scale estimation
- custom depth-from-motion / dense depth

## 一次調査で参照した代表例

### JP7708916B2 — Apple
測定点作成indicator、3D anchor、tapによる測定点追加、状態に応じたindicator外観変更等。

MVPでは固定world-space grid全体を水平面へ1回配置し、測定点作成reticleを設けない。

### JP7467534B2 — Apple
端末と対象との距離変化に応じ、以前は表示されていなかったscale markerを含む測定表現を表示する構成。

MVPでは10 cm / 1 mのworld-space gridをカメラ距離に関係なく固定する。

### JP7097991B2 — Apple
annotation間の距離条件に応じた既存annotationの維持・削除。

MVPでは削除はユーザーの明示的resetのみとする。

### JP7391317B2 — M Soft系
実在対象の3D位置、ライブビュー上のmark画像、対象関連情報の対応付け保存。

MVPでは対象物ID・写真・メモ・属性等をAR markへ紐付けて保存しない。

### JP6821222B1 / JP6867070B1 / JP6867071B1 — アイタックソリューションズ
可変直方体の測定用仮想オブジェクト、特徴点やcamera optical axisを用いた面選択・fit、面/辺操作等。

MVPでは可変3D box fittingを寸法測定に利用しない。

### JP7107166B2 — Fujitsu
身体部位の特徴点群と身体・床位置関係モデルから床面を推定する構成。

MVPでは人体特徴点を床検出に利用しない。

### JP5170223B2 — Casio
外部照合画像DB、被写体scale情報、重畳情報等を用いる構成。

MVPでは画像DB由来のscale metadataを実寸基準に使わない。

## 現行MVPとの整合

現行方針は以下のため、上記ガードレールと整合する。

- ARCoreのhorizontal plane detectionを利用
- 1つのtapでgrid全体をanchor化
- 10 cm / 1 mのworld-space scaleを固定
- resetはユーザー操作
- object annotation / persistenceなし
- box fittingなし
- body-based floor detectionなし
- image-database scale estimationなし

## Source

詳細なclaim要素と迂回案は Issue #3 のFTO一次調査コメントを履歴として参照する。
