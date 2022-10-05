package com.example.realstateapp.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class GridSpacing(private val spacing: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
        outRect.top = spacing
    }
}