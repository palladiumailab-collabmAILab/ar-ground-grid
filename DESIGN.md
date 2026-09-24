# Design

## Purpose

実世界の床・地面へ、実寸スケールのグリッドをARで重畳し、家具配置・DIY・現場確認などの空間把握を補助する。

## Design principles

- **実測可能性を優先する。** 見た目より、実機で評価可能な幾何・スケール・追跡挙動を優先する。
- **MVPを小さく保つ。** ARCore/SceneViewで成立する範囲を先に検証し、custom SLAM、custom depth、音響測距、backendは必要性が実証されるまで追加しない。
- **精度を推測で主張しない。** EmulatorやBlueStacksは動作確認用であり、実寸精度の根拠にはしない。
- **AR依存と幾何を分離する。** メートル単位のグリッド幾何はAndroid/ARCore非依存に保ち、UI/AR接続と分離する。

## Non-goals

- 現時点で測量器相当の精度保証を行うこと。
- 独自SLAMや独自depth推定を先行実装すること。
- cloud/backendを前提とすること。

## Source of truth

詳細仕様は `docs/specs/`、実装順序とdecision gateは `docs/roadmap.md`、設計判断は `docs/decisions.md` を正本とする。
