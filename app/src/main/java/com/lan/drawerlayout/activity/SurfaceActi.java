package com.lan.drawerlayout.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2015/10/01.
 */
public class SurfaceActi extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(new MyView(this));


    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.w("Sur", "down");
                break;
            case MotionEvent.ACTION_UP:
                Log.w("Sur", "up");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.w("Sur", "move");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.w("Sur", "cancel");
                break;


        }

        return super.onTouchEvent(event);
    }

    class MyView extends SurfaceView implements SurfaceHolder.Callback {

        SurfaceHolder holder;

        MyThread myThread;

        public MyView(Context context) {
            super(context);
            holder = this.getHolder();
            holder.addCallback(this);
            myThread = new MyThread(holder);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            myThread.isRun = true;
            myThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            myThread.isRun = false;

        }


    }


    class MyThread extends Thread {

        SurfaceHolder holder;
        boolean isRun;

        public MyThread(SurfaceHolder holder) {
            this.holder = holder;
            this.isRun = true;
        }

        @Override
        public void run() {

         /*   Canvas canvas=holder.lockCanvas();
            Paint mPaint=new Paint();
            mPaint.setColor(Color.BLUE);
            canvas.drawText("nima haha ",500,250,mPaint);
//                canvas.drawRect(new Rect(200,200,200,200), mPaint);
            holder.unlockCanvasAndPost(canvas);*/

            int count = 0;

            while (isRun) {
                Canvas canvas = null;

                synchronized (holder) {
                    canvas = holder.lockCanvas();
                    canvas.drawColor(Color.BLUE);
                    Paint mPaint = new Paint();
                    mPaint.setColor(Color.GRAY);
                    Rect r = new Rect(100, 50, 300, 250);
                    canvas.drawRect(r, mPaint);
                    canvas.drawText("?????" + (count++) + "??", 100, 310, mPaint);
                    try {
                        Thread.sleep(1000);//???????1??
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (canvas != null)
                            holder.unlockCanvasAndPost(canvas);
                    }

                }
            }

        }
    }


}
