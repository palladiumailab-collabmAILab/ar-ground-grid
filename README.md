# AR Ground Grid

Androidカメラ越しの床・地面に、実世界スケールのグリッドを重畳するARアプリ。

## MVP

- Kotlin + Jetpack Compose
- Google ARCore + SceneView 4.35.0
- 水平面をタップして4 m × 4 mグリッドを配置
- 小グリッド10 cm、主線1 m、リセットして再配置
- 初期MVPでは計測精度を保証せず、実機評価で検証する

## Architecture

- `app/src/main/.../grid/` — Android/ARCore非依存のメートル単位グリッド幾何
- `app/src/main/.../ui/` — ARCore/SceneView接続とCompose UI
- `docs/specs/` — 現行仕様の正本
- `docs/reference/` — 技術・FTOリファレンス
- `docs/roadmap.md` — 実装順序とdecision gate
- `docs/evaluation/` — 実機評価手順とtrial template

## Build

repository-owned Gradle Wrapper 9.5.0を標準entrypointとする。JDK 17+とAndroid SDK 37が必要。

```bash
./gradlew --no-daemon lintDebug testDebugUnitTest assembleDebug
```

Gradle distributionとWrapper JARのSHA-256を固定し、GitHub ActionsとDockerも同じWrapper経路を使う。

```bash
docker build -t ar-ground-grid .
docker run --rm -v "$PWD:/workspace" -w /workspace ar-ground-grid
```

## Validation boundaries

- BlueStacks smoke testは起動、権限処理、AR初期化失敗の観測用であり、実寸精度の根拠にしない。
- Official Android EmulatorのVirtualSceneはARCoreのtracking/plane/placement診断用であり、実寸精度の根拠にしない。
- 1 m / 3 mのaccuracy、drift、tracking-loss recoveryは実Android端末で #7 として評価する。
- 実機評価が終わるまで「計測精度確認済み」と主張しない。

## Documents

- [Specifications](docs/specs/README.md)
- [Product](docs/specs/product.md)
- [Requirements](docs/specs/requirements.md)
- [Roadmap](docs/roadmap.md)
- [Evaluation](docs/evaluation/README.md)
- [Research](docs/research.md)
- [Decisions](docs/decisions.md)
- [Agent rules](AGENTS.md)

## Current phase

**Phase: Android MVP implementation**

Android MVPの実装・CI・Docker経路はこのリポジトリで管理し、merge後に #7 の実機評価へ進む。MVP成立前にcustom SLAM、custom depth、音響測距、cloud/backendを追加しない。
