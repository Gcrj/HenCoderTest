package com.gcrj.hencodertest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by zhangxin on 2017/10/16.
 */

public class CircularTapeView extends View {

    private final static float PRECISION = 0.1F;//一个刻度0.1
    private final static int MAJOR_SCALE_LENGTH = 50;//大刻度长度
    private final static int MINOR_SCALE_LENGTH = 25;//小刻度长度

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path clipPath = new Path();
    private RectF touchRectF = new RectF();
    private int radius;
    private int n;//一个刻度，在这里应是每刻度0.1
    private int degree;

    //画3个大刻度，最左边和最右边各有(4个可见刻度 + 1个不可见刻度)，所以总共就是5 + 5 + 10 + 10 + 1 = 31个刻度，30个空隙，每个占5°
    //不可见刻度是为了平滑的画出，这样不会滚到临界值才突然显示出下一个刻度
    private int scaleCount = 30;
    private int scaleAngle = 5;
    private int maxNumberHeight;

    private int minNumber = 0;
    private int maxNumber = 200;

    private GestureDetector gestureDetector;
    private Scroller flingScroller;
    private int lastFlingPosition;
    private boolean isFling;

    private onScrollListener onScrollListener;

    public CircularTapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeWidth(5);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect rect = new Rect();
        paint.getTextBounds("0123456789", 0, 10, rect);
        maxNumberHeight = rect.height();

        gestureDetector = new GestureDetector(context, new GestureListener());
        flingScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width < height) {
            radius = width / 3;
        } else {
            radius = height / 3;
        }

        int diameter = radius * 2;
        setMeasuredDimension(diameter, diameter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = w / 2;

        clipPath.reset();
        RectF clipRectF = new RectF(0, 0, w, h);
        touchRectF.set(clipRectF.left, clipRectF.top, clipRectF.right, clipRectF.bottom - clipRectF.height() / 2);
        clipPath.addArc(clipRectF, -(180 - scaleCount * scaleAngle) / 2, -scaleCount * scaleAngle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        canvas.save();
        canvas.clipPath(clipPath);
        canvas.rotate(degree, centerX, centerY);
        paint.setColor(Color.parseColor("#B7F1FD"));
        //先逆时针转一半
        canvas.rotate(-scaleAngle * (scaleCount / 2), centerX, centerY);
        //然后挨个画
        for (int i = n; i <= scaleCount + n; i++) {
            if (i % 5 == 0 && i % 10 != 0) {//画大刻度
                canvas.drawLine(centerX, centerY - radius, centerX, centerY - (radius - MAJOR_SCALE_LENGTH), paint);
                //因为画法原因，这里的刻度显示是(int) ((i - scaleCount / 2) * PRECISION)
                //画刻度字
                canvas.drawText((int) ((i - scaleCount / 2) * PRECISION) + "", centerX
                        , centerY - (radius - maxNumberHeight - MAJOR_SCALE_LENGTH - 25) //25是文字和刻度距离
                        , paint);
            } else {//画小刻度
                canvas.drawLine(centerX, centerY - radius, centerX, centerY - (radius - MINOR_SCALE_LENGTH), paint);
            }

            canvas.rotate(scaleAngle, centerX, centerY);
        }

        canvas.restore();
        paint.setColor(Color.YELLOW);
        //画选中线
        canvas.drawLine(centerX, centerY - radius, centerX, centerY - (radius - MAJOR_SCALE_LENGTH), paint);
    }

    private boolean lastIsNext = false;
    private boolean hasScrolled = false;

    private void next(int scale) {
        for (int i = 0; i < scale; i++) {
            if (n / 10F >= maxNumber) {
                return;
            }

            next();
        }
    }

    private void last(int scale) {
        for (int i = 0; i < scale; i++) {
            if (n / 10F <= minNumber) {
                return;
            }

            last();
        }
    }

    private void next() {
        if (n / 10F >= maxNumber) {
            return;
        }

        degree--;
        if (degree != 0 && degree == -scaleAngle) {
            degree = 0;
            if (!hasScrolled || lastIsNext) {
                n++;

                if (onScrollListener != null) {
                    onScrollListener.scrolling(n / 10F);
                }
            }
        }

        hasScrolled = true;
        lastIsNext = true;
        invalidate();
    }

    private void last() {
        if (n / 10F <= minNumber) {
            return;
        }

        degree++;
        if (degree != 0 && degree == scaleAngle) {
            degree = 0;
            if (!hasScrolled || !lastIsNext) {
                n--;

                if (onScrollListener != null) {
                    onScrollListener.scrolling(n / 10F);
                }
            }
        }

        hasScrolled = true;
        lastIsNext = false;
        invalidate();
    }

    private void correctDegree(boolean inertia) {
        if (degree == 0) {
            if (onScrollListener != null) {
                onScrollListener.scrollingFinish(n / 10F);
            }
            return;
        }

        if (inertia) {
            if (lastIsNext) {
                for (int i = degree; i > -scaleAngle; i--) {
                    next();
                }

                if (onScrollListener != null) {
                    onScrollListener.scrollingFinish(n / 10F);
                }
            } else {
                for (int i = degree; i < scaleAngle; i++) {
                    last();
                }

                if (onScrollListener != null) {
                    onScrollListener.scrollingFinish(n / 10F);
                }
            }
        } else {
            if (onScrollListener != null) {
                onScrollListener.scrollingFinish(n / 10F);
            }

            degree = 0;
            invalidate();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = gestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (flingScroller.isFinished() && (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)) {
            correctDegree(false);
        }

        return consumed;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (flingScroller.computeScrollOffset()) {
            float distanceX = flingScroller.getCurrX() - lastFlingPosition;
            lastFlingPosition = flingScroller.getCurrX();
//            distanceX = distanceX / 2;
            if (distanceX > 0) {
                if (distanceX < 1) {
                    next();
                } else {
                    next((int) distanceX);
                }
            } else if (distanceX < 0) {
                if (distanceX > -1) {
                    last();
                } else {
                    last((int) -distanceX);
                }
            } else {
                invalidate();
            }
        } else if (isFling) {
            isFling = false;
            correctDegree(true);
        }
    }

    public void setOnScrollListener(CircularTapeView.onScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public int getMinNumber() {
        return minNumber;
    }

    public void setMinNumber(int minNumber) {
        this.minNumber = minNumber;
    }

    public int getMaxNumber() {
        return maxNumber;
    }

    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if (!touchRectF.contains(e.getX(), e.getY())) {
                return false;
            }

            flingScroller.abortAnimation();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            flingScroller.abortAnimation();
            distanceX = distanceX / 5;
            if (distanceX > 0) {
                if (distanceX < 1) {
                    next();
                } else {
                    next((int) distanceX);
                }
            } else {
                if (distanceX > -1) {
                    last();
                } else {
                    last((int) -distanceX);
                }
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            isFling = true;
            lastFlingPosition = (int) e2.getX();
            flingScroller.fling(lastFlingPosition, (int) e2.getY(), (int) -velocityX, (int) -velocityY, 0, getWidth(), 0, getHeight());

            if (onScrollListener != null) {
                onScrollListener.fling(velocityX > 0);
            }

            return true;
        }

    }

    interface onScrollListener {

        void scrolling(float number);

        void fling(boolean flingToRight);

        void scrollingFinish(float number);

    }

}
