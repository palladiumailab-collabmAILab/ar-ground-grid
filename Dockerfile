FROM ghcr.io/cirruslabs/android-sdk:36

ARG GRADLE_VERSION=9.5.0
RUN yes | sdkmanager "platforms;android-37" \
    && curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip \
    && unzip -q /tmp/gradle.zip -d /opt \
    && ln -s "/opt/gradle-${GRADLE_VERSION}/bin/gradle" /usr/local/bin/gradle \
    && rm /tmp/gradle.zip

WORKDIR /workspace
CMD ["gradle", "--no-daemon", "testDebugUnitTest", "assembleDebug"]
