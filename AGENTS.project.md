# Project-specific Codex instructions

## Project purpose

This repository is a discovery and implementation workspace for an AR ground-grid application.

## Sources of truth

- `docs/specs/`: current product/system requirements.
- `docs/roadmap.md`: implementation order and decision gates.
- `docs/reference/`: technical references, prior art, and alternatives.
- `docs/research.md`: research work notes.
- `docs/decisions.md`: decisions and rejected alternatives.
- GitHub Issues: current work items, not a substitute for current specifications.

## Project invariants

- Distinguish facts, external evidence, and hypotheses; cite primary or reliable sources when practical and do not assert unverified claims.
- Before implementation, define the target user, problem, MVP, success conditions, and acceptance criteria.
- Record important decisions and rejection reasons in `docs/decisions.md`.
- Move long-lived research out of Issue comments into `docs/reference/` with source and research date.
- Before adding measurement UI, snapping/reticle, depth correction, 3D fitting, or object annotation, check the re-FTO triggers in `docs/reference/japan-fto-guardrails.md`.
- Before the real-device evaluation in Issue #7, do not preemptively implement custom SLAM/VIO/SfM/dense depth, scale calibration, or an external metric anchor.
