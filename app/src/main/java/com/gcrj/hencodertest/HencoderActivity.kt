package com.gcrj.hencodertest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_hencoder.*

class HencoderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hencoder)
        circular_tape_view.setOnScrollListener(object : CircularTapeView.onScrollListener {
            override fun fling(flingToRight: Boolean) {
                if (flingToRight) {
                    fireworks_view.startAnimation()
                }
            }

            override fun scrolling(number: Float) {
                tv.text = number.toString()
            }

            override fun scrollingFinish(number: Float) {
                tv.text = number.toString()
            }

        })

        tv_tv_flip.setOnClickListener {
            fireworks_view.isDrawingCacheEnabled = true
            var bitmap = fireworks_view.drawingCache
            bitmap = bitmap?.copy(bitmap.config, true)
            fireworks_view.isDrawingCacheEnabled = false
            flipboard_view.setBitmap(bitmap)
        }
    }

}
