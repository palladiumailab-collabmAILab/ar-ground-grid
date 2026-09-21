# Decisions

重要な設計・製品判断を時系列で残す。

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

### 2026-09-21 — Android MVP implementation boundary

- Status: Accepted
- Context: Documentation-only main must become an implementation-ready Android MVP without preempting real-device evidence.
- Options: Add custom SLAM/depth/ranging now, or keep the MVP on ARCore plane tracking and defer alternatives.
- Decision: Use Kotlin + Compose + ARCore + SceneView 4.35.0; keep metric grid geometry platform-independent; defer calibration, custom depth, acoustic ranging, and arbitrary measurement until #7 evidence.
- Evidence: Android Lint, JVM unit tests, debug build, Gradle Wrapper/checksum, Docker, and GitHub Actions pass on the MVP head.
- Consequences: Main can build the MVP after the implementation PR is merged; accuracy and drift remain unverified until real-device evaluation.
- Revisit when: #7 has trial-level 1 m / 3 m accuracy and stability data.
