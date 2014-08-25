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

import java.io.IOException;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This type is responsible for the camera preview View.
 */
public class CameraPreview extends SurfaceView implements
    SurfaceHolder.Callback
{
  /**
   * This field provides the string for the Bundle key.
   */
  public static String COLORKEY = "color";

  private static String LOG_TAG = "CameraPreview";

  private Camera m_camera;

  /**
   * This field provides the context in which this view has been created.
   */
  private final Context m_context;
  /**
   * This field references the handler to which the RGB is to be sent.
   */
  private final Handler m_handler;
  private final SurfaceHolder m_holder;

  /**
   * This callback is to be used when calling Camera.takePicture.
   * 
   * When a compressed JPEG is available, we'll inspect the target pixel,
   * sending its stringified RGB color to the m_handler.
   */
  private final PictureCallback m_JPEGPicture = new PictureCallback()
  {
    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
      Log.d(LOG_TAG, "Got a picture..");

      if (null != data)
      {
        final Bitmap l_bitmap = BitmapFactory.decodeByteArray(data, 0,
            data.length);
        // Get the target (center) pixel.
        final int l_pixel = l_bitmap.getPixel(l_bitmap.getWidth() / 2,
            l_bitmap.getHeight() / 2);
        // Convert the pixel Color to a hexified String.
        final String l_hex = String.format("#%02x%02x%02x", Color.red(l_pixel),
            Color.green(l_pixel), Color.blue(l_pixel));

        Log.d(LOG_TAG, String.format("Color: %s", l_hex));

        // Send the RGB code to the handler.
        final Message l_msg = m_handler.obtainMessage();
        final Bundle l_bundle = new Bundle();
        l_bundle.putString(COLORKEY, l_hex);
        l_msg.setData(l_bundle);
        m_handler.sendMessage(l_msg);
      }
      camera.startPreview();
    }
  };

  /**
   * This callback is used to add a shutter sound to Camera.takePicture
   */
  private final ShutterCallback mShutter = new ShutterCallback()
  {
    @Override
    public void onShutter()
    {
      // TODO: Are there any error cases to handle here?
      // Play a sound when taking the picture..
      final AudioManager mgr = (AudioManager) m_context
          .getSystemService(Context.AUDIO_SERVICE);
      mgr.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
    }
  };

  /**
   * @param context The context in which this type is instantiated.
   * @param handler The Handler to receive the RGB message.
   */
  public CameraPreview(Context context, Handler handler)
  {
    super(context);

    m_handler = handler;
    m_context = context;

    openCamera();

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    m_holder = getHolder();
    m_holder.addCallback(this);
    // deprecated setting, but required on Android versions prior to 3.0
    m_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    setClickable(true);
  }

  /**
   * Open the hardware camera, instantiating a Camera field.
   */
  public void openCamera()
  {
    if (m_camera == null)
    {
      Camera c = null;
      try
      {
        c = Camera.open(); // attempt to get a Camera instance
      }
      catch (final Exception e)
      {
        // Camera is not available (in use or does not exist)
      }
      m_camera = c;

      if (m_camera != null)
      {
        if (Configuration.ORIENTATION_PORTRAIT ==
            getResources().getConfiguration().orientation)
        {
          // Rotate the camera 90 degrees.
          m_camera.setDisplayOrientation(90);
        }
      }
    }
  }

  /**
   * Take a picture, when the preview has been clicked.
   */
  @Override
  public boolean performClick()
  {
    if (m_camera != null)
    {
      m_camera.takePicture(mShutter, null, null, m_JPEGPicture);
    }
    return super.performClick();
  }

  /**
   * Release the associated Camera, if opened.
   */
  public void relaseCamera()
  {
    if (m_camera != null)
    {
      m_camera.release();
      m_camera = null;
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
    if (m_holder.getSurface() == null)
    {
      // preview surface does not exist
      return;
    }

    // stop preview before making changes
    try
    {
      m_camera.stopPreview();
    }
    catch (final Exception e)
    {
      // ignore: tried to stop a non-existent preview
    }

    // TODO: Update for screen rotation..

    // start preview with new settings
    try
    {
      m_camera.setPreviewDisplay(m_holder);
      m_camera.startPreview();

    }
    catch (final Exception e)
    {
      Log.d(LOG_TAG, "Error starting camera preview: " + e.getMessage());
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder)
  {
    // The Surface has been created, now tell the camera where to draw the
    // preview.
    try
    {
      if (null != m_camera)
      {
        m_camera.setPreviewDisplay(holder);
        m_camera.startPreview();
      }
      else
      {
        Log.d(LOG_TAG,
            "Camera not open upon surface creation. Attempting open.");
        openCamera();
      }
    }
    catch (final IOException e)
    {
      Log.d(LOG_TAG, "Error setting camera preview: " + e.getMessage());
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder)
  {
    // Surface will be destroyed when we return, so stop the preview.
    if (m_camera != null)
    {
      // Call stopPreview() to stop updating the preview surface.
      m_camera.stopPreview();
    }
  }
}
