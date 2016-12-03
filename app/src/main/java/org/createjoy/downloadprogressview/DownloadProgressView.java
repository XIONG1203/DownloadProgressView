package org.createjoy.downloadprogressview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by xiong on 2016/12/1.
 * o(一︿一+)o
 */

public class DownloadProgressView extends View {

    private String TAG = "DownloadProgressView";


    private Paint downPaint;
    private Paint arcPaint;
    private Paint circlePaint;
    private int strokeWidth;
    private RectF rectF;
    private int radius;
    private int width;
    private int height;

    private float dotRadius;
    private float downLength;

    private ValueAnimator mRotateAnimation;

    //绘制波浪
    private Paint progressPaint;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    private Path path = new Path();
    private Paint roundPaint;

    private int space = 20;

    private int currentProgress = 0;
    private int maxProgress = 100;

    private int centerY;

    private int bitmapWidth;
    private int bitmapHeight;


    private float waveHeight;

    private boolean isRise = false;

    private float percent = 0;

    private ValueAnimator mTickAnimation;
    private float tickPercent;
    private PathMeasure tickPathMeasure;

    private int status = 0;

    private Paint solidCirclePaint;

    private Bitmap bitmapDrop;

    private RectF rectDrop;


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

        radius = Math.min(width, height) / 2 - strokeWidth / 2;

        Log.d("weroeoo", radius + "");

        rectF = new RectF(strokeWidth / 2, strokeWidth / 2, width - strokeWidth / 2, width - strokeWidth / 2);

        Log.d(TAG, rectF.toString());
        centerY = height - radius - 3 * strokeWidth;
        bitmapWidth = 2 * (radius - strokeWidth / 2);
        bitmapHeight = bitmapWidth;
        bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        Path tickPath = new Path();

        tickPath.moveTo((float) (width / 2 - 0.5 * radius), height / 2);
        tickPath.lineTo((float) (width / 2 - 0.5 * radius) + 0.4f * radius, height / 2 + 0.3f * radius);
        tickPath.lineTo((float) (width / 2 - 0.5 * radius) + radius, height / 2 - 0.3f * radius);

        tickPathMeasure = new PathMeasure(tickPath, false);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {

        Resources r = this.getContext().getResources();
        bitmapDrop = BitmapFactory.decodeResource(r, R.drawable.drop);
        rectDrop = new RectF(0, 0, 38, 68);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);

        arcPaint.setColor(Color.argb(255, 0, 150, 136));

        arcPaint.setStrokeWidth(strokeWidth);
        Log.d("dsfwerrr", strokeWidth + "    " + radius);
        arcPaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint(arcPaint);
        circlePaint.setColor(Color.argb(255, 173, 216, 230));

        downPaint = new Paint();
        downPaint.setColor(Color.argb(255, 173, 216, 230));


        //波浪

        roundPaint = new Paint();
        roundPaint.setColor(Color.argb(255, 255, 255, 255));
        roundPaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(Color.argb(255, 173, 216, 230));

        //交集上层
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));


        //圆圈旋转动画
        mRotateAnimation = ValueAnimator.ofFloat(0f, 1f);
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setRepeatCount(100);
        mRotateAnimation.setStartDelay(0);
        mRotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        mRotateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                percent = (float) animation.getAnimatedValue();
                downLength = (float) animation.getAnimatedValue() * (2f * radius + 2 * dotRadius);
                Log.d(TAG, "sfsafasf    " + downLength);

                if (downLength / waveHeight > 0.98) {
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
                mTickAnimation.start();
            }
        });

        //打钩动画
        mTickAnimation = ValueAnimator.ofFloat(0f, 1f);
        mTickAnimation.setStartDelay(500);
        mTickAnimation.setDuration(500);
        mTickAnimation.setInterpolator(new AccelerateInterpolator());
        mTickAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tickPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });


        solidCirclePaint = new Paint();
        solidCirclePaint.setColor(Color.argb(255, 173, 216, 230));

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (status == 0) {

            arcPaint.setStrokeWidth(strokeWidth);
            circlePaint.setStrokeWidth(strokeWidth);

            Log.d(TAG, getMeasuredWidth() + "");
            canvas.drawArc(rectF, 0, 360, false, circlePaint);
            canvas.drawArc(rectF, -90 - 360 * percent, -(20 + percent * 344), false, arcPaint);

            Log.d(TAG, centerY + "centerY");

            canvas.drawBitmap(bitmap, width / 2 - radius + strokeWidth / 2, width / 2 - radius + strokeWidth / 2, downPaint);

            int smallRadius = bitmapWidth / 2;

            bitmapCanvas.drawCircle(bitmapWidth / 2, bitmapHeight / 2, bitmapWidth / 2, roundPaint);

            path.reset();
            int count = (int) (smallRadius + 1) * 2 / space;

            //决定上升的高度
            waveHeight = (1 - (float) currentProgress / maxProgress) * smallRadius * 2 + bitmapHeight / 2 - smallRadius;
            path.moveTo(-bitmapWidth + waveHeight, waveHeight);

            Log.d("asdfasfsa", waveHeight + "     " + bitmapHeight);

            //决定 曲线的弯曲程度
            float d = (1 - (float) currentProgress / maxProgress) * space;
            for (int i = 0; i < count; i++) {
                path.rQuadTo(space, -d, space * 2, 0);
                path.rQuadTo(space, d, space * 2, 0);
            }

            Log.d(TAG, " - bitmapWidth + y:" + (-bitmapWidth + waveHeight) + "  y: " + waveHeight + "   d: " + d + "   sssssss");
            path.lineTo(bitmapWidth, waveHeight);
            path.lineTo(bitmapWidth, bitmapHeight);
            path.lineTo(0, bitmapHeight);
            path.close();
            bitmapCanvas.drawPath(path, progressPaint);

            //水滴覆盖在最上面

            canvas.drawCircle(width / 2, height - 2 * radius - 2 * strokeWidth + downLength - 2 * dotRadius, dotRadius, downPaint);

            //    canvas.drawBitmap(bitmapDrop,null,rectDrop,circlePaint);

        } else if (status == 1) {
            canvas.drawArc(rectF, 0, 360, false, solidCirclePaint);
            canvas.drawArc(rectF, 0, 360, false, arcPaint);
            drawTick(canvas);
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

    /**
     * 绘制打钩
     *
     * @param canvas
     */
    private void drawTick(Canvas canvas) {
        Path path = new Path();

        tickPathMeasure.getSegment(0, tickPercent * tickPathMeasure.getLength(), path, true);
        Log.d("sdfasf", tickPathMeasure.getLength() + "");
        path.rLineTo(0, 0);
        canvas.drawPath(path, arcPaint);
        //canvas.drawArc(mRectF, 0, 360, false, tickPaint);
    }

    public void startLoad() {
        start();
    }

}
