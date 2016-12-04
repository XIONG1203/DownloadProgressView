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
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

import java.util.Random;

/**
 * Created by xiong on 2016/12/1.
 * o(一︿一+)o
 */

public class DownloadProgressView extends View {

    private String TAG = "DownloadProgressView";
    private Paint downPaint;
    private Paint arcPaint;
    private Paint circlePaint;
    private float strokeWidth;
    private RectF rectF;
    private float radius;
    private float width;
    private float height;

    private float dotRadius;
    private float downLength;

    private ValueAnimator mRotateAnimation;
    private float percent = 0;

    //绘制波浪
    private Paint progressPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Path path = new Path();
    private Paint roundPaint;

    private float space;

    private int currentProgress = 0;
    private int maxProgress = 100;

    private float centerY;

    private float bitmapWidth;
    private float bitmapHeight;
    private float waveHeight;

    private boolean isRise = false;


    //打钩

    private ValueAnimator tickAnimation;
    private float tickPercent;
    private PathMeasure tickMeasure;
    private Path tickPath;

    //打X
    private ValueAnimator forkAnimation;
    private float forkPercent;
    private PathMeasure forkMeasure;
    private PathMeasure forkMeasure2;
    private Path forkPath;
    private Path forkPath2;
    private int forkStatus = 0;
    private Paint forkPaint;


    private int status = 0;

    private Paint solidCirclePaint;

    private int colorArc;
    private int colorsoild;
    private int colorFork;

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

        strokeWidth = width / 18;

        dotRadius = strokeWidth / 2;

        space = width / 12;

        radius = Math.min(width, height) / 2 - strokeWidth / 2;

        rectF = new RectF(strokeWidth / 2, strokeWidth / 2, width - strokeWidth / 2, width - strokeWidth / 2);

        Log.d(TAG, rectF.toString());
        centerY = height - radius - 3 * strokeWidth;
        bitmapWidth = 2 * (radius - strokeWidth / 2);
        bitmapHeight = bitmapWidth;
        bitmap = Bitmap.createBitmap((int) bitmapWidth, (int) bitmapHeight, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);

        //打钩
        tickPath = new Path();
        tickPath.moveTo(width / 2 - 0.5f * radius, height / 2);
        tickPath.lineTo(width / 2 - 0.5f * radius + 0.4f * radius, height / 2 + 0.3f * radius);
        tickPath.lineTo(width / 2 - 0.5f * radius + radius, height / 2 - 0.3f * radius);
        tickMeasure = new PathMeasure(tickPath, false);

        //打叉

        forkPath = new Path();
        forkPath.moveTo(width / 2 - 0.5f * radius, height / 2 - 0.5f * radius);
        forkPath.lineTo(width / 2 + 0.5f * radius, height / 2 + 0.5f * radius);
        forkMeasure = new PathMeasure(forkPath, false);
        forkPath2 = new Path();
        forkPath2.moveTo(width / 2 + 0.5f * radius, height / 2 - 0.5f * radius);
        forkPath2.lineTo(width / 2 - 0.5f * radius, height / 2 + 0.5f * radius);
        forkMeasure2 = new PathMeasure(forkPath2, false);


        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {

        colorArc = Color.argb(255, 0, 150, 136);
        colorsoild = Color.argb(255, 173, 216, 230);
        colorFork = Color.argb(255, 238, 0, 0);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(colorArc);
        arcPaint.setStrokeWidth(strokeWidth);
        arcPaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint(arcPaint);
        circlePaint.setColor(colorsoild);

        downPaint = new Paint();
        downPaint.setAntiAlias(true);
        downPaint.setColor(colorsoild);

        //波浪
        roundPaint = new Paint();
        roundPaint.setAntiAlias(true);
        roundPaint.setColor(Color.argb(255, 255, 255, 255));
        roundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(colorsoild);

        solidCirclePaint = new Paint();
        solidCirclePaint.setColor(colorsoild);

        forkPaint = new Paint();
        forkPaint.setStyle(Paint.Style.STROKE);
        forkPaint.setStrokeWidth(strokeWidth);
        forkPaint.setColor(colorFork);
        forkPaint.setAntiAlias(true);


        //交集上层
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        //圆圈旋转动画
        mRotateAnimation = ValueAnimator.ofFloat(0f, 1f);
        mRotateAnimation.setDuration(2000);
        mRotateAnimation.setRepeatCount(100);
        mRotateAnimation.setStartDelay(0);
        mRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mRotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                percent = (float) animation.getAnimatedValue();
                downLength = (float) animation.getAnimatedValue() * (2f * radius + 2 * dotRadius);

                if (downLength / waveHeight > 1.01) {
                    if (currentProgress < maxProgress) {
                        if (isRise) {
                            currentProgress += 10;
                            isRise = false;
                        }
                    } else {
                        if ((float) animation.getAnimatedValue() > 0.99)
                            mRotateAnimation.cancel();
                    }
                } else {
                    isRise = true;
                }
                invalidate();
            }
        });

        mRotateAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                status = 1;
                tickAnimation.start();
//                forkStatus = 0;
//                forkAnimation.start();
            }
        });

        //打钩动画
        tickAnimation = ValueAnimator.ofFloat(0f, 1f);
        tickAnimation.setStartDelay(200);
        tickAnimation.setDuration(500);
        tickAnimation.setInterpolator(new AccelerateInterpolator());
        tickAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tickPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        forkAnimation = ValueAnimator.ofFloat(0f, 1f);
        forkAnimation.setStartDelay(200);
        forkAnimation.setDuration(500);
        forkAnimation.setInterpolator(new AccelerateInterpolator());
        forkAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                forkPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        forkAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (forkStatus == 0) {
                    forkPercent = 0;
                    forkAnimation.start();
                    forkStatus = 1;
                }
            }
        });


    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (status == 0) {

            arcPaint.setStrokeWidth(strokeWidth);
            circlePaint.setStrokeWidth(strokeWidth);

            Log.d(TAG, getMeasuredWidth() + "");
            canvas.drawArc(rectF, 0, 360, false, circlePaint);
            canvas.drawArc(rectF, -90 - 360 * percent, -(20 + percent * 344), false, arcPaint);

            canvas.drawBitmap(bitmap, width / 2 - radius + strokeWidth / 2, width / 2 - radius + strokeWidth / 2, downPaint);

            bitmapCanvas.drawCircle(bitmapWidth / 2, bitmapHeight / 2, bitmapWidth / 2, roundPaint);
            path.reset();

            //波的数量
            int count = (int) ((int) (bitmapWidth / 2 + 1) * 2 / space);

            //决定上升的高度
            waveHeight = (1 - (float) currentProgress / maxProgress) * bitmapHeight;
            path.moveTo(-bitmapWidth + waveHeight, waveHeight);
            //决定 曲线的弯曲程度
            float d = (1 - (float) currentProgress / maxProgress) * space;
            for (int i = 0; i < count; i++) {
                path.rQuadTo(space, -d, space * 2, 0);
                path.rQuadTo(space, d, space * 2, 0);
            }

//            Log.d(TAG, " - bitmapWidth + y:" + (-bitmapWidth + waveHeight) + "  y: " + waveHeight + "   d: " + d + "   sssssss");
            path.lineTo(bitmapWidth, waveHeight);
            path.lineTo(bitmapWidth, bitmapHeight);
            path.lineTo(0, bitmapHeight);
            path.close();
            bitmapCanvas.drawPath(path, progressPaint);

            //水滴覆盖在最上面

            canvas.drawCircle(width / 2, height - 2 * radius - 2 * strokeWidth + downLength - 2 * dotRadius, dotRadius, downPaint);

        } else if (status == 1) {
            canvas.drawArc(rectF, 0, 360, false, solidCirclePaint);
            canvas.drawArc(rectF, 0, 360, false, arcPaint);
            Path path = new Path();
            tickMeasure.getSegment(0, tickPercent * tickMeasure.getLength(), path, true);
            path.rLineTo(0, 0);
            canvas.drawPath(path, arcPaint);

        } else if (status == 2) {
            canvas.drawArc(rectF, 0, 360, false, solidCirclePaint);
            canvas.drawArc(rectF, 0, 360, false, arcPaint);


            Path path = new Path();
            forkPaint.setStrokeWidth(strokeWidth);
            if (forkStatus == 0) {
                forkMeasure.getSegment(0, forkPercent * forkMeasure.getLength(), path, true);
            } else {
                Log.d("dsafer", "fasfd");
                canvas.drawPath(forkPath, forkPaint);
                forkMeasure2.getSegment(0, forkPercent * forkMeasure2.getLength(), path, true);
            }
            path.rLineTo(0, 0);
            canvas.drawPath(path, forkPaint);
        }

    }

    private void start() {
        post(new Runnable() {
            @Override
            public void run() {
                mRotateAnimation.start();
            }
        });
    }


    public void startLoad() {
        start();
    }

}
