FROM ghcr.io/cirruslabs/android-sdk:36

RUN yes | sdkmanager "platforms;android-37.0"

WORKDIR /workspace
COPY gradle gradle
COPY gradle.properties build.gradle.kts settings.gradle.kts gradlew ./
COPY app app
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

CMD ["./gradlew", "--no-daemon", "lintDebug", "testDebugUnitTest", "assembleDebug"]
