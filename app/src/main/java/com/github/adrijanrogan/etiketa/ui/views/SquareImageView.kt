package com.github.adrijanrogan.etiketa.ui.views

import android.content.Context
import android.util.AttributeSet

class SquareImageView : androidx.appcompat.widget.AppCompatImageView {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    // Poskrbi, da je nas ImageView vedno kvadratne oblike.
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}