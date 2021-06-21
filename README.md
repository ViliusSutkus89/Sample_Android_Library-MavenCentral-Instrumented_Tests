# Sample Android library
The purpose of this project is to provide a sample library with a particular set of CI/CD features.

[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89/samplelib.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:com.viliussutkus89%20AND%20a:samplelib)

## Features

Project contains an Android [library](lib) with [instrumented tests](lib/src/androidTest/java/com/viliussutkus89/samplelib/ExampleInstrumentedTest.java) and a [sample application](sampleapp).  
Library is built on GitHub Actions pipeline.  
Each successful build is deployed to a new staging repository in MavenCentral (Additional keywords: OSSRH, Nexus, Sonatype).  
Instrumented tests are run against the previously deployed library on a matrix of emulated devices, also in the GitHub Actions pipeline.  
Build, which passes instrumented tests, can be promoted to production.  
Build promotion to production creates a new GitHub release.

## Drawbacks

No build badge - badge status updates only when the whole workflow finishes. Either failed or succeeded.  
Current implementation has only two types of finished workflows: failed builds and released build.  
This means that after a build break, the badge will stay red until a new release, not until a passing build.

Build artifacts from Sample application are not signed yet.

## Workflows

#### [privilegedBuild.yml](.github/workflows/privilegedBuild.yml)
Triggered either by push to master or main branch or manually (`workflow_dispatch`).
Composed of four jobs:
1) build:
   1) Compiles the library, signs it with a private key. 
   1) Deploys build artifacts to a staging repository in MavenCentral.
   1) Deploys build artifacts to MavenLocal (~/.m2) for easier file access during GitHub release creation.
1) staging:
   1) Prepares instrumented tests to be executed against library in the staging repository.
   1) Runs instrumented tests on a matrix of emulated Android devices.
1) releaseSonatype: Promotes the staging repository to MavenCentral.
1) releaseGitHub:
   1) Increments library version in git repository.
   1) Builds sample application against the newly released library.
   1) Creates GitHub release and post release version increment commit.

#### [unprivilegedBuild.yml](.github/workflows/unprivilegedBuild.yml)
Triggered either by pull request or manually (`workflow_dispatch`).
1) Build. Compiles the library, deploys to mavenLocal (~/.m2), builds sample application.
2) Staging. Runs instrumented tests on a matrix of emulated Android devices against the library deployed to a staging repository in MavenLocal (~/.m2).

#### [manualVersionIncrement_{major,minor,patch}.yml](.github/workflows/manualVersionIncrement_major.yml)
Triggered only manually (`workflow_dispatch`).  
Used to increment project version and commit changes to source control.

## Environments
#### BuildWithDeployToSonatype - (privilegedBuild workflow)
Used by the build job.  
Environment contains the following secrets:  
`SIGNING_KEY`, `SIGNING_PASS` - ASCII armored private key and password.  
`SONATYPE_USERNAME`, `SONATYPE_PASSWORD` - User token (not the actual login to oss.sonatype.org), obtained through oss.sonatype.org -> Profile -> User Token.

#### Staging - (privilegedBuild workflow)
Used by the staging job, which depends on build job.  
Has no need to be an actual environment, because it contains no secrets and no protection rules.  
Is an environment just for clarity.

#### ReleaseSonatype - (privilegedBuild workflow)
Used by the releaseSonatype job, which depends on staging job.  
Has manual review protection rule, which is used to gate builds.  
Environment contains the following secrets:   
`SONATYPE_USERNAME`, `SONATYPE_PASSWORD` - User token (not the actual login to oss.sonatype.org), obtained through oss.sonatype.org -> Profile -> User Token.

#### ReleaseGitHub - (privilegedBuild workflow)
Used by the releaseGitHub job, which depends on releaseSonatype job.
Has a timed gate, to ensure Sonatype release propagation to MavenCentral.
Environment contains no secrets.

#### BuildUnprivileged - (unprivilegedBuild workflow)
Used by the build and staging jobs in the unprivilegedBuild workflow.
Has no need to be an actual environment, because it contains no secrets and no protection rules.  
Is an environment just for clarity.

## PGP signature

Build artifacts are signed using an RSA key.
Public key is available at
[0x9545ABF95D3ED906902B2240CB501D449281F9CD.asc](0x9545ABF95D3ED906902B2240CB501D449281F9CD.asc) or
[keys.gnupg.net](http://keys.gnupg.net/pks/lookup?search=0x9545ABF95D3ED906902B2240CB501D449281F9CD&fingerprint=on&hash=on&exact=on&op=vindex).

## CI/CD scripts

Used by CI/CD pipelines to either obtain some information or modify the project.

#### [getVersion](scripts/getVersion)
Obtains and prints out current project version from [build.gradle](build.gradle).

#### [incrementVersion](scripts/incrementVersion)
Increments project version and versionCode in various files used by the library and sample application.

#### [prepareLibraryTestsForStagingRepository](scripts/prepareLibraryTestsForStagingRepository)
Library contains sources and instrumented tests.  
Used to extract instrumented tests from the library into a new project, which does not have library sources.
The newly created project depends on the previously built library, which is deployed to a staging repository.
Staging repository is either in MavenCentral (privilegedBuild workflow) or MavenLocal (unprivilegedBuild workflow).

#### [prepareSampleAppForStagingRepository](scripts/prepareSampleAppForStagingRepository)
Sample application depends on a released version of the library.
This script modifies sample application to create a new temporary project, which depends on library in staging repository.
Using by the build jobs of privilegedBuild and unprivilegedBuild workflows.
On release, production environment builds the sample application again, which does not rely on this script.

#### [checkIfVersionReleased](scripts/checkIfVersionReleased)
Checks if given URL is reachable. Used to verify that a released version is actually available in MavenCentral.
Configurable timeout and retry count allows waiting until the version becomes available.

#### [promoteStagingRepository](scripts/promoteStagingRepository)
Promotes a given staging repository to MavenCentral.
