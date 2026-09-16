# Research

構想に関係する外部情報を、事実と仮説を分けて記録する。

## Existing solutions / building blocks

| 対象 | 何を解決するか | 強み | 弱み | 根拠 |
|---|---|---|---|---|
| Google ARCore | Androidで平面検出、hit test、Anchor、端末姿勢追跡を提供 | Android標準的なAR基盤 | 端末認証・追跡品質に依存 | https://developers.google.com/ar/develop/java/quickstart |
| SceneView | Compose上でARCore + Filament描画を扱う | AR描画のボイラープレートを減らせる。Android/iOSで類似概念を持つ | Google公式SDKではないため依存リスクがある | https://github.com/SceneView/sceneview |

## User / market evidence

- 事実: 本リポジトリでは現時点で市場需要を示す一次データは収集していない。
- 解釈: まず技術MVPで実寸グリッドの視認性と精度特性を検証し、その後用途別の需要検証を行う。

## Technical feasibility

- ARCoreはAndroid上で水平面検出、hit test、Anchorを提供する。
- ARCoreのワールド座標はメートル単位で扱えるため、0.1 m間隔の幾何を生成してAnchor配下に描画できる。
- SceneView 4.36.0はCompose向けの`ARSceneView`、`AnchorNode`、`LineNode`を提供する。
- Android MVPはKotlin + Jetpack Compose + ARCore + SceneViewで構成する。
- グリッド線座標の生成は純粋Kotlinへ分離し、将来のiOS移植時に同じ仕様を再利用する。

## Constraints

- ARの実世界精度は端末、照明、テクスチャ、移動量、追跡状態に依存する。MVPでは精度保証を行わず、実機評価で特性を測る。
- AR RequiredアプリはAndroid API 24以上を前提とする。
- SceneViewはサードパーティ依存であるため、ARCore直接実装へ置換できる境界を保つ。

## Risks

| Risk | Evidence | Impact | Mitigation / Test |
|---|---|---|---|
| AR追跡ドリフト | VIOベースのARは環境条件に依存 | グリッドが実物からずれる | 1 m基準長で複数環境を実測する |
| 平面誤検出・未検出 | ARCoreは画像特徴と端末運動から平面を推定 | 配置不能・誤配置 | horizontal upward facing + polygon内hitのみ許可 |
| SceneView依存変更 | 外部ライブラリ | ビルド破損 | バージョン固定、AR層を局所化 |
| 線数増加による描画負荷 | 10 cm間隔では4 m四方で82本 | FPS低下の可能性 | MVPでは82本に固定し実機確認 |

## Sources

- ARCore Android quickstart: https://developers.google.com/ar/develop/java/quickstart
- ARCore enable AR: https://developers.google.com/ar/develop/java/enable-arcore
- ARCore supported devices: https://developers.google.com/ar/devices
- SceneView: https://github.com/SceneView/sceneview
- SceneView node reference: https://github.com/sceneview/sceneview/blob/main/docs/docs/nodes.md
- Android Compose BOM: https://developer.android.com/develop/ui/compose/bom
