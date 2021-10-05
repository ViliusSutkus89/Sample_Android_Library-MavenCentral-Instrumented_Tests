Splitting off release logic from privilegedBuild.yml into a separate manually dispatched workflow.

### Upsides

Build badge.
No "build failed" spam for each unreleased build run.

### Downside

GitHub Release does not have following assets:

`maven-local.tar`, `library-lint-report.html` and `instrumentedTestsReport-*-*.tar`.

This is because they are available as artifacts in privilegedBuild.yml workflow.
Not sure if there's a way to obtain them programmatically from a different workflow. Not sure if there's a need for this. 

Application APK and Application lint report remains attached.
