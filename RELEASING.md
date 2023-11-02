# Releasing

## Preparing the release environment

### Set up your Sonatype OSSRH account

* Create a [Sonatype OSSRH JIRA account](https://issues.sonatype.org/secure/Signup!default.jspa).
* Create a ticket to request access to the `com.squareup.radiography` project. Here's an example: [OSSRH-54959](https://issues.sonatype.org/browse/OSSRH-54959).
* Then ask someone with deployer role from the team to confirm access.

### Set up your signing key

```bash
# Create a new key
gpg --gen-key
# List local keys. Key id is last 8 characters
gpg -K
cd ~/.gnupg
# Export key locally
gpg --export-secret-keys -o secring.gpg
# Upload key to Ubuntu servers
gpg --send-keys --keyserver keyserver.ubuntu.com <KEY ID>
# Confirm the key can now be found
gpg --recv-keys --keyserver keyserver.ubuntu.com <KEY ID>
```

### Set up your home gradle.properties

Add this to your `~/.gradle/gradle.properties`:

```
signing.keyId=<KEY ID>
signing.password=<KEY PASSWORD>
signing.secretKeyRingFile=/Users/YOUR_USERNAME_/.gnupg/secring.gpg
SONATYPE_NEXUS_USERNAME=<SONATYPE_USERNAME>
SONATYPE_NEXUS_PASSWORD=<SONATYPE_PASSWORD>
```

### Set up GitHub CLI

Install GitHub CLI

```bash
brew install gh
```

## Releasing

* Create a local release branch from `main`
```bash
git checkout main
git pull
git checkout -b release_{NEW_VERSION}
```

* Update `VERSION_NAME` in `gradle.properties` (remove `-SNAPSHOT`)
```gradle
sed -i '' 's/VERSION_NAME={NEW_VERSION}-SNAPSHOT/VERSION_NAME={NEW_VERSION}/' gradle.properties
```

* Update the changelog
```
mate CHANGELOG.md
```	

* Update the released version in the readme
```
mate README.md
```	

* Commit all local changes
```bash
git commit -am "Prepare {NEW_VERSION} release"
```

* Perform a clean build
```bash
./gradlew clean
./gradlew build
```

* Create a tag and push it
```bash
git tag v{NEW_VERSION}
git push origin v{NEW_VERSION}
```

* Upload the artifacts to Sonatype OSS Nexus
```bash
./gradlew publish --no-daemon --no-parallel --no-configuration-cache && \
./gradlew closeAndReleaseRepository
```

* Merge the release branch to main
```bash
git checkout main
git pull
git merge --no-ff release_{NEW_VERSION}
```
* Update `VERSION_NAME` in `gradle.properties` (increase version and add `-SNAPSHOT`)
```gradle
sed -i '' 's/VERSION_NAME={NEW_VERSION}/VERSION_NAME={NEXT_VERSION}-SNAPSHOT/' gradle.properties
```

* Commit your changes
```bash
git commit -am "Prepare for next development iteration"
```

* Push your changes
```bash
git push
```

* Create a new release
```bash
gh release create v{NEW_VERSION} --title v{NEW_VERSION} --notes 'See [Change Log](https://github.com/square/radiography/blob/main/CHANGELOG.md)'
```

* Wait for the release to be available [on Maven Central](https://repo1.maven.org/maven2/com/squareup/radiography/radiography/).
* Tell your friends, update all of your apps, and tweet the new release. As a nice extra touch, mention external contributions.