package com.readrops.app.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.readrops.app.R

/**
 * A simple custom view to display a empty list message
 */
class EmptyListView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        // no binding here, it makes the view rendering fail
        View.inflate(context, R.layout.empty_list_view, this)
        val imageView: ImageView = findViewById(R.id.empty_list_image)
        val textView: TextView = findViewById(R.id.empty_list_text_v)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.EmptyListView)
        imageView.setImageDrawable(attributes.getDrawable(R.styleable.EmptyListView_image))
        textView.text = attributes.getString(R.styleable.EmptyListView_text)

        attributes.recycle()
    }
}