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
- `docs/research.md` — 技術調査
- `docs/decisions.md` — 設計判断

将来のiPhone対応では、グリッド仕様・幾何を維持し、AR層をARKit/RealityKit側へ置き換える。

## Build

Android Studioでプロジェクトを開くか、Dockerで再現可能な品質ゲートを実行する。

```bash
docker build -t ar-ground-grid .
docker run --rm -v "$PWD:/workspace" -w /workspace ar-ground-grid
```

Dockerを使わない場合は JDK 17+、Android SDK 37、Gradle 9.5.0 が必要。

```bash
gradle --no-daemon lintDebug testDebugUnitTest assembleDebug
```

## Device requirements

AR Requiredアプリのため、ARCore対応Android端末とGoogle Play Services for ARが必要。`minSdk = 24`。`compileSdk = 37`、`targetSdk = 36`。

## Current phase

**Phase: Android MVP implementation**

実装後の完了条件は `docs/specs/requirements.md` を参照する。特に、実機で1 m基準長との誤差を測定するまでは「計測精度確認済み」としない。

## Harness

`codex-dev-harness` の共通開発原則を適用する。GitHub ActionsとDockerでAndroid Lint、JVM単体テスト、debug buildを同じGradleコマンドで実行する。
