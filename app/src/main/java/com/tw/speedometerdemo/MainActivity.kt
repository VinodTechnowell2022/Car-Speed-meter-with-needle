package com.tw.speedometerdemo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MainActivity : AppCompatActivity() {


    private val color = intArrayOf(
        R.color.colorAccent,
        R.color.black,
        R.color.maroon,
        R.color.green,
        R.color.blue,
        R.color.fuchsia
    )
    private var speedoMeterView: SpeedoMeterView? = null
    private var seekBar: SeekBar? = null
    private val random = Random()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        speedoMeterView = findViewById(R.id.speedometerview)
        seekBar = findViewById(R.id.seekBar)
        var tvSpeed: TextView = findViewById(R.id.tvSpeed)

        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvSpeed.text = progress.toString()
                speedoMeterView!!.setSpeed(progress, true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { }
        })

    }


    fun removeborder(view: View?) {
        speedoMeterView!!.setisborder(!speedoMeterView!!.isborder())
    }

    fun linecolorchange(view: View?) {
        val color = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        speedoMeterView!!.setLinecolor(color)
    }

    fun needlecolorchange(view: View?) {
        val color = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        speedoMeterView!!.setNeedlecolor(color)
    }

    fun backColorChange(view: View?) {
        speedoMeterView!!.setbackImageResource(color[random.nextInt(color.size)])
    }

}