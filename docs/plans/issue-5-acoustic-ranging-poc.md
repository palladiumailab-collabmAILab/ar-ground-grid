# Issue #5 implementation plan: Android acoustic ranging PoC

## Goal

Validate whether a commodity Android phone can estimate distance to a passive wall or other reflective surface using only its speaker and microphone, well enough to serve as a metric-scale aid for the AR ground-grid concept.

This plan implements the Phase 1 PoC described in #5. It does **not** implement ARCore integration, generic two-point length measurement, UWB/Bluetooth/Wi-Fi ranging, or product UI.

## Acceptance criteria

The PoC is considered technically promising only when measured on real Android hardware and all of the following hold:

- measurement range: 0.3-3.0 m
- no external responder/tag/device
- P90 absolute distance error <= 5 cm over the defined baseline test set
- repeated static measurements are recorded so variance can be quantified
- failures are observable rather than silently converted into a distance

A desktop/JVM unit test or Android emulator result cannot satisfy the real-device accuracy criterion.

## Baseline design

### Signal

Start with one deliberately narrow baseline:

- sample rate: 48 kHz
- linear chirp: 8-16 kHz
- short transmit/listen cycle rather than continuous application-level ranging
- PCM 16-bit mono unless device capability requires a different supported format

Near-ultrasonic 15-20 kHz and 96 kHz sampling remain experiments, not baseline requirements. They should be added only if the baseline cannot reach the acceptance criteria or audible UX becomes a blocking concern.

### Ranging algorithm

Use matched filtering / cross-correlation first because #5 explicitly allows it and it is the smallest algorithm that can validate the core hypothesis.

Pipeline:

1. generate a deterministic chirp
2. play it with `AudioTrack`
3. record synchronously with `AudioRecord`
4. correlate the recording with the transmitted chirp
5. identify the direct-path/reference peak
6. search for a plausible later reflection peak inside the 0.3-3.0 m window
7. convert peak separation to distance with `distance = delta_t * sound_speed / 2`
8. return either a measurement plus diagnostics or an explicit failure reason

Do not add FMCW dechirping, phase-based displacement estimation, beamforming, SLAM, or learned peak classification before the baseline is measured and shown insufficient.

## Minimal architecture

Keep Android I/O separate from signal processing so the latter is deterministic and unit-testable.

```text
app/
  audio/
    AudioDuplexEngine      # AudioTrack/AudioRecord only
    AudioCapabilityProbe   # supported rates/sources/effects
  ranging/
    ChirpGenerator         # pure Kotlin
    CrossCorrelator        # pure Kotlin
    PeakDetector           # pure Kotlin
    RangeEstimator         # pure Kotlin, no Android dependency
    RangingResult          # measurement or explicit failure
  measurement/
    MeasurementRunner      # orchestrates one measurement cycle
    MeasurementRecord      # expected distance + result + diagnostics
  ui/
    MainActivity           # start/stop, current result, failure state
```

No repository/service/DI framework is introduced for the PoC. Constructors and interfaces are sufficient unless a concrete testing problem appears.

## Device-audio handling

The implementation must record the actual conditions under which each measurement ran:

- requested and actual sample rate
- audio source used
- whether AGC/noise suppression/acoustic echo cancellation appear available/enabled
- input/output route where Android exposes it
- device model and Android version

Prefer the least-processed capture path supported by the device. Do not assume preprocessing can always be disabled; unsupported combinations must be reported in diagnostics.

## Measurement data

For each run, persist/export a compact record sufficient to reproduce the aggregate metrics:

- timestamp
- expected distance in metres
- estimated distance or failure reason
- absolute error when an estimate exists
- peak lag / peak score or equivalent confidence diagnostic
- device/audio configuration
- optional environment labels entered by the tester: surface, angle, noise, orientation

Raw PCM capture is optional behind a debug-only switch. It should not be required for normal measurement runs because it increases storage and handling complexity.

## Evaluation protocol

Baseline distances:

- 0.3 m
- 0.5 m
- 1.0 m
- 2.0 m
- 3.0 m

At each distance, collect enough repeated samples to compute at least:

- mean absolute error
- P50 absolute error
- P90 absolute error
- max absolute error
- failure rate
- standard deviation for static repeated measurements

The first acceptance run should use one flat, acoustically reflective wall and a fixed phone orientation. Material, angle, noise, orientation, alternative speaker/microphone paths, 96 kHz, and near-ultrasonic bands are follow-up matrices after the baseline works.

## Validation strategy

### Unit tests

Pure-Kotlin tests cover:

- chirp length and frequency sweep bounds
- correlation peak location for synthetic delayed signals
- distance conversion from sample lag
- rejection outside the allowed 0.3-3.0 m window
- peak detector behaviour for no-echo / ambiguous-echo fixtures

Synthetic fixtures should include additive noise and at least one competing reflection, but should remain small and deterministic.

### Android tests / manual checks

- app builds and starts on a physical Android device
- microphone permission failure is explicit
- unsupported audio configuration is explicit
- one measurement cycle terminates and produces result/diagnostics
- repeated measurements do not leak or leave playback/recording active

### CI

Because executable Android code enters the repository in the implementation PR, add GitHub Actions for the checks that do not require hardware:

- Gradle build
- JVM unit tests
- Android lint

Do not claim the acoustic accuracy criterion from CI; it requires real-device measurements.

## Implementation sequence

### Slice 1 — project skeleton and deterministic DSP

- create minimal Android project
- add Gradle wrapper and CI
- implement `ChirpGenerator`, `CrossCorrelator`, `PeakDetector`, `RangeEstimator`
- add synthetic unit tests

Exit: DSP pipeline can recover known synthetic delays and the Android project builds in CI.

### Slice 2 — real audio I/O

- implement `AudioDuplexEngine`
- add permission/capability handling
- connect one-shot playback/recording to `MeasurementRunner`
- expose diagnostics and explicit failure states

Exit: physical device can run one measurement cycle without ARCore or other sensors.

### Slice 3 — measurement logging and evaluation

- add expected-distance entry
- record/export compact measurement rows
- compute MAE/P50/P90/max/failure rate/stddev from recorded runs
- document the baseline real-device procedure

Exit: the Issue #5 Go/No-Go metric can be calculated from captured data.

### Slice 4 — only if baseline misses the criterion

Use captured evidence to choose one next experiment at a time, for example:

- alternate chirp band or duration
- 96 kHz where supported
- audio preprocessing changes
- improved direct-path cancellation / echo peak selection
- FMCW dechirp

Do not implement all candidates pre-emptively.

## Non-goals

- ARCore scale correction or sensor fusion
- arbitrary two-point length measurement
- room mapping
- multi-surface semantic selection
- UWB, Bluetooth Channel Sounding, Wi-Fi RTT, BLE RSSI, GNSS, barometer
- production UI, analytics, cloud backend, accounts, database
- custom native/C++ DSP before Kotlin performance is shown insufficient
- third-party DSP dependency unless the baseline needs functionality that is costly or risky to maintain in-house

## Done definition for Issue #5 PoC implementation

Implementation work following this plan is complete when:

- the Android app builds through the repository CI gate
- deterministic DSP tests pass
- a physical phone can execute and log measurements
- baseline measurements cover 0.3/0.5/1/2/3 m
- MAE, P50, P90, max error, failure rate, and static variance are reported
- the P90 <= 5 cm Go/No-Go criterion is explicitly evaluated
- limitations and the next decision are recorded in `docs/decisions.md`

## Follow-up decision

If the criterion passes, create a separate issue for evaluating acoustic distance as an ARCore scale anchor. If it fails, keep Issue #5 scoped to the measured acoustic failure and open only the smallest evidence-backed follow-up experiment.