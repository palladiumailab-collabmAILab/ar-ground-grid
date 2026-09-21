# Requirements specification

## MVP

### Must

- [x] Android ARCore対応端末でカメラ権限を要求し、水平面へ4 m × 4 mのmetric gridを配置できる。
- [x] 10 cmのminor line、1 mのmajor line、reset/repositionを提供する。
- [x] ARCore unavailable / session failureを画面上の状態として観測できる。
- [x] Grid geometryをARCore非依存のpure Kotlinとしてテストできる。
- [x] `./gradlew --no-daemon lintDebug testDebugUnitTest assembleDebug`をCI/Docker/開発環境の共通entrypointにする。

### Should

- [x] tracking stateをユーザーが判別できる。
- [ ] 実機で1 m / 3 mのaccuracy、drift、tracking-loss recoveryをtrial-level dataとして記録する。

### Won't (for now)

- [x] custom SLAM / VIO / SfM / dense depth
- [x] acoustic ranging、cloud/backend、accounts、analytics
- [x] 実測前のscale calibrationや任意2点計測

## Functional requirements

| ID | Requirement | Acceptance criteria |
|---|---|---|
| FR-001 | 水平面へmetric gridを配置する | ARCoreの水平面をタップするとgridが配置され、配置済み状態を表示する |
| FR-002 | 配置をリセットする | reset後に再度水平面をタップして配置できる |
| FR-003 | 失敗状態を表示する | カメラ権限拒否・ARCore開始失敗・tracking待機を画面で判別できる |
| FR-004 | geometryを再現可能にする | 4 m / 10 cm / 1 mの生成結果をJVM unit testで検証できる |

## Non-functional requirements

| ID | Requirement | Target |
|---|---|---|
| NFR-001 | 再現性 | repository-owned Gradle Wrapper 9.5.0と固定checksumを使う |
| NFR-002 | 品質ゲート | Lint、JVM unit test、debug buildをCIとDockerで実行する |
| NFR-003 | 安全な主線 | 実機accuracyが出るまで精度保証や補正方式を仕様化しない |

## Data / external dependencies

- 入力データ: カメラ映像とARCoreのplane/tracking状態（MVPでは永続保存しない）
- 外部API: Google ARCore、SceneView 4.35.0
- 保存対象: MVPではなし。実機評価は`docs/evaluation/`のtrial-level CSVへ記録する。
- 個人情報・機密情報: MVPでは収集しない。

## Definition of Done for MVP

- [x] 主要ユースケースがCI/Dockerでbuild・unit test・lintされる
- [ ] 実Android端末で起動し、1 m / 3 mの成功条件を測定する
- [x] camera permission / ARCore unavailableの失敗条件が整理されている
