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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import com.google.ase.activity.AseServiceHelper;
import com.google.ase.future.FutureResult;

import com.googlecode.android_scripting.exception.Sl4aRuntimeException;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Wrapper class for alert dialog running in separate thread.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog extends RunnableDialog {

  private final String mTitle;
  private final String mMessage;

  private CharSequence[] mItems;
  private final Set<Integer> mSelectedItems;
  private ListType mListType;

  private String mPositiveButtonText;
  private String mNegativeButtonText;
  private String mNeutralButtonText;

  private enum ListType {
    MENU, SINGLE_CHOICE, MULTI_CHOICE;
  }

  public RunnableAlertDialog(String title, String message) {
    mTitle = title;
    mMessage = message;
    mListType = ListType.MENU;
    mSelectedItems = new TreeSet<Integer>();
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
    if (mItems == null) {
      mItems = new CharSequence[items.length()];
      for (int i = 0; i < items.length(); i++) {
        try {
          mItems[i] = items.getString(i);
        } catch (JSONException e) {
          throw new Sl4aRuntimeException(e);
        }
      }
      mListType = ListType.MENU;
    }
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
    if (mItems == null) {
      setItems(items);
      mSelectedItems.clear();
      mSelectedItems.add(selected);
      mListType = ListType.SINGLE_CHOICE;
    }
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
    if (mItems == null) {
      setItems(items);
      mSelectedItems.clear();
      if (selected != null) {
        for (int i = 0; i < selected.length(); i++) {
          mSelectedItems.add(selected.getInt(i));
        }
      }
      mListType = ListType.MULTI_CHOICE;
    }
  }

  /**
   * Returns the list of selected items.
   */
  public Set<Integer> getSelectedItems() {
    return mSelectedItems;
  }

  @Override
  public void run(final AseServiceHelper activity, FutureResult<Object> result) {
    mActivity = activity;
    mResult = result;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    if (mTitle != null) {
      builder.setTitle(mTitle);
    }
    // Can't display both a message and items. We'll elect to show the items instead.
    if (mMessage != null && mItems == null) {
      builder.setMessage(mMessage);
    }
    if (mItems != null) {
      switch (mListType) {
      // Add single choice menu items to dialog.
      case SINGLE_CHOICE:
        builder.setSingleChoiceItems(mItems, mSelectedItems.iterator().next(),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int item) {
                mSelectedItems.clear();
                mSelectedItems.add(item);
              }
            });
        break;
      // Add multiple choice items to the dialog.
      case MULTI_CHOICE:
        boolean[] selectedItems = new boolean[mItems.length];
        for (int i : mSelectedItems) {
          selectedItems[i] = true;
        }
        builder.setMultiChoiceItems(mItems, selectedItems,
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
      default:
        builder.setItems(mItems, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int item) {
            Map<String, Integer> result = new HashMap<String, Integer>();
            result.put("item", item);
            mResult.set(result);
            // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory
            // leak.
            dialog.dismiss();
            activity.taskDone(getTaskId());
          }
        });
        break;
      }
    }
    configureButtons(builder, activity);
    addOnCancelListener(builder, activity);
    mDialog = builder.show();
    mShowLatch.countDown();
  }

  private Builder addOnCancelListener(final AlertDialog.Builder builder,
      final AseServiceHelper activity) {
    return builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        result.put("canceled", true);
        mResult.set(result);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        dialog.dismiss();
        activity.taskDone(getTaskId());
      }
    });
  }

  private void configureButtons(final AlertDialog.Builder builder, final AseServiceHelper activity) {
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Map<String, String> result = new HashMap<String, String>();
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
          result.put("which", "positive");
          break;
        case DialogInterface.BUTTON_NEGATIVE:
          result.put("which", "negative");
          break;
        case DialogInterface.BUTTON_NEUTRAL:
          result.put("which", "neutral");
          break;
        }
        mResult.set(result);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        dialog.dismiss();
        activity.taskDone(getTaskId());
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
