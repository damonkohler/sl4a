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

package com.googlecode.android_scripting.facade.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;

/**
 * Wrapper class for alert dialog running in separate thread.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class AlertDialogTask extends DialogTask {

  private final String mTitle;
  private final String mMessage;

  private final List<String> mItems;
  private final Set<Integer> mSelectedItems;
  private final Map<String, Object> mResultMap;
  private InputType mInputType;

  private String mPositiveButtonText;
  private String mNegativeButtonText;
  private String mNeutralButtonText;

  private enum InputType {
    MENU, SINGLE_CHOICE, MULTI_CHOICE, PLAIN_TEXT, PASSWORD;
  }

  public AlertDialogTask(String title, String message) {
    mTitle = title;
    mMessage = message;
    mInputType = InputType.MENU;
    mItems = new ArrayList<String>();
    mSelectedItems = new TreeSet<Integer>();
    mResultMap = new HashMap<String, Object>();
  }

  public void setPositiveButtonText(String text) {
    mPositiveButtonText = text;
  }

  public void setNegativeButtonText(String text) {
    mNegativeButtonText = text;
  }

  public void setNeutralButtonText(String text) {
    mNeutralButtonText = text;
  }

  /**
   * Set list items.
   * 
   * @param items
   */
  public void setItems(JSONArray items) {
    mItems.clear();
    for (int i = 0; i < items.length(); i++) {
      try {
        mItems.add(items.getString(i));
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }
    mInputType = InputType.MENU;
  }

  /**
   * Set single choice items.
   * 
   * @param items
   *          a list of items as {@link String}s to display
   * @param selected
   *          the index of the item that is selected by default
   */
  public void setSingleChoiceItems(JSONArray items, int selected) {
    setItems(items);
    mSelectedItems.clear();
    mSelectedItems.add(selected);
    mInputType = InputType.SINGLE_CHOICE;
  }

  /**
   * Set multi choice items.
   * 
   * @param items
   *          a list of items as {@link String}s to display
   * @param selected
   *          a list of indices for items that should be selected by default
   * @throws JSONException
   */
  public void setMultiChoiceItems(JSONArray items, JSONArray selected) throws JSONException {
    setItems(items);
    mSelectedItems.clear();
    if (selected != null) {
      for (int i = 0; i < selected.length(); i++) {
        mSelectedItems.add(selected.getInt(i));
      }
    }
    mInputType = InputType.MULTI_CHOICE;
  }

  /**
   * Returns the list of selected items.
   */
  public Set<Integer> getSelectedItems() {
    return mSelectedItems;
  }

  public void setTextInput() {
    mInputType = InputType.PLAIN_TEXT;
  }

  public void setPasswordInput() {
    mInputType = InputType.PASSWORD;
  }

  @Override
  public void onCreate() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    if (mTitle != null) {
      builder.setTitle(mTitle);
    }
    // Can't display both a message and items. We'll elect to show the items instead.
    if (mMessage != null && mItems == null) {
      builder.setMessage(mMessage);
    }
    switch (mInputType) {
    // Add single choice menu items to dialog.
    case SINGLE_CHOICE:
      builder.setSingleChoiceItems(mItems.toArray(getItemsAsCharSequenceArray()), mSelectedItems
          .iterator().next(), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
          mSelectedItems.clear();
          mSelectedItems.add(item);
        }
      });
      break;
    // Add multiple choice items to the dialog.
    case MULTI_CHOICE:
      boolean[] selectedItems = new boolean[mItems.size()];
      for (int i : mSelectedItems) {
        selectedItems[i] = true;
      }
      builder.setMultiChoiceItems(getItemsAsCharSequenceArray(), selectedItems,
          new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item, boolean isChecked) {
              if (isChecked) {
                mSelectedItems.add(item);
              } else {
                mSelectedItems.remove(item);
              }
            }
          });
      break;
    // Add standard, menu-like, items to dialog.
    case MENU:
      builder.setItems(getItemsAsCharSequenceArray(), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          Map<String, Integer> result = new HashMap<String, Integer>();
          result.put("item", item);
          dismissDialog();
          setResult(result);
        }
      });
      break;
    // Add plain text input.
    case PLAIN_TEXT:
      final EditText plainTextInput = new EditText(getActivity());
      builder.setView(plainTextInput);
      plainTextInput.setOnKeyListener(new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
          mResultMap.put("value", plainTextInput.getText().toString());
          return false;
        }
      });
      break;
    // Add password input.
    case PASSWORD:
      final EditText passwordInput = new EditText(getActivity());
      passwordInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
      passwordInput.setTransformationMethod(new PasswordTransformationMethod());
      builder.setView(passwordInput);
      passwordInput.setOnKeyListener(new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
          mResultMap.put("value", passwordInput.getText().toString());
          return false;
        }
      });
      break;
    default:
      // No input type specified.
    }
    configureButtons(builder, getActivity());
    addOnCancelListener(builder, getActivity());
    mDialog = builder.show();
    mShowLatch.countDown();
  }

  private CharSequence[] getItemsAsCharSequenceArray() {
    return new CharSequence[mItems.size()];
  }

  private Builder addOnCancelListener(final AlertDialog.Builder builder, final Activity activity) {
    return builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        mResultMap.put("canceled", true);
        dismissDialog();
        setResult(mResultMap);
      }
    });
  }

  private void configureButtons(final AlertDialog.Builder builder, final Activity activity) {
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
          mResultMap.put("which", "positive");
          break;
        case DialogInterface.BUTTON_NEGATIVE:
          mResultMap.put("which", "negative");
          break;
        case DialogInterface.BUTTON_NEUTRAL:
          mResultMap.put("which", "neutral");
          break;
        }
        dismissDialog();
        setResult(mResultMap);
      }
    };
    if (mNegativeButtonText != null) {
      builder.setNegativeButton(mNegativeButtonText, buttonListener);
    }
    if (mPositiveButtonText != null) {
      builder.setPositiveButton(mPositiveButtonText, buttonListener);
    }
    if (mNeutralButtonText != null) {
      builder.setNeutralButton(mNeutralButtonText, buttonListener);
    }
  }
}
