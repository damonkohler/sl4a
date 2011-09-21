package com.googlecode.android_scripting.facade.ui;

import android.R;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.future.FutureActivityTask;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.xmlpull.v1.XmlPullParser;

public class FullScreenTask extends FutureActivityTask<Object> implements OnClickListener,
    OnItemClickListener {
  private EventFacade mEventFacade;
  private UiFacade mUiFacade;
  public View mView = null;
  protected ViewInflater mInflater = new ViewInflater();
  protected String mLayout;
  protected final CountDownLatch mShowLatch = new CountDownLatch(1);
  protected Handler mHandler = null;

  public FullScreenTask(String layout) {
    super();
    mLayout = layout;
  }

  @Override
  public void onCreate() {
    // super.onCreate();
    if (mHandler == null) {
      mHandler = new Handler();
    }
    mInflater.getErrors().clear();
    try {
      if (mView == null) {
        StringReader sr = new StringReader(mLayout);
        XmlPullParser xml = ViewInflater.getXml(sr);
        mView = mInflater.inflate(getActivity(), xml);
      }
    } catch (Exception e) {
      mInflater.getErrors().add(e.toString());
      mView = defaultView();
      mInflater.setIdList(R.id.class);
    }
    getActivity().setContentView(mView);
    getActivity().setTitle("SL4A Title");
    mInflater.setClickListener(mView, this, this);
    mShowLatch.countDown();
  }

  @Override
  public void onDestroy() {
    mEventFacade.postEvent("screen", "destroy");
    super.onDestroy();
  }

  /** default view in case of errors */
  protected View defaultView() {
    LinearLayout result = new LinearLayout(getActivity());
    result.setOrientation(LinearLayout.VERTICAL);
    TextView text = new TextView(getActivity());
    text.setText("Sample Layout");
    result.addView(text, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    Button b = new Button(getActivity());
    b.setText("OK");
    result.addView(b, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    return result;
  }

  public EventFacade getEventFacade() {
    return mEventFacade;
  }

  public void setEventFacade(EventFacade eventFacade) {
    mEventFacade = eventFacade;
  }

  public void setUiFacade(UiFacade uiFacade) {
    mUiFacade = uiFacade;
  }

  public CountDownLatch getShowLatch() {
    return mShowLatch;
  }

  public Map<String, Map<String, String>> getViewAsMap() {
    return mInflater.getViewAsMap(mView);
  }

  private View getViewByName(String idName) {
    View result = null;
    int id = mInflater.getId(idName);
    if (id != 0) {
      result = mView.findViewById(id);
    }
    return result;
  }

  public Map<String, String> getViewDetail(String idName) {
    Map<String, String> result = new HashMap<String, String>();
    result.put("error", "id not found (" + idName + ")");
    View v = getViewByName(idName);
    if (v != null) {
      result = mInflater.getViewInfo(v);
    }
    return result;
  }

  public String setViewProperty(String idName, String property, String value) {
    View v = getViewByName(idName);
    mInflater.getErrors().clear();
    if (v != null) {
      SetProperty p = new SetProperty(v, property, value);
      mHandler.post(p);
      try {
        p.mLatch.await();
      } catch (InterruptedException e) {
        mInflater.getErrors().add(e.toString());
      }
    } else {
      return "View " + idName + " not found.";
    }
    if (mInflater.getErrors().size() == 0) {
      return "OK";
    }
    return mInflater.getErrors().get(0);
  }

  public String setList(String id, JSONArray items) {
    View v = getViewByName(id);
    mInflater.getErrors().clear();
    if (v != null) {
      SetList p = new SetList(v, items);
      mHandler.post(p);
      try {
        p.mLatch.await();
      } catch (InterruptedException e) {
        mInflater.getErrors().add(e.toString());
      }
    } else {
      return "View " + id + " not found.";
    }
    if (mInflater.getErrors().size() == 0) {
      return "OK";
    }
    return mInflater.getErrors().get(0);
  }

  @Override
  public void onClick(View view) {
    mEventFacade.postEvent("click", mInflater.getViewInfo(view));
  }

  private class SetProperty implements Runnable {
    View mView;
    String mProperty;
    String mValue;
    CountDownLatch mLatch = new CountDownLatch(1);

    SetProperty(View view, String property, String value) {
      mView = view;
      mProperty = property;
      mValue = value;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      mInflater.setProperty(mView, mProperty, mValue);
      mView.invalidate();
      mLatch.countDown();
    }
  }

  private class SetList implements Runnable {
    View mView;
    JSONArray mItems;
    CountDownLatch mLatch = new CountDownLatch(1);

    SetList(View view, JSONArray items) {
      mView = view;
      mItems = items;
      mView.invalidate();
      mLatch.countDown();
    }

    @Override
    public void run() {
      mInflater.setListAdapter(mView, mItems);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    Map<String, String> data = new HashMap<String, String>();
    data.put("key", String.valueOf(keyCode));
    data.put("action", String.valueOf(event.getAction()));
    mEventFacade.postEvent("key", data);
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      return true;
    }
    return false;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return mUiFacade.onPrepareOptionsMenu(menu);
  }

  @Override
  public void onItemClick(AdapterView<?> aview, View aitem, int position, long id) {
    Map<String, String> data = mInflater.getViewInfo(aview);
    data.put("position", String.valueOf(position));
    mEventFacade.postEvent("itemclick", data);
  }

}
