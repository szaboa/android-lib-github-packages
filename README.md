## Publishing AAR Library to Github Packages
This guide explains how to publish an AAR library to Github Packages using Github Actions.
### Create a Personal Access Token
Before publishing a library, we need to create a **Personal Access Token** that has `write:packages` permission. To do this, navigate to Github's *Settings* &rarr; *Developer Settings* &rarr; *Personal Access Token*. Add this token to the **Repository secret** as `PAT_TOKEN` under *Settings* &rarr; *Secrets and Variables* &rarr; *Repository secrets*.
### Gradle Configuration
In the library's `build.gradle file`, configure the `groupId`, `artifactId`, `versionCode`, `versionName` and add the publishing script as shown below:
```groovy
plugins {
    id 'com.android.library'
    id 'maven-publish'
}

ext {
    _groupId = "com.example"
    _artifactId = "example-lib"
    _versionCode = 10002
    _versionName = "0.0.2"

    _libraryName = "ExampleLib"
    _libraryDescription = "Example Library description!"
}

afterEvaluate {
    publishing {
        publications {
            release(MavenPublication) {
                from components.release

                groupId    _groupId
                artifactId _artifactId
                version    _versionName
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/szaboa/android-lib-github-packages")

                credentials {
                    username = System.getenv("GPR_USER")
                    password = System.getenv("GPR_KEY")
                }
            }
        }
    }
}

publish.dependsOn assemble
```

### Github Action
The following **Github Action** will build, publish and create a release of our library whenever a new tag is pushed:

```yml
name: Build Android Library

on:
  push:
    tags:
      - '*'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v2

      - name: Set up JDK 11
        uses: actions/setup-java@v1
        with:
          java-version: '11'

      - name: Build AAR ⚙️🛠
        run: ./gradlew :example-lib:assemble
        
      - name: Publish to GitHub Package Registry 🚀
        run: bash ./gradlew :example-lib:publish
        env:
          GPR_USER: ${{ github.actor }}
          GPR_KEY: ${{ secrets.PAT_TOKEN }}
          
      - name: Create Release ✅
        id: create_release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.PAT_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: ${{ github.ref }}
          draft: true
          prerelease: false
          
      - name: Upload Library AAR 🗳
        uses: actions/upload-release-asset@v1
        env:
          GITHUB_TOKEN: ${{ secrets.PAT_TOKEN }}
        with:
          upload_url: ${{ steps.create_release.outputs.upload_url }}
          asset_path: example-lib/build/outputs/aar/example-lib-release.aar
          asset_name: example-lib.aar
          asset_content_type: application/aar
```

## Add the library to your application
This section describes how to add the already published library to your application.
### Create a Personal Access Token to resolve the dependency
We need to create a **Personal Access Token** that has `read:packages` permission. This can be done on Github, under *Settings* &rarr; *Developer Settings* &rarr; *Personal Access Token*.

Create a file `github.properties` in the project's root folder and add your token:
```groovy
gpr.usr = <github-username>
gpr.key = <token>
```
### Gradle dependency
Add the Maven repository in `build.gradle`:
```groovy
def githubProperties = new Properties()
githubProperties.load(new FileInputStream(rootProject.file("github.properties")))

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/szaboa/android-lib-github-packages")
            credentials {
                username = githubProperties['gpr.usr']
                password = githubProperties['gpr.key']
            }
        }
    }
}
```
And finally, add the library dependency:
```groovy
dependencies {
    implementation 'com.example:example-lib:0.0.2'
    ...
}    
```
