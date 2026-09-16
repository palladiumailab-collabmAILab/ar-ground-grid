# Decisions

重要な設計・製品判断を時系列で残す。

## 2026-09-16 — Android native first, iOS later

- Status: Accepted
- Context: AR地面グリッドをまずAndroidで成立させ、将来iPhoneにも展開したい。
- Options: Android native / Unity AR Foundation / WebXR
- Decision: 初期MVPはKotlin + Jetpack ComposeのAndroidネイティブとする。iOSは後続フェーズで実装する。
- Evidence: ARCoreはAndroid向けに水平面検出、hit test、Anchorを提供する。
- Consequences: Android固有UI/AR層と、プラットフォーム非依存のグリッド仕様・幾何ロジックを分離する。
- Revisit when: iOS実装開始時、またはAndroid/iOS間の重複実装コストが大きくなった時。

## 2026-09-16 — Use SceneView as the Android rendering adapter

- Status: Accepted
- Context: ARCore自体は追跡を提供するが、カメラ背景と3D描画の実装が別途必要。
- Options: ARCore + OpenGL直接実装 / SceneView / Unity
- Decision: MVPではMaven Centralで公開済みのSceneView 4.35.0をARCore + Filament描画アダプタとして使用する。
- Evidence: SceneViewはCompose向け`ARSceneView`、`AnchorNode`、基本3Dプリミティブを提供し、ARCore/Filamentの定型実装を削減できる。2026-09-16時点では公式mainドキュメントの4.36.0表記に対し、Maven Central公開版は4.35.0。
- Consequences: サードパーティ依存となるため、SceneView型をUI/AR層から外へ漏らさず、グリッド幾何ロジックは純粋Kotlinに保つ。
- Revisit when: SceneViewの更新停止、重大な互換性問題、または精度・性能上の制約が確認された時。

## 2026-09-16 — MVP grid definition

- Status: Accepted
- Context: 最初に検証可能な実寸グリッド仕様が必要。
- Options: 可変無限グリッド / 4 m固定 / 10 m固定
- Decision: 4 m × 4 m、10 cm小線、1 m主線とする。ユーザーが水平面をタップしてAnchorを作成し、そこを原点として描画する。
- Evidence: 4 m幅では10 cm間隔でも線数はX/Z合計82本に収まり、MVPとして単純で検証しやすい。
- Consequences: 広範囲用途は初期MVP対象外。必要性と描画負荷を実機評価後に再検討する。
- Revisit when: 4 mで不足する利用例が確認された時、または実機で描画負荷が問題になった時。

---

## Template

### YYYY-MM-DD — Decision title

- Status: Proposed / Accepted / Rejected / Superseded
- Context:
- Options:
- Decision:
- Evidence:
- Consequences:
- Revisit when:
