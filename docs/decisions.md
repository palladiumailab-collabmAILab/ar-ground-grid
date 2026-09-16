# Decisions

重要な設計・製品判断を時系列で残す。

## 2026-09-16 — Issue #5 acoustic ranging PoC baseline

- Status: Proposed
- Context: Issue #5 identifies speaker/microphone acoustic ranging as the only investigated option that can directly range a passive wall without an external responder. The repository is still in Discovery and has no executable Android project yet.
- Options: (1) implement matched-filter/cross-correlation ranging first; (2) start with full FMCW dechirp/phase processing; (3) immediately integrate acoustic ranging with ARCore; (4) explore all wireless ranging alternatives in code.
- Decision: Plan a standalone Android PoC using a 48 kHz 8-16 kHz chirp, `AudioTrack` + `AudioRecord`, and matched filtering/cross-correlation. Keep DSP pure Kotlin and Android audio I/O isolated. Evaluate 0.3-3.0 m with P90 absolute error <= 5 cm as the initial Go/No-Go criterion before ARCore integration or more complex DSP.
- Evidence: Issue #5 permits matched filtering or FMCW dechirp for Phase 1 and defines the same range and P90 target. The matched-filter baseline has fewer moving parts and can directly test whether phone audio hardware is viable.
- Consequences: The first executable-code PR must add an Android build/CI path and physical-device evaluation procedure. CI can validate deterministic DSP/build quality but cannot establish real acoustic accuracy. Near-ultrasonic/96 kHz, phase processing, and ARCore fusion are deferred until baseline measurements justify them.
- Revisit when: baseline real-device measurements are available or the baseline cannot produce a stable direct/reflection peak pair.

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

---
