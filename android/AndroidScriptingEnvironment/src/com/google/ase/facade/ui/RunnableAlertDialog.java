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

import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.ase.exception.AseRuntimeException;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureIntent;

/**
 * Wrapper class for alert dialog running in separate thread.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog extends FutureActivityTask implements RunnableDialog {
  private FutureIntent mResult;
  private Activity mActivity;

  private AlertDialog mDialog;
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
          throw new AseRuntimeException(e);
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

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  /**
   * Returns the list of selected items.
   */
  public Set<Integer> getSelectedItems() {
    return mSelectedItems;
  }

  @Override
  public void run(final Activity activity, FutureIntent result) {
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
              Intent intent = new Intent();
              intent.putExtra("item", item);
              mResult.set(intent);
              // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory
              // leak.
              dialog.dismiss();
              activity.finish();
            }
          });
          break;
      }
    }
    configureButtons(builder, activity);
    addOnCancelListener(builder, activity);
    mDialog = builder.show();
  }

  private Builder addOnCancelListener(final AlertDialog.Builder builder, final Activity activity) {
    return builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        intent.putExtra("canceled", true);
        mResult.set(intent);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        dialog.dismiss();
        activity.finish();
      }
    });
  }

  private void configureButtons(final AlertDialog.Builder builder, final Activity activity) {
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        switch (which) {
          case DialogInterface.BUTTON_POSITIVE:
            intent.putExtra("which", "positive");
            break;
          case DialogInterface.BUTTON_NEGATIVE:
            intent.putExtra("which", "negative");
            break;
          case DialogInterface.BUTTON_NEUTRAL:
            intent.putExtra("which", "neutral");
            break;
        }
        mResult.set(intent);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        dialog.dismiss();
        activity.finish();
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

  @Override
  public void dismissDialog() {
    mDialog.dismiss();
    mActivity.finish();
  }
}
