package com.viliussutkus89.samplelib;

import static org.junit.Assert.assertNotEquals;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.screenshot.ScreenCapture;
import androidx.test.runner.screenshot.Screenshot;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public RuleChain screenshotRule;

    @Before
    public void screenshotRuleChain() {
        // Android R requires storage permission finessing
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            screenshotRule = RuleChain
                    .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .around(new TestWatcher() {
                        @Override
                        protected void failed(Throwable e, Description description) {
                            super.failed(e, description);
                            ScreenCapture capture = Screenshot.capture()
                                    .setName(description.getTestClass().getSimpleName() + "-" + description.getMethodName())
                                    .setFormat(Bitmap.CompressFormat.PNG);
                            try {
                                capture.process();
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                        }
                    });
        }
    }

    @Test
    public void useGetVersion() {
        assertNotEquals("0", VersionGetter.getVersion());
    }

    @Test
    public void useGetGitCommit() {
        assertNotEquals("xxx", VersionGetter.getGitCommit());
    }
}
