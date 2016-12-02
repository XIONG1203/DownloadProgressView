package org.createjoy.downloadprogressview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by xiong on 2016/12/1.
 * o(一︿一+)o
 */

public class DownloadProgressView extends View {

    private String TAG = "DownloadProgressView";


    private Paint downPaint;
    private Paint arcPaint;
    private Paint circlePaint;
    private int strokeWidth = 20;
    private RectF rectF;
    private int radius;
    private int width;
    private int height;
    private float circleAngle;
    private float dotRadius;
    private float downLength;

    private ValueAnimator mRotateAnimation;
    private ValueAnimator downAnimation;

    //绘制波浪
    private Paint progressPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Path path = new Path();
    private Paint roundPaint;

    private int space = 30;
    private int move = 0;
    private int currentProgress = 0;
    private int maxProgress = 100;

    private int centerY;

    private SingleTapThread singleTapThread;


    public DownloadProgressView(Context context) {
        this(context, null);
    }

    public DownloadProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        dotRadius = 10;
        radius = Math.min(width, height) / 3 - 2 * strokeWidth;
        rectF = new RectF(width / 2 - radius - strokeWidth, height - 2 * (radius + 2 * strokeWidth), width / 2 + radius + strokeWidth, height - 2 * strokeWidth);
        Log.d(TAG, rectF.toString());
        centerY = height - radius - 3 * strokeWidth;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        bitmapCanvas = new Canvas(bitmap);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.argb(255, 48, 63, 159));
        arcPaint.setStrokeWidth(strokeWidth);
        arcPaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint(arcPaint);
        circlePaint.setColor(Color.argb(255, 234, 43, 43));

        downPaint = new Paint();
        downPaint.setColor(Color.argb(255, 180, 255, 47));
        downPaint.setColor(Color.argb(255, 48, 63, 159));


        //波浪

        roundPaint = new Paint();
        roundPaint.setColor(Color.argb(255, 127, 255, 156));
        roundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(Color.argb(255, 48, 63, 159));
        //取两层绘制交集。显示上层
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        //圆圈旋转动画
        mRotateAnimation = ValueAnimator.ofFloat(0f, 1f);
        mRotateAnimation.setDuration(5000);
        mRotateAnimation.setRepeatCount(20);
        mRotateAnimation.setStartDelay(0);
        mRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mRotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                circleAngle = (float) animation.getAnimatedValue() * 360f;
                downLength = (float) animation.getAnimatedValue() * (2f * radius + 2 * dotRadius + strokeWidth);
                invalidate();

            }
        });


        if (singleTapThread == null) {
            singleTapThread = new SingleTapThread();
            postDelayed(singleTapThread, 100);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, getMeasuredWidth() + "");
        canvas.drawArc(rectF, 0, 360, false, circlePaint);
        canvas.drawArc(rectF, -100 - circleAngle, 20, false, arcPaint);

        canvas.drawCircle(width / 2, height - 2 * radius - 3 * strokeWidth + downLength - 2 * dotRadius, dotRadius, downPaint);


        Log.d(TAG, centerY + "");

        canvas.drawBitmap(bitmap, 0, centerY - radius, downPaint);

        bitmapCanvas.drawCircle(width / 2, radius, radius+strokeWidth/2, roundPaint);

        path.reset();
        int count = (int) (radius + 1) * 2 / space;
        float y = (1 - (float) currentProgress / maxProgress) * radius * 2 + height / 2 - radius;
        move += 20;
        if (move > width) {
            move = width;
        }
        path.moveTo(-width + y, y);
        float d = (1 - (float) currentProgress / maxProgress) * space;
        for (int i = 0; i < count; i++) {
            path.rQuadTo(space, -d, space * 2, 0);
            path.rQuadTo(space, d, space * 2, 0);
        }
        path.lineTo(width, y);
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
        bitmapCanvas.drawPath(path, progressPaint);

    }

    private void start() {
        post(new Runnable() {
            @Override
            public void run() {
                mRotateAnimation.start();
            }
        });
    }

    private void drawDrop() {

    }

    public void startLoad() {
        start();
    }

    private class SingleTapThread implements Runnable {
        @Override
        public void run() {
            if (currentProgress < maxProgress) {
                invalidate();
                postDelayed(singleTapThread, 100);
                currentProgress++;
            } else {
                removeCallbacks(singleTapThread);
            }
        }
    }
}
