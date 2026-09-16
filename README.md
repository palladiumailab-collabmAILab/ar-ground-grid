# AR Ground Grid

Androidカメラ越しの床・地面に、実世界スケールのグリッドを重畳するARアプリ。

## MVP

- Android native: Kotlin + Jetpack Compose
- AR: Google ARCore
- Rendering adapter: SceneView 4.35.0 (ARCore + Filament)
- 水平面をタップして4 m × 4 mグリッドを配置
- 小グリッド: 10 cm
- 主線: 1 m
- リセットして再配置
- 初期MVPでは計測精度を保証せず、1 m基準長で実機評価する

## Architecture

- `app/src/main/java/.../grid/` — Android/ARCore非依存のメートル単位グリッド幾何
- `app/src/main/java/.../ui/` — ARCore/SceneViewとの接続とCompose UI
- `docs/specs/` — 現行仕様の正本
- `docs/reference/` — 技術リファレンス
- `docs/roadmap.md` — 実装順序・decision gate
- `docs/decisions.md` — 設計判断

将来のiPhone対応では、グリッド仕様・幾何を維持し、AR層をARKit/RealityKit側へ置き換える。

## Build

repository-owned Gradle Wrapper 9.5.0を標準entrypointとする。JDK 17+とAndroid SDK 37が必要。

```bash
./gradlew --no-daemon lintDebug testDebugUnitTest assembleDebug
```

WrapperはGradle 9.5.0のbinary distributionをSHA-256で検証する。GitHub Actionsでは`gradle/actions/setup-gradle@v4`によるWrapper JAR検証も実行する。

Dockerでも同じ品質ゲートを使う。

```bash
docker build -t ar-ground-grid .
docker run --rm -v "$PWD:/workspace" -w /workspace ar-ground-grid
```

## Device requirements

AR Requiredアプリのため、ARCore対応Android端末とGoogle Play Services for ARが必要。`minSdk = 24`。`compileSdk = 37`、`targetSdk = 36`。

## Current phase

**Phase: Android MVP implementation**

実装後の完了条件は `docs/specs/requirements.md` を参照する。特に、実機で既知長との誤差を測定するまでは「計測精度確認済み」としない。

## Harness

`codex-dev-harness` の共通開発原則を適用する。GitHub Actions、Docker、開発端末で同じ`./gradlew`品質ゲートを使用する。
