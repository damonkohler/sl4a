/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ServiceHelper extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent launchIntent = getIntent().getParcelableExtra("launchIntent");
    setPersistent(true);
    startActivityForResult(launchIntent, getIntent().getIntExtra("requestCode", 0));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 0) {
      Intent intent = new Intent(this, AseService.class);
      intent.setAction(Constants.ACTION_ACTIVITY_RESULT);
      intent.putExtra("requestCode", requestCode);
      intent.putExtra("resultCode", resultCode);
      intent.putExtra("data", data);
      startService(intent);
      setPersistent(false);
      finish();
    }
  }
}
