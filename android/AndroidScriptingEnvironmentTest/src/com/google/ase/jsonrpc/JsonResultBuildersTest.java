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

package com.google.ase.jsonrpc;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ase.jsonrpc.JsonResultBuilders;

import android.content.Intent;

public class JsonResultBuildersTest extends TestCase {

  public void testBuildJsonList() throws JSONException {
    List<JSONObject> objects = new ArrayList<JSONObject>();
    JSONObject foo = new JSONObject();
    JSONObject bar = new JSONObject();
    JSONObject baz = new JSONObject();
    objects.add(foo);
    objects.add(bar);
    objects.add(baz);
    JSONArray result = JsonResultBuilders.buildJsonList(objects);
    assertEquals(result.get(0), foo);
    assertEquals(result.get(1), bar);
    assertEquals(result.get(2), baz);
  }

  public void testBuildJsonIntent() throws JSONException {
    Intent intent = new Intent();
    intent.putExtra("foo", "value");
    Intent nestedIntent = new Intent();
    nestedIntent.putExtra("baz", 123);
    intent.putExtra("bar", nestedIntent);
    JSONObject result = JsonResultBuilders.buildJsonIntent(intent);
    assertEquals(result.get("foo"), "value");
    Object nestedJson = result.get("bar");
    assertEquals(((JSONObject) nestedJson).getInt("baz"), 123);
  }
}
