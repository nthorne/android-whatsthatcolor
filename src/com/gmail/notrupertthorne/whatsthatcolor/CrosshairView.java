package com.gmail.notrupertthorne.whatsthatcolor;

/*
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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.View;

/**
 * This type is responsible for the drawing of the crosshair
 * that is shown on top of the preview.
 */
public class CrosshairView extends View
{ // implements SurfaceHolder.Callback {
  // private SurfaceHolder mHolder;
  private final String LOG_TAG = "CrosshairView";
  private final Paint m_paint = new Paint(Paint.ANTI_ALIAS_FLAG);

  public CrosshairView(Context context)
  {
    super(context);

    m_paint.setColor(Color.BLUE);
    m_paint.setStyle(Style.FILL);

    // Signal that we'll do some drawing on the view.
    setWillNotDraw(false);
  }

  /**
   * This method will draw the crosshair upon the _canvas_.
   * 
   * @param canvas The canvas upon which the crosshair shall be drawn.
   */
  private void drawCrosshair(Canvas canvas)
  {
    if (canvas == null)
    {
      Log.e(LOG_TAG, "Cannot draw onto the canvas as it's null");
    }
    else
    {
      // Draw the crosshair
      final int l_width = canvas.getWidth();
      final int l_height = canvas.getHeight();

      canvas.drawLine((l_width / 2) - 2, 0, (l_width / 2) + 2, l_height,
          m_paint);
      canvas.drawLine(0, (l_height / 2) - 2, l_width, (l_height / 2) + 2,
          m_paint);
    }
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    drawCrosshair(canvas);
  }
}
