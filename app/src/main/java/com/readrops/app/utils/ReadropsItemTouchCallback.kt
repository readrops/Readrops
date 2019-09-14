package com.readrops.app.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ReadropsItemTouchCallback(private val config: Config) : ItemTouchHelper.SimpleCallback(config.dragDirs, config.swipeDirs) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        config.moveCallback?.onMove()
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        config.swipeCallback?.onSwipe(viewHolder, direction)
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

    class Config(val dragDirs: Int = 0, val swipeDirs: Int = 0, val moveCallback: MoveCallback? = null,
                 val swipeCallback: SwipeCallback? = null) {

        private constructor(builder: Builder) : this(builder.dragDirs, builder.swipeDirs, builder.moveCallback, builder.swipeCallback)

        class Builder {
            var dragDirs: Int = 0
                private set

            var swipeDirs: Int = 0
                private set

            var moveCallback: MoveCallback? = null
                private set

            var swipeCallback: SwipeCallback? = null
                private set

            fun dragDirs(dragDirs: Int) = apply { this.dragDirs = dragDirs }

            fun swipeDirs(swipeDirs: Int) = apply { this.swipeDirs = swipeDirs }

            fun moveCallback(moveCallback: MoveCallback) = apply { this.moveCallback = moveCallback }

            fun swipeCallback(swipeCallback: SwipeCallback) = apply { this.swipeCallback = swipeCallback }

            fun build(): Config {
                return Config(this)
            }
        }
    }


}