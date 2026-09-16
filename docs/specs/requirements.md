# Requirements specification

## MVP

### Must

- [ ] Androidネイティブアプリとして起動できる。
- [ ] カメラ権限とARCore利用可否を扱える。
- [ ] 水平面を検出できる。
- [ ] 検出した水平面をタップしてグリッドを固定できる。
- [ ] 10 cm間隔の小グリッドと1 m間隔の主線を4 m × 4 mの範囲に表示できる。
- [ ] 配置済みグリッドをリセットして再配置できる。
- [ ] AR追跡状態を画面上で確認できる。

### Should

- [ ] グリッド原点を視認できる。
- [ ] 1 mスケールを画面上で説明できる。
- [ ] 実機精度評価手順を文書化する。

### Won't (for now)

- [ ] iOS版
- [ ] 家具などの3Dオブジェクト配置
- [ ] 手動キャリブレーション
- [ ] 永続化・クラウド同期
- [ ] 測量器としての精度保証

## Functional requirements

| ID | Requirement | Acceptance criteria |
|---|---|---|
| FR-001 | ARセッションを開始する | カメラ権限付与後、ARカメラ映像が表示される |
| FR-002 | 水平面を検出する | ARCoreの水平面検出が有効で、追跡可能な水平面を利用できる |
| FR-003 | タップ位置へグリッドを配置する | 水平面ポリゴン内のhit test成功時のみAnchorを作成する |
| FR-004 | 実寸グリッドを生成する | 小線間隔0.1 m、主線間隔1.0 m、全幅4.0 mを純粋ロジックで生成する |
| FR-005 | グリッドを描画する | Anchor座標系上にX/Z方向のグリッド線が表示される |
| FR-006 | 再配置する | リセット操作で既存Anchorを破棄し、再度タップ配置できる |
| FR-007 | 状態を表示する | 未配置、追跡中、追跡不十分、配置済みを識別できる |

## Non-functional requirements

| ID | Requirement | Target |
|---|---|---|
| NFR-001 | Android要件 | minSdk 24、compileSdk 37、targetSdk 36 |
| NFR-002 | アーキテクチャ | グリッド幾何計算はAndroid/ARCore非依存のKotlinコードに分離する |
| NFR-003 | テスト | グリッド本数、座標、主線判定をJVM単体テストで検証する |
| NFR-004 | CI | pull request / main pushでunit testとAndroid debug buildを実行する |
| NFR-005 | 再現性 | Gradle 9.5.0をCIとDockerで固定し、同じbuild/testコマンドを使う |
| NFR-006 | 実測検証 | 1 m基準長との誤差を複数条件で記録できる評価手順を持つ |

## Data / external dependencies

- 入力データ: 端末カメラ映像、ARCoreの端末姿勢・平面推定
- 外部API: なし
- ライブラリ: Google ARCore、SceneView (Android: ARCore + Filament wrapper)
- 保存対象: MVPではなし
- 個人情報・機密情報: 保存・送信しない

## Definition of Done for MVP

- [ ] Android実機で起動する。
- [ ] 水平面へのタップで4 m × 4 mグリッドを配置できる。
- [ ] リセット後に再配置できる。
- [ ] 純粋Kotlinのグリッド生成テストが通る。
- [ ] GitHub Actionsでunit testとdebug buildが通る。
- [ ] 1 m基準長を用いた実機精度評価結果を記録する。
- [ ] 既知の重大な失敗条件が整理されている。
