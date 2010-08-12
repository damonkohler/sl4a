package org.connectbot.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

import com.googlecode.android_scripting.R;

import org.connectbot.util.UberColorPickerDialog.OnColorChangedListener;

/**
 * @author modified by raaar
 */
public class ColorsActivity extends Activity implements OnItemClickListener, OnColorChangedListener {

  private SharedPreferences mPreferences;

  private static int sLayoutLanscapeWidth = 400;
  private static int sLayoutPortraitWidth = 210;

  private GridView mColorGrid;
  private LinearLayout mLayout;

  private int mFgColor;
  private int mBgColor;

  private int mCurrentColor = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    mFgColor =
        mPreferences.getInt(PreferenceConstants.COLOR_FG, PreferenceConstants.DEFAULT_FG_COLOR);
    mBgColor =
        mPreferences.getInt(PreferenceConstants.COLOR_BG, PreferenceConstants.DEFAULT_BG_COLOR);

    setContentView(R.layout.act_colors);

    this.setTitle("Terminal Colors");

    mLayout = (LinearLayout) findViewById(R.id.color_layout);

    mColorGrid = (GridView) findViewById(R.id.color_grid);
    mColorGrid.setOnItemClickListener(this);
    mColorGrid.setSelection(0);

    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      mColorGrid.setNumColumns(2);
      LayoutParams params = mLayout.getLayoutParams();
      params.height = params.width;
      params.width = LayoutParams.WRAP_CONTENT;
    }
    mColorGrid.setAdapter(new ColorsAdapter(true));

  }

  private class ColorsAdapter extends BaseAdapter {
    private boolean mSquareViews;

    public ColorsAdapter(boolean squareViews) {
      mSquareViews = squareViews;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ColorView c;
      if (convertView == null) {
        c = new ColorView(ColorsActivity.this, mSquareViews);
      } else {
        c = (ColorView) convertView;
      }
      if (position == 0) {
        c.setColor(mFgColor);
        c.setTitle("Foreground color");
      } else {
        c.setColor(mBgColor);
        c.setTitle("Background color");
      }
      return c;
    }

    public int getCount() {
      return 2;
    }

    public Object getItem(int position) {
      return (position == 0) ? mFgColor : mBgColor;
    }

    public long getItemId(int position) {
      return position;
    }
  }

  private class ColorView extends View {
    private boolean mSquare;

    private Paint mTextPaint;
    private Paint mShadowPaint;
    // Things we paint
    private int mBackgroundColor;
    private String mText;

    private int mAscent;
    private int mWidthCenter;
    private int mHeightCenter;

    public ColorView(Context context, boolean square) {
      super(context);

      mSquare = square;

      mTextPaint = new Paint();
      mTextPaint.setAntiAlias(true);
      mTextPaint.setTextSize(16);
      mTextPaint.setColor(0xFFFFFFFF);
      mTextPaint.setTextAlign(Paint.Align.CENTER);

      mShadowPaint = new Paint(mTextPaint);
      mShadowPaint.setStyle(Paint.Style.STROKE);
      mShadowPaint.setStrokeCap(Paint.Cap.ROUND);
      mShadowPaint.setStrokeJoin(Paint.Join.ROUND);
      mShadowPaint.setStrokeWidth(4f);
      mShadowPaint.setColor(0xFF000000);

      setPadding(20, 20, 20, 20);
    }

    public void setColor(int color) {
      mBackgroundColor = color;
    }

    public void setTitle(String title) {
      mText = title;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      int width = measureWidth(widthMeasureSpec);

      int height;
      if (mSquare) {
        height = width;
      } else {
        height = measureHeight(heightMeasureSpec);
      }

      mAscent = (int) mTextPaint.ascent();
      mWidthCenter = width / 2;
      mHeightCenter = height / 2 - mAscent / 2;

      setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
      int result = 0;
      int specMode = MeasureSpec.getMode(measureSpec);
      int specSize = MeasureSpec.getSize(measureSpec);

      if (specMode == MeasureSpec.EXACTLY) {
        // We were told how big to be
        result = specSize;
      } else {
        // Measure the text
        result = (int) mTextPaint.measureText(mText) + getPaddingLeft() + getPaddingRight();
        if (specMode == MeasureSpec.AT_MOST) {
          // Respect AT_MOST value if that was what is called for by
          // measureSpec
          result = Math.min(result, specSize);
        }
      }

      return result;
    }

    private int measureHeight(int measureSpec) {
      int result = 0;
      int specMode = MeasureSpec.getMode(measureSpec);
      int specSize = MeasureSpec.getSize(measureSpec);

      mAscent = (int) mTextPaint.ascent();
      if (specMode == MeasureSpec.EXACTLY) {
        // We were told how big to be
        result = specSize;
      } else {
        // Measure the text (beware: ascent is a negative number)
        result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop() + getPaddingBottom();
        if (specMode == MeasureSpec.AT_MOST) {
          // Respect AT_MOST value if that was what is called for by
          // measureSpec
          result = Math.min(result, specSize);
        }
      }
      return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.drawColor(mBackgroundColor);
      canvas.drawText(mText, mWidthCenter, mHeightCenter, mShadowPaint);
      canvas.drawText(mText, mWidthCenter, mHeightCenter, mTextPaint);
    }
  }

  private void editColor(int colorNumber) {
    mCurrentColor = colorNumber;
    new UberColorPickerDialog(this, this, (colorNumber == 0) ? mFgColor : mBgColor).show();
  }

  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    editColor(position);
  }

  public void onNothingSelected(AdapterView<?> arg0) {
  }

  public void colorChanged(int value) {
    SharedPreferences.Editor editor = mPreferences.edit();
    if (mCurrentColor == 0) {
      mFgColor = value;
      editor.putInt(PreferenceConstants.COLOR_FG, mFgColor);
    } else {
      mBgColor = value;
      editor.putInt(PreferenceConstants.COLOR_BG, mBgColor);
    }
    editor.commit();
    mColorGrid.invalidateViews();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      mColorGrid.setNumColumns(2);
      LayoutParams params = mLayout.getLayoutParams();
      params.height = params.width;
      params.width = sLayoutLanscapeWidth;
    } else {
      mColorGrid.setNumColumns(1);
      LayoutParams params = mLayout.getLayoutParams();
      params.height = LayoutParams.WRAP_CONTENT;
      params.width = sLayoutPortraitWidth;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuItem reset = menu.add("Reset");
    reset.setAlphabeticShortcut('r');
    reset.setNumericShortcut('1');
    reset.setIcon(android.R.drawable.ic_menu_revert);
    reset.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(MenuItem arg0) {
        mFgColor = PreferenceConstants.DEFAULT_FG_COLOR;
        mBgColor = PreferenceConstants.DEFAULT_BG_COLOR;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PreferenceConstants.COLOR_FG, mFgColor);
        editor.putInt(PreferenceConstants.COLOR_BG, mBgColor);
        editor.commit();
        mColorGrid.invalidateViews();
        return true;
      }
    });
    return true;
  }
}
