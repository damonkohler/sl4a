package com.googlecode.android_scripting;

import android.content.Intent;

import java.io.File;

import junit.framework.TestCase;

public class IntentBuildersTest extends TestCase {

  private static final String mScriptPath = "/foo/bar/baz";
  private final File mScript;

  public IntentBuildersTest() {
    mScript = new File(mScriptPath);
  }

  public void testBuildStartInBackgroundIntent() {
    Intent intent = IntentBuilders.buildStartInBackgroundIntent(mScript);
    assertEquals(mScriptPath, intent.getStringExtra(Constants.EXTRA_SCRIPT_PATH));
  }

  public void testBuildStartInTerminalIntent() {
    Intent intent = IntentBuilders.buildStartInTerminalIntent(mScript);
    assertEquals(mScriptPath, intent.getStringExtra(Constants.EXTRA_SCRIPT_PATH));
  }
}
