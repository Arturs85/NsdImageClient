package com.example.user.nsdimageclient;

/**
 * Created by user on 2017.01.08..
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by user on 2017.01.04..
 */


/**
 * Created by user on 2016.05.31..
 */
public class AttelaSkats extends View {
    public byte[] atelaDati;
    //   ArrayList<HorizontalaLinija> linijas;
    Paint paint = new Paint();
    Canvas canvas = new Canvas();
    Bitmap bitmap;
    boolean uzzimetsPirmais = true;
int velamaisAugstums;
    float merogs=1f;
    public AttelaSkats(Context context,int velamaisAugstums) {
        super(context);
        this.velamaisAugstums = velamaisAugstums;
        paint.setAntiAlias(true);
        paint.setColor(Color.MAGENTA);
paint.setTextSize(10);

    }
    public void mSetBitmap(Bitmap bitmap){
       // if(uzzimetsPirmais)
            this.bitmap = bitmap;

        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Rect rect = new Rect(0,0,479,400);
       // synchronized (bitmap) {
if(bitmap!=null){
     merogs = (float)(velamaisAugstums) / (float)(bitmap.getHeight());
    Log.d("onDraw-----", Float.toString(merogs)+"--velamais--"+velamaisAugstums+" bitm "+bitmap.getHeight());

}
        canvas.scale(merogs, merogs);
       // canvas.rotate(90f);


            if (!(bitmap==null)){


                canvas.drawBitmap(bitmap, 0, 0, paint);
           // bitmap.recycle();
                bitmap = null;
            }
else

                canvas.drawText("Nav attēla",55,55,paint);

            super.onDraw(canvas);
        }




    }
//}
// invalidate();





