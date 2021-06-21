package com.viliussutkus89.samplelib;

public class VersionGetter {
    public static String getVersion() {
        return "Code=" + BuildConfig.VERSION_CODE + ", name=" + BuildConfig.VERSION_NAME;
    }

    public static String getGitCommit() {
        return BuildConfig.GIT_COMMMIT;
    }
}
