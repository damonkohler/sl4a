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

package com.google.ase.facade.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureIntent;

/**
 * Wrapper class for alert dialog running in separate thread.
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog extends FutureActivityTask implements RunnableDialog {
  private AlertDialog mDialog;
  private final String mTitle;
  private final String mMessage;
  private FutureIntent mResult;
  private final String[] mButtonTexts;
  private Activity mActivity;
  private JSONArray mItems;
  private ArrayList<String> mListData;
  private ArrayAdapter<String> mListAdapter;
  private ListView mList;

  public RunnableAlertDialog(String title, String message) {
    mTitle = title;
    mMessage = message;
    mButtonTexts = new String[3];
  }

  /**
   * Set button text.
   *
   * @param buttonNumber
   *          button number
   * @param text
   *          button text
   */
  public void setButton(int buttonNumber, String text) {
    mButtonTexts[buttonNumber] = text;
  }
  
  /**
   * Set list items
   * 
   * @param Items
   */
  public void setItems(JSONArray items) {
    if (mActivity == null) {
      // store items localy
      mItems = items;
    } else {
      mListData.clear();
      for (int i=0; i<items.length(); i++)
        try {
          mListData.add((String) items.get(i));
        } catch (JSONException e) {}
      mList.setAdapter(mListAdapter);
    }
  }

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  @Override
  public void run(final Activity activity, FutureIntent result) {
    mActivity = activity;
    mResult = result;
    mDialog = new AlertDialog.Builder(activity).create();
    if (mTitle.length() > 0) {
      mDialog.setTitle(mTitle);
    }
    if (mMessage.length() > 0) {
      mDialog.setMessage(mMessage);
    }
    if (mItems != null) { 
      createListView();
    }
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        intent.putExtra("which", which);
        mResult.set(intent);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        mDialog.dismiss();
        activity.finish();
      }
    };
    if (mButtonTexts[0] != null) {
      mDialog.setButton(mButtonTexts[0], buttonListener);
    }
    if (mButtonTexts[1] != null) {
      mDialog.setButton2(mButtonTexts[1], buttonListener);
    }
    if (mButtonTexts[2] != null) {
      mDialog.setButton3(mButtonTexts[2], buttonListener);
    }
    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        intent.putExtra("canceled", true);
        mResult.set(intent);
        activity.finish();
      }
    });
    mDialog.show();
  }

  /**
   * Create {@link ListView} to contain items
   */
  private void createListView() {
    mListData = new ArrayList<String>();
    mListAdapter = new ArrayAdapter<String>(
                                          mActivity, 
                                          android.R.layout.simple_list_item_1, 
                                          mListData
                                        );
    mList = new ListView(mActivity);
    mList.setAdapter(mListAdapter);
    //TODO(meaneye.rcf): Add onClick event
    mDialog.setView(mList);
    setItems(mItems);   
  }
  
  @Override
  public void dismissDialog() {
    mDialog.dismiss();
    mActivity.finish();
  }
}
