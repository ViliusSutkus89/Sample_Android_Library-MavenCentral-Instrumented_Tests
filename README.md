# Sample Android library
[![vuild](https://github.com/ViliusSutkus89/Sample_Android_Library-MavenCentral-Instrumented_Tests/actions/workflows/build.yml/badge.svg)](https://github.com/ViliusSutkus89/Sample_Android_Library-MavenCentral-Instrumented_Tests/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89/samplelib.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.viliussutkus89%20AND%20a:samplelib)

The purpose of this project is to provide a sample library with a particular set of CI/CD features.

```gradle
dependencies {
    implementation 'com.viliussutkus89:samplelib:2.1.0'
}
```

## Features

Project contains an Android [library](lib) with [instrumented tests](lib/src/androidTest/java/com/viliussutkus89/samplelib/ExampleInstrumentedTest.java) and a [sample application](sampleapp).  
Library is built on GitHub Actions pipeline.  
Each successful build is published to a new staging repository in MavenCentral (Additional keywords: OSSRH, Nexus, Sonatype).
Publishing is done using [publish-plugin](https://github.com/gradle-nexus/publish-plugin).  
Instrumented tests are run on a matrix of emulated devices against the previously deployed library.

Build is released using `fullRelease.yml` or `appRelease.yml` manual workflow.  
Release implies promoting staging repository to MavenCentral and creating a new GitHub release.  
FullRelease releases both library and application. AppRelease only releases the application.

## Workflows

#### [build.yml](.github/workflows/build.yml)
Triggered either by push to main/master branch or manually (`workflow_dispatch`).  
Composed of three jobs:
1) buildLibrary:
   1) Compiles the library and signs it with a private key. 
   1) Deploys build artifacts to a Sonatype staging repository.
   1) Deploys build artifacts to MavenLocal (~/.m2).
   1) Artifacts MavenLocal.
   1) Artifacts lint report.
1) buildSampleAppStaging (depends on buildLibrary):
   1) Builds sample application against the library deployed to Sonatype staging repository.
   1) Artifacts APKs as sampleapp-staging-apks. Will not be attached to GitHub release.
   1) Artifacts lint report.
1) runInstrumentedTests (depends on buildLibrary):
   1) Runs instrumented tests on a matrix of emulated Android devices against the library deployed to Sonatype staging repository.
   1) Artifacts test reports.

#### [fullRelease.yml](.github/workflows/fullRelease.yml)
Triggered manually (`workflow_dispatch`).  
Requires input variable `STAGING_REPO_URL`, which is printed as a warning in
`buildLibrary` build job of `build.yml` workflow.  
Composed of three jobs:
1) releaseSonatype:  
   Promotes the Sonatype staging repository to MavenCentral.
1) releaseGitHub (depends on releaseSonatype):
   1) Waits for release to propagate to MavenCentral.
   1) Updates sample application version.
   1) Creates a GitHub release.
   1) Increments library version.
1) buildSampleApp (depends on releaseGitHub):
   1) Builds sample application against the released library.
   1) Attaches APKs and lint-results.html to GitHub release.

#### [appRelease.yml](.github/workflows/appRelease.yml)
Triggered manually (`workflow_dispatch`).

Composed of two jobs:
1) buildSampleApp:
   1) Builds sample application against the released library.
   2) Artifacts APKs and lint-results.html.
1) releaseGitHub (depends on buildSampleApp):
   1) Creates a GitHub release.
   1) Attaches artifacted APKs and lint-results.html to GitHub release.
   1) Increments sample application version.

#### [unprivilegedBuild.yml](.github/workflows/unprivilegedBuild.yml)
Triggered either by push to branch other than main/master or manually (`workflow_dispatch`).  
Composed of three jobs:
1) buildLibrary:
   1) Compiles the library.
   1) Deploys build artifacts to MavenLocal (~/.m2).
   1) Artifacts MavenLocal.
   1) Artifacts lint report.
1) buildSampleApp. Depends on buildLibrary.  
   1) Builds the sample application against the library deployed to a staging repository in MavenLocal (~/.m2).
   1) Artifacts APKs.
   1) Artifacts lint report.
1) runInstrumentedTests. Depends on buildLibrary.  
   1) Runs instrumented tests on a matrix of emulated Android devices against the library deployed to a staging repository in MavenLocal (~/.m2).
   1) Artifacts test reports.

#### [manualVersionIncrement_{major,minor,patch}.yml](.github/workflows/manualVersionIncrement_major.yml)
Triggered only manually (`workflow_dispatch`).  
Used to increment project version and commit changes to source control.

## Environments

#### LibraryKeyAndSonatypeAccess - (build workflow, buildLibrary job)
Environment contains the following secrets:  
`SIGNING_KEY`, `SIGNING_PASS` - ASCII armored private key and password used for signing library artifacts.  
`SONATYPE_USERNAME`, `SONATYPE_PASSWORD` - User token (not the actual login to oss.sonatype.org), obtained through oss.sonatype.org -> Profile -> User Token.

#### SonatypeAccess - (fullRelease workflow, releaseSonatype job)
Environment contains the following secrets:   
`SONATYPE_USERNAME`, `SONATYPE_PASSWORD` - User token (not the actual login to oss.sonatype.org), obtained through oss.sonatype.org -> Profile -> User Token.

#### TenMinuteWait - (fullRelease workflow, releaseGitHub job)
A timed gate. Release propagation to MavenCentral takes over ten minutes. Timed gate waits a set amount of time without having a build job running.

#### SampleAppKeystore - (fullRelease and appRelease workflows, buildSampleApp job; build workflow, buildSampleAppAgainstUnReleasedLibrary and buildSampleAppAgainstReleasedLibrary jobs)
Environment contains the following secrets:  
`APP_SIGNING_KEYFILE_BASE64`, `APP_SIGNING_PASS`, `APP_SIGNING_ALIAS` - keystore used for sample application signing.

## Cryptographic signature

MavenCentral requires all artifacts to be cryptographically signed.
GPG can be used to generate RSA keypair.
```shell
$ gpg --full-gen-key
gpg (GnuPG) 2.2.20; Copyright (C) 2020 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
   (1) RSA and RSA (default)
   (2) DSA and Elgamal
   (3) DSA (sign only)
   (4) RSA (sign only)
  (14) Existing key from card
Your selection? 4
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048) 4096
Requested keysize is 4096 bits
Please specify how long the key should be valid.
         0 = key does not expire
      <n>  = key expires in n days
      <n>w = key expires in n weeks
      <n>m = key expires in n months
      <n>y = key expires in n years
Key is valid for? (0) 1y
Key expires at Thu Jun 09 05:13:17 2022 EEST
Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: Vilius Sutkus '89
Email address: ViliusSutkus89@gmail.com
Comment: Sample_Android_Library-MavenCentral-Instrumented_Tests signing key
You selected this USER-ID:
    "Vilius Sutkus '89 (Sample_Android_Library-MavenCentral-Instrumented_Tests signing key) <ViliusSutkus89@gmail.com>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
We need to generate a lot of random bytes. It is a good idea to perform
some other action (type on the keyboard, move the mouse, utilize the
disks) during the prime generation; this gives the random number
generator a better chance to gain enough entropy.
gpg: key CB501D449281F9CD marked as ultimately trusted
gpg: revocation certificate stored as '/home/user/.gnupg/openpgp-revocs.d/9545ABF95D3ED906902B2240CB501D449281F9CD.rev'
public and secret key created and signed.

Note that this key cannot be used for encryption.  You may want to use
the command "--edit-key" to generate a subkey for this purpose.
pub   rsa4096 2021-06-09 [SC] [expires: 2022-06-09]
      9545ABF95D3ED906902B2240CB501D449281F9CD
uid           [ultimate] Vilius Sutkus '89 (Sample_Android_Library-MavenCentral-Instrumented_Tests signing key) <ViliusSutkus89@gmail.com>
```

Build job in BuildWithDeployToSonatype environment needs access to the secret key and password used when creating it.
Secret key is exported using:
```shell
$ gpg --armor --export-secret-keys 9545ABF95D3ED906902B2240CB501D449281F9CD 
-----BEGIN PGP PRIVATE KEY BLOCK-----
... [redacted] ...
-----END PGP PRIVATE KEY BLOCK-----
```
Save them as `SIGNING_KEY` and `SIGNING_PASS` environment secrets.

MavenCentral searches for public keys in SKS and other public key servers. It can be uploaded through GPG:
```shell
$ gpg --send-keys --keyserver hkp://eu.pool.sks-keyservers.net 0x9545ABF95D3ED906902B2240CB501D449281F9CD
gpg: sending key CB501D449281F9CD to hkp://eu.pool.sks-keyservers.net
```

Public key can also be uploaded manually (for example to [keys.gnupg.net](http://keys.gnupg.net), [keyserver.ubuntu.com](https://keyserver.ubuntu.com)) by exporting it first:
```shell
$ gpg --armor --export 9545ABF95D3ED906902B2240CB501D449281F9CD 
-----BEGIN PGP PUBLIC KEY BLOCK-----

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
=vf/G
-----END PGP PUBLIC KEY BLOCK-----
```

#### Signed sample application
Gradle needs a keystore to sign the application. Keystore is generated using keytool. JRE includes keytool binary.  
Validity needs to be long, because unlike GPG keys, keystore validity cannot be extended.
```shell
$ keytool -genkey -v -keystore com.viliussutkus89.samplelib.sampleapp.jks -keyalg RSA -keysize 4096 -validity 10000 -alias my-alias
Enter keystore password:  
Re-enter new password: 
What is your first and last name?
  [Unknown]:  com.viliussutkus89.samplelib.sampleapp                        
What is the name of your organizational unit?
  [Unknown]:  Android apps
What is the name of your organization?
  [Unknown]:  ViliusSutkus89.com
What is the name of your City or Locality?
  [Unknown]:  Kaunas
What is the name of your State or Province?
  [Unknown]:  Kaunas
What is the two-letter country code for this unit?
  [Unknown]:  LT
Is CN=com.viliussutkus89.samplelib.sampleapp, OU=Android apps, O=ViliusSutkus89.com, L=Kaunas, ST=Kaunas, C=LT correct?
  [no]:  yes

Generating 4,096 bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 10,000 days
	for: CN=com.viliussutkus89.samplelib.sampleapp, OU=Android apps, O=ViliusSutkus89.com, L=Kaunas, ST=Kaunas, C=LT
[Storing com.viliussutkus89.samplelib.sampleapp.jks]
```
Even though the keystore file is password protected, there is no need to commit it to git.  
Encode it as base64 and save it as a secret named `APP_SIGNING_KEYFILE_BASE64` in the environment ReleaseGitHub.  
```shell
$ base64 com.viliussutkus89.samplelib.sampleapp.jks
... [redacted] ...
```
Also include `APP_SIGNING_PASS` and `APP_SIGNING_ALIAS` ("my-alias" in the example) in the same environment.

## CI/CD scripts

Used by CI/CD pipelines to either obtain some information or modify the project.

#### [getVersion](ci-scripts/getVersion)
Obtains and prints out current project version from [build.gradle](build.gradle).

#### [incrementVersion](ci-scripts/incrementVersion)
Increments project version and versionCode in library files.

#### [updateDownstreamVersion](ci-scripts/updateDownstreamVersion)
Updates version and versionCode in downstream files (this README.md, sample application) to match current library version.

#### [prepareLibraryTestsForStagingRepository](ci-scripts/prepareLibraryTestsForStagingRepository)
Library contains sources and instrumented tests.  
Used to extract instrumented tests from the library into a new project, which does not have library sources.
The newly created project depends on the previously built library, which is deployed to a staging repository.
Staging repository is either in MavenCentral (build workflow) or MavenLocal (unprivilegedBuild workflow).

#### [prepareSampleAppForStagingRepository](ci-scripts/prepareSampleAppForStagingRepository)
Sample application depends on a released version of the library.
This script modifies sample application to create a new temporary project, which depends on library in staging repository.
Using by the build jobs of build and unprivilegedBuild workflows.
On release, production environment builds the sample application again, which does not rely on this script.

## MavenCentral

Publishing to MavenCentral requires setting up an account with Sonatype.  
Follow the [Getting started guide](https://central.sonatype.org/publish/publish-guide/).  
[Publish-plugin](https://github.com/gradle-nexus/publish-plugin) is a required read to figure out how to set Sonatype host to s01.oss.sonatype.org.

## Version

Library is versioned semantically - `$Major.$Minor.$Patch`.\
Library patch version is incremented automatically after each release.

Application consumes previously released library. Previously released library has version lower than CURRENT version in source control.

Application is versioned by appending release number to library version, resulting in `$LibraryMajor.$LibraryMinor.$LibraryPatch.$ApplicationReleaseNumber`.\
Since version 2.0.0, application release number is always incremental, does not reset to zero after library releases. 
