package com.gcrj.hencodertest

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator


/**
 * Created by zhangxin on 2017/10/19.
 */
class FlipboardView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        const val PAINT_STROKE_WIDTH = 10F
    }

    private val paint = Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private var bitmap: Bitmap? = null
    private var camera = Camera()
    private var degree = 0F
    private var animator = ObjectAnimator.ofFloat(this, "degree", 0F, 360F)

    private val staticPath by lazy {
        Path()
    }
    private val flipPath by lazy {
        Path()
    }

    private var zDegree = 0F
    private var yDegree = 0F

    init {
        paint.isFilterBitmap = true
        paint.strokeWidth = PAINT_STROKE_WIDTH
        paint.color = Color.YELLOW
        paint.style = Paint.Style.STROKE

        animator.duration = 3600
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE

        val displayMetrics = resources.displayMetrics
        val newZ = -displayMetrics.density * 6
        camera.setLocation(0F, 0F, newZ * 2)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animator.isRunning) {
            animator.cancel()
        }
    }

    //整成一个方的，方便动画计算
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val length = if (width < height) width else height
        setMeasuredDimension(length, length)
    }

    fun setBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            //原图是个圆表，动画不明显，画上一个框
            val rectBitmap = Bitmap.createBitmap(bitmap.width + PAINT_STROKE_WIDTH.toInt(), bitmap.height + PAINT_STROKE_WIDTH.toInt(), bitmap.config)
            val canvas = Canvas(rectBitmap)
            canvas.drawBitmap(bitmap, PAINT_STROKE_WIDTH, PAINT_STROKE_WIDTH, paint)
            val halfStrokeWidth = PAINT_STROKE_WIDTH.toInt() / 2
            canvas.drawRect(Rect(halfStrokeWidth, halfStrokeWidth, rectBitmap.width - halfStrokeWidth, rectBitmap.height - halfStrokeWidth), paint)
            this.bitmap = rectBitmap

            if (animator.isRunning) {
                animator.cancel()
            }

            animator.start()
        }
    }

    fun setDegree(degree: Float) {
        this.degree = degree
        bitmap ?: return;

        staticPath.reset()
        flipPath.reset()

        //因为是方的
        val length = width.toFloat()
        //下面有时候转45°，有时候转90°，完全是因为我解析动画的时候脑子这么想的顺，所以就这么分了
        when (degree) {
            in 0F..45F -> {// 右半边起来
                staticPath.moveTo(0F, 0F)
                staticPath.lineTo(length / 2, 0F)
                staticPath.lineTo(length / 2, length)
                staticPath.lineTo(0F, length)
                staticPath.close()

                flipPath.moveTo(length / 2, 0F)
                flipPath.lineTo(length, 0F)
                flipPath.lineTo(length, length)
                flipPath.lineTo(length / 2, length)
                flipPath.close()

                yDegree = -degree
                zDegree = 0F
            }
            in 45F..90F -> {// Z轴转45°
                val diff = (degree - 45) / 45 * (length / 2)

                staticPath.moveTo(0F, 0F)
                staticPath.lineTo(length / 2 - diff, 0F)
                staticPath.lineTo(length / 2 + diff, length)
                staticPath.lineTo(0F, length)
                staticPath.close()

                flipPath.moveTo(length / 2 - diff, 0F)
                flipPath.lineTo(length, 0F)
                flipPath.lineTo(length, length)
                flipPath.lineTo(length / 2 + diff, length)
                flipPath.close()

                yDegree = -45F
                zDegree = degree - 45
            }
            in 90F..180F -> {// Z轴再转90°
                val diff = (degree - 90) / 90 * length

                staticPath.moveTo(0F, diff)
                staticPath.lineTo(0F, length)
                staticPath.lineTo(length, length)
                staticPath.lineTo(length, length - diff)
                staticPath.close()

                flipPath.moveTo(0F, diff)
                flipPath.lineTo(0F, 0F)
                flipPath.lineTo(length, 0F)
                flipPath.lineTo(length, length - diff)
                flipPath.close()

                yDegree = -45F
                zDegree = degree - 45
            }
            in 180F..270F -> {// Z轴再转90°
                val diff = (degree - 180) / 90 * length

                staticPath.moveTo(length - diff, 0F)
                staticPath.lineTo(length, 0F)
                staticPath.lineTo(length, length)
                staticPath.lineTo(diff, length)
                staticPath.close()

                flipPath.moveTo(0F, 0F)
                flipPath.lineTo(length - diff, 0F)
                flipPath.lineTo(diff, length)
                flipPath.lineTo(0F, length)
                flipPath.close()

                yDegree = -45F
                zDegree = degree - 45
            }
            in 270F..315F -> {// Z轴再转45°终点为下半边起来
                val diff = (degree - 270) / 45 * (length / 2)

                staticPath.moveTo(0F, 0F)
                staticPath.lineTo(length, 0F)
                staticPath.lineTo(length, length - diff)
                staticPath.lineTo(0F, diff)
                staticPath.close()

                flipPath.moveTo(0F, diff)
                flipPath.lineTo(length, length - diff)
                flipPath.lineTo(length, length)
                flipPath.lineTo(0F, length)
                flipPath.close()

                yDegree = -45F
                zDegree = degree - 45
            }
            in 315F..360F -> {// 上半边也起来，这里的staticPath和flipPath较真的话有点混淆，yDegree这里其实也是xDegree
                staticPath.moveTo(0F, 0F)
                staticPath.lineTo(length, 0F)
                staticPath.lineTo(length, length / 2)
                staticPath.lineTo(0F, length / 2)
                staticPath.close()

                flipPath.moveTo(0F, length / 2)
                flipPath.lineTo(length, length / 2)
                flipPath.lineTo(length, length)
                flipPath.lineTo(0F, length)
                flipPath.close()

                yDegree = 315 - degree
                zDegree = 0F
            }
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = this.bitmap ?: return;
        canvas.drawColor(Color.parseColor("#3899ED"))

        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        val centerX = width / 2F
        val centerY = height / 2F
        val x = centerX - bitmapWidth / 2
        val y = centerY - bitmapHeight / 2

        // 不动的部分
        canvas.save()
        canvas.clipPath(staticPath)
        canvas.translate(centerX, centerY)

        camera.save()
        camera.rotateZ(zDegree)
        if (degree >= 315) {//上半边起来，所以yDegree当xDegree用
            camera.rotateX(yDegree)
        }

        camera.applyToCanvas(canvas)
        camera.restore()

        canvas.rotate(zDegree)
        canvas.translate((-centerX), (-centerY))
        canvas.drawBitmap(bitmap, x, y, paint)
        canvas.restore()


        // 动的部分
        canvas.save()
        canvas.clipPath(flipPath)
        canvas.translate(centerX, centerY)

        camera.save()
        camera.rotateZ(zDegree)
        if (degree >= 315) {//下半边保持住
            camera.rotateX(45F)
        } else {
            camera.rotateY(yDegree)
        }

        camera.applyToCanvas(canvas)
        camera.restore()

        canvas.rotate(zDegree)
        canvas.translate((-centerX), (-centerY))
        canvas.drawBitmap(bitmap, x, y, paint)
        canvas.restore()
    }

}