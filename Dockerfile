FROM ghcr.io/cirruslabs/android-sdk:36

RUN yes | sdkmanager "platforms;android-37"

WORKDIR /workspace
CMD ["./gradlew", "--no-daemon", "lintDebug", "testDebugUnitTest", "assembleDebug"]
