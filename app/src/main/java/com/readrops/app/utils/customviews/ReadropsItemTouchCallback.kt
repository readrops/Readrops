package com.readrops.app.utils.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ReadropsItemTouchCallback(private val context: Context, private val config: Config) :
        ItemTouchHelper.SimpleCallback(config.dragDirs, config.swipeDirs) {

    private val iconHorizontalMargin = 40

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        config.moveCallback?.onMove()
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        config.swipeCallback?.onSwipe(viewHolder, direction)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val background: ColorDrawable
        var icon: Drawable? = null
        val itemView: View = viewHolder.itemView
        var draw = true // variable used to draw under some conditions

        // do not draw anymore if the view has reached the screen's left/right side
        if (abs(dX).toInt() == itemView.right) {
            draw = false
        } else if (abs(dX).toInt() == 0) {
            draw = true
        }

        // left swipe
        if (dX > 0 && config.leftDraw != null && draw) {
            background = ColorDrawable(config.leftDraw.bgColor)
            background.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)

            icon = config.leftDraw.drawable
                    ?: ContextCompat.getDrawable(context, config.leftDraw.iconRes)!!
            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
            icon.setBounds(itemView.left + iconHorizontalMargin, itemView.top + iconMargin,
                    itemView.left + iconHorizontalMargin + icon.intrinsicWidth, itemView.bottom - iconMargin)
            // right swipe
        } else if (dX < 0 && config.rightDraw != null && draw) {
            background = ColorDrawable(config.rightDraw.bgColor)
            background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

            icon = config.rightDraw.drawable
                    ?: ContextCompat.getDrawable(context, config.rightDraw.iconRes)!!
            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
            icon.setBounds(itemView.right - iconHorizontalMargin - icon.intrinsicWidth, itemView.top + iconMargin,
                    itemView.right - iconHorizontalMargin, itemView.bottom - iconMargin)
        } else {
            background = ColorDrawable()
        }

        background.draw(c)

        if (dX > 0)
            c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
        else if (dX < 0)
            c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

        icon?.draw(c)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return config.swipeCallback != null
    }

    override fun isLongPressDragEnabled(): Boolean {
        return config.moveCallback != null
    }

    interface MoveCallback {
        fun onMove()
    }

    interface SwipeCallback {
        fun onSwipe(viewHolder: RecyclerView.ViewHolder, direction: Int)
    }

    class SwipeDraw(@ColorInt val bgColor: Int, @DrawableRes val iconRes: Int = 0, val drawable: Drawable?)

    class Config(val dragDirs: Int = 0, val swipeDirs: Int = 0, val moveCallback: MoveCallback? = null,
                 val swipeCallback: SwipeCallback? = null, val leftDraw: SwipeDraw? = null, val rightDraw: SwipeDraw? = null) {

        private constructor(builder: Builder) : this(builder.dragDirs, builder.swipeDirs,
                builder.moveCallback, builder.swipeCallback, builder.leftDraw, builder.rightDraw)

        class Builder {
            var dragDirs: Int = 0
                private set

            var swipeDirs: Int = 0
                private set

            var moveCallback: MoveCallback? = null
                private set

            var swipeCallback: SwipeCallback? = null
                private set

            var leftDraw: SwipeDraw? = null
                private set

            var rightDraw: SwipeDraw? = null
                private set

            fun dragDirs(dragDirs: Int) = apply { this.dragDirs = dragDirs }

            fun swipeDirs(swipeDirs: Int) = apply { this.swipeDirs = swipeDirs }

            fun moveCallback(moveCallback: MoveCallback) = apply { this.moveCallback = moveCallback }

            fun swipeCallback(swipeCallback: SwipeCallback) = apply { this.swipeCallback = swipeCallback }

            fun leftDraw(@ColorInt bgColor: Int, @DrawableRes iconRes: Int, @Nullable icon: Drawable? = null) = apply { leftDraw = SwipeDraw(bgColor, iconRes, icon) }

            fun rightDraw(@ColorInt bgColor: Int, @DrawableRes iconRes: Int, @Nullable icon: Drawable? = null) = apply { this.rightDraw = SwipeDraw(bgColor, iconRes, icon) }

            fun build(): Config {
                return Config(this)
            }
        }
    }


}