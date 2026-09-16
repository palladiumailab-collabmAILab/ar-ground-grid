# hoge

新しいアプリの構想を整理し、調査・要件定義を経て実装可否を判断するためのプライベートリポジトリ。

`codex-dev-harness` の共通開発原則を適用する。現時点は Discovery フェーズのため、実装用の Docker / Ruff / GitHub Actions はまだ導入せず、実行コードを追加する段階でプロジェクト構成に合わせて導入する。

## 進め方

1. `docs/specs/product.md` で解決したい課題、対象ユーザー、価値、成功条件を定義する
2. `docs/research.md` に既存サービス、技術、根拠を記録する
3. `docs/specs/requirements.md` に MVP、機能要件、非機能要件、受入条件を落とす
4. 重要な判断は `docs/decisions.md` に残す
5. 実装に進む段階で Issue に分解し、必要な品質ゲートを追加する

## 現在の状態

**Phase: Discovery**

まだアプリ案は確定していない。最初のゴールは、解く価値のある課題と検証可能な成功条件を定義すること。

## Documents

- [Specifications](docs/specs/README.md)
- [Product](docs/specs/product.md)
- [Requirements](docs/specs/requirements.md)
- [Research](docs/research.md)
- [Decisions](docs/decisions.md)
- [Agent rules](AGENTS.md)

## Harness baseline

実装フェーズに入ったら、対象技術に応じて以下を適用する。

- Docker による再現可能な開発・検証経路
- Python を含む場合は Ruff による lint / format
- GitHub Actions による PR / default branch の遠隔品質ゲート
- テスト、型チェック、build、repository invariant、domain validator のうち必要なもの

技術スタック、framework、DB、service topology は要件に基づいて決め、ハーネスから一律には固定しない。
