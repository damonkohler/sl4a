package net.londatiga.android;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Popup window, shows action list as icon and text (QuickContact / Twitter app).
 * 
 * @author Lorensius. W. T
 */
public class QuickAction extends CustomPopupWindow {
  private final View mRoot;
  private final ImageView mArrowUp;
  private final ImageView mArrowDown;
  private final Animation mTrackAnim;
  private final LayoutInflater mInflater;
  private final Context mContext;

  public static final int ANIM_GROW_FROM_LEFT = 1;
  public static final int ANIM_GROW_FROM_RIGHT = 2;
  public static final int ANIM_GROW_FROM_CENTER = 3;
  public static final int ANIM_AUTO = 4;

  private int mAnimStyle;
  private boolean mAnimateTrack;
  private ViewGroup mTrack;
  private List<ActionItem> mActionList;

  /**
   * Constructor
   * 
   * @param anchor
   *          {@link View} on where the popup should be displayed
   */
  public QuickAction(View anchor) {
    super(anchor);

    mActionList = new ArrayList<ActionItem>();
    mContext = anchor.getContext();
    mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    mRoot = mInflater.inflate(R.layout.quickaction, null);

    mArrowDown = (ImageView) mRoot.findViewById(R.id.arrow_down);
    mArrowUp = (ImageView) mRoot.findViewById(R.id.arrow_up);

    setContentView(mRoot);

    mTrackAnim = AnimationUtils.loadAnimation(anchor.getContext(), R.anim.rail);

    mTrackAnim.setInterpolator(new Interpolator() {
      public float getInterpolation(float t) {
        // Pushes past the target area, then snaps back into place.
        // Equation for graphing: 1.2-((x*1.6)-1.1)^2
        final float inner = (t * 1.55f) - 1.1f;

        return 1.2f - inner * inner;
      }
    });

    mTrack = (ViewGroup) mRoot.findViewById(R.id.tracks);
    mAnimStyle = ANIM_AUTO;
    mAnimateTrack = true;
  }

  /**
   * Animate track
   * 
   * @param animateTrack
   *          flag to animate track
   */
  public void animateTrack(boolean animateTrack) {
    this.mAnimateTrack = animateTrack;
  }

  /**
   * Set animation style
   * 
   * @param animStyle
   *          animation style, default is set to ANIM_AUTO
   */
  public void setAnimStyle(int animStyle) {
    this.mAnimStyle = animStyle;
  }

  /**
   * Add action item
   * 
   * @param action
   *          {@link ActionItem}
   */
  public void addActionItem(ActionItem action) {
    mActionList.add(action);
  }

  public void addActionItems(ActionItem... actions) {
    mActionList.addAll(Arrays.asList(actions));
  }

  /**
   * Show popup window
   */
  public void show() {
    preShow();

    int[] location = new int[2];

    anchor.getLocationOnScreen(location);

    Rect anchorRect =
        new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1]
            + anchor.getHeight());

    mRoot.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    mRoot.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    int rootWidth = mRoot.getMeasuredWidth();
    int rootHeight = mRoot.getMeasuredHeight();

    int screenWidth = windowManager.getDefaultDisplay().getWidth();
    // int screenHeight = windowManager.getDefaultDisplay().getHeight();

    int xPos = (screenWidth - rootWidth) / 2;
    int yPos = anchorRect.top - rootHeight + anchor.getPaddingTop();

    boolean onTop = true;

    // display on bottom
    if (rootHeight > anchorRect.top) {
      yPos = anchorRect.bottom;
      onTop = false;
    }

    showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX() / 2);

    setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

    createActionList();

    window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

    if (mAnimateTrack) {
      mTrack.startAnimation(mTrackAnim);
    }
  }

  /**
   * Set animation style
   * 
   * @param screenWidth
   *          Screen width
   * @param requestedX
   *          distance from left screen
   * @param onTop
   *          flag to indicate where the popup should be displayed. Set TRUE if displayed on top of
   *          anchor and vice versa
   */
  private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
    int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;

    switch (mAnimStyle) {
    case ANIM_GROW_FROM_LEFT:
      window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
          : R.style.Animations_PopDownMenu_Left);
      break;

    case ANIM_GROW_FROM_RIGHT:
      window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
          : R.style.Animations_PopDownMenu_Right);
      break;

    case ANIM_GROW_FROM_CENTER:
      window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
          : R.style.Animations_PopDownMenu_Center);
        break;

    case ANIM_AUTO:
      if (arrowPos <= screenWidth / 4) {
        window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
            : R.style.Animations_PopDownMenu_Left);
      } else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
        window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
            : R.style.Animations_PopDownMenu_Center);
      } else {
        window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right
            : R.style.Animations_PopDownMenu_Right);
      }

      break;
    }
  }

  /**
   * Create action list
   * 
   */
  private void createActionList() {
    View view;
    String title;
    Drawable icon;
    OnClickListener listener;
    int index = 1;

    for (int i = 0; i < mActionList.size(); i++) {
      title = mActionList.get(i).getTitle();
      icon = mActionList.get(i).getIcon();
      listener = mActionList.get(i).getListener();

      view = getActionItem(title, icon, listener);

      view.setFocusable(true);
      view.setClickable(true);
      mTrack.addView(view, index);

      index++;
    }
  }

  /**
   * Get action item {@link View}
   * 
   * @param title
   *          action item titleintent
   * @param icon
   *          {@link Drawable} action item icon
   * @param listener
   *          {@link View.OnClickListener} action item listener
   * @return action item {@link View}
   */
  private View getActionItem(String title, Drawable icon, OnClickListener listener) {
    LinearLayout container = (LinearLayout) mInflater.inflate(R.layout.action_item, null);
    ImageView img = (ImageView) container.findViewById(R.id.icon);
    TextView text = (TextView) container.findViewById(R.id.title);

    if (icon != null) {
      img.setImageDrawable(icon);
    } else {
      img.setVisibility(View.GONE);
    }

    if (title != null) {
      text.setText(title);
    } else {
      text.setVisibility(View.GONE);
    }

    if (listener != null) {
      container.setOnClickListener(listener);
    }

    return container;
  }

  /**
   * Show arrow
   * 
   * @param whichArrow
   *          arrow type resource id
   * @param requestedX
   *          distance from left screen
   */
  private void showArrow(int whichArrow, int requestedX) {
    final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
    final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

    final int arrowWidth = mArrowUp.getMeasuredWidth();

    showArrow.setVisibility(View.VISIBLE);

    ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();

    param.leftMargin = requestedX - arrowWidth / 2;

    hideArrow.setVisibility(View.INVISIBLE);
  }
}
