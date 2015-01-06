package com.gmail.notrupertthorne.whatsthatcolor;

/*
Copyright 2014 Niklas Thörne

This file is part of What's That Color.

What's That Color is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

What's That Color is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with What's That Color.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * This type implements the application's main view.
 */
public class MainActivity extends ActionBarActivity
{
  /**
   * This type is responsible for receiving a _Message_
   * from the camera preview, containing the color code
   * of the selected pixel.
   */
  private static class HandlerClass extends Handler
  {
    private final WeakReference<MainActivity> m_weakMain;

    public HandlerClass(MainActivity context)
    {
      // Store a weak reference to the main activity, in
      // order to be able to fetch widgets.
      m_weakMain = new WeakReference<MainActivity>(context);
    }

    /**
     * Updates the widgets once a Message has been received.
     */
    @Override
    public void handleMessage(Message msg)
    {
      final MainActivity l_main = m_weakMain.get();
      if (null != l_main)
      {
        final String colorString = msg.getData().getString(
            CameraPreview.COLORKEY);
        l_main.updateSampledColor(colorString);
      }
    }
  }

  private int REQUEST_CODE = 42;

  private CrosshairView m_crosshair;

  private HandlerClass m_handler;

  private CameraPreview m_preview;

  private String m_colorString;

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    final int id = item.getItemId();
    switch (id)
    {
      case R.id.action_about:
        launchAboutActivity();
        return true;
      case R.id.action_license:
        launchLicenseActivity();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    m_preview.relaseCamera();
  }

  @Override
  public void onResume()
  {
    int l_result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (ConnectionResult.SUCCESS != l_result)
    {
      Dialog l_dialog = GooglePlayServicesUtil.getErrorDialog(l_result, this, REQUEST_CODE);
      if (null != l_dialog)
      {
        l_dialog.show();
      }
    }
    super.onResume();
    m_preview.openCamera();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    m_handler = new HandlerClass(this);

    // Look up the AdView as a resource and load a request.
    AdView adView = (AdView) this.findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder()
    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)       // Emulator
    .addTestDevice("A5B23121C2B1DA201B438621179B3FD8") // .. to get from logcat.
    .build();
    adView.loadAd(adRequest);


    // Create the preview view, and set it as content of this Activity.
    m_preview = new CameraPreview(this, m_handler);
    final FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(m_preview);

    // Create the crosshair view, and set it as content of this Activity.
    m_crosshair = new CrosshairView(this);
    final FrameLayout crosshair = (FrameLayout) findViewById(R.id.crosshair_view);
    crosshair.addView(m_crosshair);

    if (null != savedInstanceState && 
        savedInstanceState.containsKey(CameraPreview.COLORKEY))
    {
      updateSampledColor(savedInstanceState.getString(CameraPreview.COLORKEY));
    }
  }

  void launchAboutActivity()
  {
    final Intent intent = new Intent(this, AboutActivity.class);
    startActivity(intent);
  }

  void launchLicenseActivity()
  {
    final Intent intent = new Intent(this, LicenseActivity.class);
    startActivity(intent);
  }

  void updateSampledColor(String colorString)
  {
    final TextView textView = (TextView) findViewById(R.id.color_textbox);
    textView.setText(colorString);
    final FrameLayout sampleFrame = (FrameLayout) findViewById(R.id.color_sample);
    sampleFrame.setBackgroundColor(Color.parseColor(colorString));
    m_colorString = colorString;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState)
  {
    outState.putString(CameraPreview.COLORKEY, m_colorString);
  }

  public void onColorTextViewClick(View v)
  {
    final ClipboardManager l_clipboard = 
        (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
    final ClipData l_clip = 
        ClipData.newPlainText("Color code", m_colorString);
    l_clipboard.setPrimaryClip(l_clip);

    final Context l_context = getApplicationContext();
    final int l_duration = Toast.LENGTH_LONG;
    final CharSequence l_text = "Color code copied to clipboard.";

    Toast.makeText(l_context, l_text, l_duration).show();
  }
}
