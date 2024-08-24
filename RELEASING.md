# Releasing

* Install GitHub CLI if needed
```bash
brew install gh
```

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
```bash
mate CHANGELOG.md
```	

* Update the released version in the readme
```bash
mate README.md
```	

* Commit all local changes
```bash
git commit -am "Prepare {NEW_VERSION} release"
```

* Perform a clean build
```bash
./gradlew clean && ./gradlew build && ./gradlew connectedCheck
```

* Create a tag and push it
```bash
git tag v{NEW_VERSION}
git push origin v{NEW_VERSION}
```

* Run the _Publish Release_ workflow
```bash
gh workflow run publish-release.yml --ref v{NEW_VERSION}
```

Alternatively, you can run the workflow manually from the GitHub UI [here](https://github.com/square/radiography/actions/workflows/publish-release.yml) and select it to run from the release tag.

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
* Tell your friends, update all of your apps, and tweet the new release. 
  As a nice extra touch, mention external contributions.
