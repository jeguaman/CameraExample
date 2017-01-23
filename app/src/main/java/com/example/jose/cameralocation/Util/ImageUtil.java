package com.example.jose.cameralocation.Util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * Created by Jose on 11/28/2016.
 */

public class ImageUtil {

    public static Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }

    public static Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;

        int width, height = 0;

        //if(c.getWidth() > s.getWidth()) {
        width = c.getWidth();
        height = c.getHeight();
        //} else {
        //width = s.getWidth();
        //height = c.getHeight();
        //}

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);


        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, 0f, null);

        return cs;
    }

    public static void drawTextOnBitmap(Bitmap c, String text, float x, float y, Typeface typeface, float size, int color, Paint.Align align) {
        Canvas comboImage = new Canvas(c);
        comboImage.drawBitmap(c, 0f, 0f, null);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTypeface(typeface);
        paint.setTextAlign(align);
        paint.setShadowLayer(80, 0, 0, Color.BLACK);
        String[] lines = text.split("\n");
        float txtSize = -paint.ascent() + paint.descent();

        if (paint.getStyle() == Paint.Style.FILL_AND_STROKE || paint.getStyle() == Paint.Style.STROKE) {
            txtSize += paint.getStrokeWidth(); //add stroke width to the text size
        }
        float lineSpace = txtSize * 0.2f;  //default line spacing

        for (int i = 0; i < lines.length; ++i) {
            comboImage.drawText(lines[i], x, y + (txtSize + lineSpace) * i, paint);
        }
    }
}
