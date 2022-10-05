package com.example.realstateapp.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View


class ColumnQty(context: Context, viewId: Int) {
    private val width: Int
    private val height: Int
    private var remaining = 0
    private val displayMetrics: DisplayMetrics
    fun calculateNoOfColumns(): Int {
        var numberOfColumns = displayMetrics.widthPixels / width
        remaining = displayMetrics.widthPixels - numberOfColumns * width
        //        System.out.println("\nRemaining\t" + remaining + "\nNumber Of Columns\t" + numberOfColumns);
        if (remaining / (2 * numberOfColumns) < 15) {
            numberOfColumns--
            remaining = displayMetrics.widthPixels - numberOfColumns * width
        }
        return numberOfColumns
    }

    fun calculateSpacing(): Int {
        val numberOfColumns = calculateNoOfColumns()
        //        System.out.println("\nNumber Of Columns\t"+ numberOfColumns+"\nRemaining Space\t"+remaining+"\nSpacing\t"+remaining/(2*numberOfColumns)+"\nWidth\t"+width+"\nHeight\t"+height+"\nDisplay DPI\t"+displayMetrics.densityDpi+"\nDisplay Metrics Width\t"+displayMetrics.widthPixels);
        return remaining / (2 * numberOfColumns)
    }

    init {
        val view: View = View.inflate(context, viewId, null)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = view.measuredWidth
        height = view.measuredHeight
        displayMetrics = context.resources.displayMetrics
    }
}