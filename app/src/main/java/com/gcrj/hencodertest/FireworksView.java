package com.gcrj.hencodertest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by zhangxin on 2017/10/16.
 */

//写的头痛，注释稍后写
public class FireworksView extends ViewGroup {

    private Paint bigCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint smallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Shader bigLightShader;
    private Shader lightShader;
    private Shader darkShader;

    private Path fireworksPath = new Path();

    private int radius;
    private int angle = 180;
    private int bigAngle;

    private ValueAnimator bigRotateAnimator;
    private ValueAnimator rotateAnimator;

    public FireworksView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        bigCirclePaint.setStyle(Paint.Style.STROKE);

        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(5);

        smallCirclePaint.setStyle(Paint.Style.STROKE);
        int smallCircleRadius = 10;
        Path dashPath = new Path();
        dashPath.addCircle(smallCircleRadius, smallCircleRadius, smallCircleRadius, Path.Direction.CCW);
        PathEffect pathDashEffect = new PathDashPathEffect(dashPath, smallCircleRadius * 5, 0, PathDashPathEffect.Style.TRANSLATE);
        PathEffect discreteEffect = new DiscretePathEffect(smallCircleRadius * 2, smallCircleRadius * 2);
        PathEffect pathEffect = new ComposePathEffect(pathDashEffect, discreteEffect);
        smallCirclePaint.setPathEffect(pathEffect);

        bigRotateAnimator = ValueAnimator.ofInt(0, 360);
        bigRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bigAngle = Integer.parseInt(animation.getAnimatedValue().toString());
                invalidate();
            }
        });
        bigRotateAnimator.setDuration(8000);
        bigRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);

        rotateAnimator = ValueAnimator.ofInt(180, 540);
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                angle = Integer.parseInt(animation.getAnimatedValue().toString());
                invalidate();
            }
        });
        rotateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animate().scaleX(1.2F).scaleY(1.2F).translationYBy(-100).setInterpolator(new AccelerateInterpolator()).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animate().setListener(null).scaleX(1).scaleY(1).translationYBy(100).setInterpolator(new OvershootInterpolator()).setDuration(200).start();
                    }
                }).start();
            }
        });
        rotateAnimator.setDuration(2000);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bigRotateAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bigRotateAnimator.cancel();
        rotateAnimator.cancel();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View view = getChildAt(0);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        int diameter = view.getMeasuredWidth() + 140;
        setMeasuredDimension(diameter, diameter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View view = getChildAt(0);
        int childRadius = view.getMeasuredWidth() / 2;
        view.layout(getWidth() / 2 - childRadius, getHeight() / 2 - childRadius, getWidth() / 2 + childRadius, getHeight() / 2 + childRadius);
    }

    public void startAnimation() {
        if (!rotateAnimator.isRunning()) {
            rotateAnimator.start();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = w / 2 - 45;
        bigLightShader = new LinearGradient(getWidth() / 2 - radius, getHeight() / 2, getWidth() / 2 + radius, getHeight() / 2, Color.parseColor("#8AC3EB"), Color.WHITE, Shader.TileMode.CLAMP);
        lightShader = new SweepGradient(getWidth() / 2, getHeight() / 2, Color.parseColor("#3899ED"), Color.WHITE);
        darkShader = new SweepGradient(getWidth() / 2, getHeight() / 2, Color.parseColor("#3899ED"), Color.parseColor("#8AC3EB"));

        smallCirclePaint.setShader(new LinearGradient(getWidth() / 2, (getHeight() - radius * 2) / 2, (getWidth() + radius * 2) / 2, getHeight() / 2, Color.parseColor("#3899ED"), Color.WHITE, Shader.TileMode.CLAMP));
        fireworksPath.reset();
        fireworksPath.addArc(getOvalRect(getWidth() / 2, getHeight() / 2, radius), -45, 45);
        fireworksPath.addArc(getOvalRect(getWidth() / 2, getHeight() / 2, radius - 10), -45, 45);
        fireworksPath.addArc(getOvalRect(getWidth() / 2, getHeight() / 2, radius + 10), -45, 45);
    }

    private RectF getOvalRect(float x, float y, float radius) {
        return new RectF(x - radius, y - radius, x + radius, y + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        if (rotateAnimator.isRunning()) {
            canvas.save();
            canvas.rotate(angle, centerX, centerY);

            circlePaint.setShader(lightShader);
            canvas.drawCircle(centerX, centerY, radius + 10, circlePaint);
            canvas.drawCircle(centerX + 10, centerY, radius + 10, circlePaint);
            canvas.drawCircle(centerX, centerY + 10, radius + 10, circlePaint);

            circlePaint.setShader(darkShader);
            canvas.drawCircle(centerX - 10, centerY, radius + 10, circlePaint);
            canvas.drawCircle(centerX, centerY - 10, radius + 10, circlePaint);
            canvas.drawCircle(centerX - 10, centerY - 10, radius + 10, circlePaint);

            canvas.drawPath(fireworksPath, smallCirclePaint);
            canvas.restore();
        } else {
            canvas.save();
            canvas.rotate(bigAngle, centerX, centerY);

            bigCirclePaint.setStrokeWidth(2);
            bigCirclePaint.setShader(null);
            bigCirclePaint.setColor(Color.parseColor("#8AC3EB"));
            bigCirclePaint.setShadowLayer(10, 0, 0, Color.WHITE);
            canvas.drawArc(getOvalRect(centerX + 20, centerY, radius), -90, 180, false, bigCirclePaint);
            canvas.drawArc(getOvalRect(centerX + 25, centerY, radius), -90, 180, false, bigCirclePaint);
            canvas.drawArc(getOvalRect(centerX + 30, centerY, radius), -90, 180, false, bigCirclePaint);
            bigCirclePaint.clearShadowLayer();

            bigCirclePaint.setStrokeWidth(30);
            bigCirclePaint.setShader(bigLightShader);
            canvas.drawCircle(centerX, centerY, radius, bigCirclePaint);

            canvas.restore();
        }
    }

}
