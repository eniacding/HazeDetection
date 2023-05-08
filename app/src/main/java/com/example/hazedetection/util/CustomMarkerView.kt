package com.example.hazedetection.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.hazedetection.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight

/**
 * 自定义图表的MarkerView(点击坐标点，弹出提示框)
 */
internal class CustomMarkerView(context: Context?, layoutResource: Int, private val unitName: String) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById<View>(R.id.txt_tips) as TextView

    /**
     *
     * @param context
     * 上下文
     * @param layoutResource
     * 资源文件
     * @param unitName
     * Y轴数值计量单位名称
     */

    // 每次markerview回调重绘，可以用来更新内容
    override fun refreshContent(e: Entry, highlight: Highlight) {
        // 设置Y周数据源对象Entry的value值为显示的文本内容
        tvContent.text = "" + e.y + unitName
    }

    fun getXOffset(xpos: Float): Int {
        // 水平居中
        return -(width / 2)
    }

    fun getYOffset(ypos: Float): Int {
        // 提示框在坐标点上方显示
        return -height
    }
}