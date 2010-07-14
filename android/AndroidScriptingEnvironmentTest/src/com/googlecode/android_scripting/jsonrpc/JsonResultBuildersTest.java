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

package com.googlecode.android_scripting.jsonrpc;

import com.googlecode.android_scripting.jsonrpc.JsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    JSONArray result = (JSONArray) JsonBuilder.build(objects);
    assertEquals(result.get(0), foo);
    assertEquals(result.get(1), bar);
    assertEquals(result.get(2), baz);
  }

  public void testBuildJsonSet() throws JSONException {
    Set<String> objects = new TreeSet<String>();
    objects.add("foo");
    objects.add("bar");
    objects.add("baz");
    JSONArray result = (JSONArray) JsonBuilder.build(objects);
    assertEquals(result.get(0), "bar");
    assertEquals(result.get(1), "baz");
    assertEquals(result.get(2), "foo");
  }

  public void testBuildJsonIntent() throws JSONException {
    Intent intent = new Intent();
    intent.putExtra("foo", "value");
    Intent nestedIntent = new Intent();
    nestedIntent.putExtra("baz", 123);
    intent.putExtra("bar", nestedIntent);
    JSONObject result = (JSONObject) JsonBuilder.build(intent);
    JSONObject extras = (JSONObject) result.get("extras");
    assertEquals(extras.get("foo"), "value");
    JSONObject nestedJson = (JSONObject) extras.get("bar");
    JSONObject nestedExtras = (JSONObject) nestedJson.get("extras");
    assertEquals(nestedExtras.getInt("baz"), 123);
  }
}
