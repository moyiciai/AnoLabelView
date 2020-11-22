package cn.moyiciai.lib

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children

/**
 * Created by dx on 2020/11/14
 */
class AnoLabelView : ViewGroup {

    var horizontalSpace = 0
    var verticalSpace = 0
    var itemBackground: Drawable? = null
    var itemTextColor: ColorStateList? = null
    var itemTextSize: Int = 30
    var checkType: CheckType = CheckType.NONE
    var onCheckChangeListener: OnCheckedChangeListener? = null

    private val childRectCache: SparseArray<Rect> = SparseArray()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AnoLabelView)
        for (i in 0 until ta.indexCount) {
            when (val attr = ta.getIndex(i)) {
                R.styleable.AnoLabelView_label_horizontalSpace -> {
                    horizontalSpace = ta.getDimensionPixelSize(attr, 30)
                }
                R.styleable.AnoLabelView_label_verticalSpace -> {
                    verticalSpace = ta.getDimensionPixelSize(attr, 10)
                }
                R.styleable.AnoLabelView_label_item_background -> {
                    val res = ta.getResourceId(attr, 0)
                    if (res != 0) {
                        itemBackground = ContextCompat.getDrawable(context, res)
                        continue
                    }
                    val color = ta.getColor(attr, Color.TRANSPARENT)
                    itemBackground = ColorDrawable(color)
                }
                R.styleable.AnoLabelView_label_item_textColor -> {
                    itemTextColor = ta.getColorStateList(attr)
                }
                R.styleable.AnoLabelView_label_item_textSize -> {
                    itemTextSize = ta.getDimensionPixelSize(attr, 30)
                }
                R.styleable.AnoLabelView_label_check_type -> {
                    val value = ta.getInt(attr, CheckType.NONE.value)
                    checkType = CheckType.get(value)
                }
            }
        }
        ta.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // 记录整体高度
        var overallHeight = 0
        // 记录最宽的行宽
        var maxRowWidth = 0
        // 记录当前行宽
        var curRowWidth = 0
        // 记录当前行中item的最大高度
        var curRowMaxHeight = 0
        // 最大宽度，如果超过则需要换行
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        // 新的一行
        var newRow = true

        var l: Int

        for (i in children.withIndex()) {
            val childView = i.value
            val index = i.index

            if (newRow) newRow = false
            else curRowWidth += horizontalSpace

            l = curRowWidth
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)

            // 需要换行的情况
            if (childView.measuredWidth + curRowWidth >= maxWidth) {
                maxRowWidth = curRowWidth.coerceAtLeast(maxRowWidth)
                overallHeight += (verticalSpace + curRowMaxHeight)
                curRowWidth = 0
                curRowMaxHeight = 0
                l = 0
            }

            curRowMaxHeight = curRowMaxHeight.coerceAtLeast(childView.measuredHeight)
            curRowWidth += childView.measuredWidth

            // 记录子View位置
            val childRect = Rect(
                l, overallHeight, curRowWidth, overallHeight + childView.measuredHeight
            )
            childRectCache.put(index, childRect)
        }

        // 记录最后一行
        maxRowWidth = curRowWidth.coerceAtLeast(maxRowWidth)
        overallHeight += curRowMaxHeight

        setMeasuredDimension(
            measureWidth(widthMeasureSpec, maxRowWidth),
            measureHeight(heightMeasureSpec, overallHeight)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in children.withIndex()) {
            val rect = childRectCache[i.index]
            i.value.layout(rect.left, rect.top, rect.right, rect.bottom)
        }
    }

    /**
     * 测量自身宽度
     */
    private fun measureWidth(measureSpec: Int, contentWidth: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(contentWidth)
        var resultSize: Int

        when (mode) {
            MeasureSpec.EXACTLY -> resultSize = size
            else -> {
                resultSize = contentWidth + paddingLeft + paddingRight
                if (mode == MeasureSpec.AT_MOST)
                    resultSize = resultSize.coerceAtMost(size)
            }
        }

        return resultSize.coerceAtLeast(suggestedMinimumWidth)
    }

    /**
     * 测量自身高度
     */
    private fun measureHeight(measureSpec: Int, contentHeight: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(contentHeight)
        var resultSize: Int

        when (mode) {
            MeasureSpec.EXACTLY -> resultSize = size
            else -> {
                resultSize = contentHeight + paddingTop + paddingBottom
                if (mode == MeasureSpec.AT_MOST)
                    resultSize = resultSize.coerceAtMost(size)
            }
        }

        return resultSize.coerceAtLeast(suggestedMinimumHeight)
    }

    /**
     * 标签的选择类型
     */
    enum class CheckType(val value: Int) {
        // 不可选
        NONE(1),
        // 单选
        SINGLE(2),
        // 单选，不可反选
        SINGLE_IRREVOCABLY(3),
        // 多选
        MULTI(4);

        companion object {
            fun get(value: Int): CheckType {
                return when (value) {
                    1 -> NONE
                    2 -> SINGLE
                    3 -> SINGLE_IRREVOCABLY
                    4 -> MULTI
                    else -> NONE
                }
            }
        }
    }

    /**
     * 新建子View
     */
    private fun newChildView(): TextView {
        val view = TextView(context)
        view.run {
            background = itemBackground
            setTextColor(itemTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize.toFloat())
        }
        return view
    }

    /**
     * 设置数据
     */
    fun setData(data: List<String>) {
        for (datum in data) {
            val childView = newChildView()
            childView.text = datum
            addView(childView)
        }
        requestLayout()
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(labelView: TextView, isChecked: Boolean)
    }
}