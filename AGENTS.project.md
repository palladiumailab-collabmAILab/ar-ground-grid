# Project-specific Codex instructions

## Project purpose
- Develop AR Ground Grid from discovery through implementation while keeping product, FTO, and measurement assumptions explicit.

## Sources of truth
- `docs/specs/`: current product/system requirements.
- `docs/roadmap.md`: implementation order and decision gates.
- `docs/reference/`: technical/FTO references.
- `docs/decisions.md`: material decisions and rejected alternatives.

## Project invariants
- Separate external facts, evidence, and hypotheses.
- Keep target user, problem, MVP, success condition, and acceptance criteria explicit before implementation.
- Check `docs/reference/japan-fto-guardrails.md` before adding measurement UI, snapping/reticle, depth correction, 3D fitting, or object annotation covered by its re-FTO triggers.
- Before the real-device evaluation gate, do not pre-emptively implement custom SLAM/VIO/SfM, dense depth, scale calibration, or an external metric anchor unless the roadmap changes explicitly.
- Persist durable research in `docs/reference/` with source and date rather than only in Issue comments.

## Verification
- Use checks proportional to the changed implementation surface and the project's configured CI.
