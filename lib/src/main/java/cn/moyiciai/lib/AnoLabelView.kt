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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children

/**
 * Created by dx on 2020/11/14
 */
class AnoLabelView : ViewGroup {

    private val KEY = R.id.ano_label_view_key

    /**
     * 标签间的横向间隔
     */
    var horizontalSpace = 0
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 标签间的纵向间隔
     */
    var verticalSpace = 0
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 标签背景
     */
    var itemBackground: Drawable? = null

    /**
     * 标签的文字颜色
     */
    var itemTextColor: ColorStateList? = null

    /**
     * 标签的文字大小
     */
    var itemTextSize: Int = 30
        set(value) {
            field = value
            views.forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_PX, value.toFloat()) }
        }

    /**
     * 修改为不可选时会清空所有的已选项；
     * 单选改为多选时，如果有标签被选中并且maxCheckedCount小于1，那么maxCheckedCount会被置为1；
     * 多选改为单选时，会清空所有的已选项。
     *
     * @see CheckType
     */
    var checkType: CheckType = CheckType.NONE
        set(value) {
            if (field == value) return
            when (field) {
                CheckType.SINGLE -> {
                    if (field == CheckType.NONE) {
                        checkedViews.forEach { setItemChecked(it, false) }
                    } else if (field == CheckType.MULTI) {
                        maxCheckedCount = maxCheckedCount.coerceAtLeast(1)
                    }
                }
                CheckType.MULTI -> {
                    checkedViews.forEach { setItemChecked(it, false) }
                }
                else -> print("")
            }
            field = value
        }

    /**
     * 最大选择数；
     * 在多选模式下，如果设置的最大选择数小于已选数量，设置将不会有效。
     */
    var maxCheckedCount: Int = 1
        set(value) {
            if (field == value) return
            if (checkType == CheckType.MULTI && value < checkedViews.size) return
            field = value
        }

    /**
     * 最大显示行数，为负数或0则不限制
     */
    var maxLines: Int = -1
        set(value) {
            field = value
            requestLayout()
        }

    /**
     * 标签左内边距
     */
    var itemPaddingLeft: Int = 0
        set(value) {
            field = value
            setupItemPadding()
        }

    /**
     * 标签上内边距
     */
    var itemPaddingTop: Int = 0
        set(value) {
            field = value
            setupItemPadding()
        }

    /**
     * 标签右内边距
     */
    var itemPaddingRight: Int = 0
        set(value) {
            field = value
            setupItemPadding()
        }

    /**
     * 标签下内边距
     */
    var itemPaddingBottom: Int = 0
        set(value) {
            field = value
            setupItemPadding()
        }

    /**
     * 记录子控件位置
     */
    private val childRectCache: SparseArray<Rect> = SparseArray()

    private var onCheckChangeListener: OnCheckedChangeListener? = null

    private var onLabelClickListener: OnLabelClickListener? = null

    private var onCheckedChangeInterceptor: OnCheckedChangeInterceptor? = null

    /**
     * 保存所有的子控件
     */
    private val views: ArrayList<TextView> = arrayListOf()

    /**
     * 保存所有选中状态的子控件
     */
    private val checkedViews: LinkedHashSet<TextView> = linkedSetOf()

    /**
     * 单选时被选中的item的位置
     */
    private var singleCheckedPosition: Int = -1

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
                R.styleable.AnoLabelView_label_check_maxNum -> {
                    maxCheckedCount = ta.getInt(attr, 1)
                }
                R.styleable.AnoLabelView_label_maxLines -> {
                    maxLines = ta.getInt(attr, -1)
                }
                R.styleable.AnoLabelView_label_item_padding -> {
                    val padding = ta.getDimensionPixelSize(attr, 0)
                    itemPaddingLeft = padding
                    itemPaddingTop = padding
                    itemPaddingRight = padding
                    itemPaddingBottom = padding
                }
                R.styleable.AnoLabelView_label_item_paddingLeft -> {
                    itemPaddingLeft = ta.getDimensionPixelSize(attr, 0)
                }
                R.styleable.AnoLabelView_label_item_paddingTop -> {
                    itemPaddingTop = ta.getDimensionPixelSize(attr, 0)
                }
                R.styleable.AnoLabelView_label_item_paddingRight -> {
                    itemPaddingRight = ta.getDimensionPixelSize(attr, 0)
                }
                R.styleable.AnoLabelView_label_item_paddingBottom -> {
                    itemPaddingBottom = ta.getDimensionPixelSize(attr, 0)
                }
            }
        }
        ta.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        childRectCache.clear()

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
        // 记录当前行数
        var lines = 1

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
                if (lines == maxLines) { // 达到最大行数限制时结束测量
                    overallHeight += curRowMaxHeight
                    setMeasuredDimension(
                        measureWidth(widthMeasureSpec, maxRowWidth),
                        measureHeight(heightMeasureSpec, overallHeight)
                    )
                    return
                } else {
                    overallHeight += (verticalSpace + curRowMaxHeight)
                }
                curRowWidth = 0
                curRowMaxHeight = 0
                l = 0
                lines++
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
        for (indexed in children.withIndex()) {
            val i = indexed.index
            if (i < childRectCache.size()) {
                val rect = childRectCache[i]
                getChildAt(i).layout(rect.left, rect.top, rect.right, rect.bottom)
            } else {
                // 通过改变横向间隔导致最大行数超过限制，多余出来的View可能已经被布局过，所以此处要把它们的位置清零
                getChildAt(i).layout(0, 0, 0, 0)
            }
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

        // 多选
        MULTI(3);

        companion object {
            fun get(value: Int): CheckType {
                return when (value) {
                    1 -> NONE
                    2 -> SINGLE
                    3 -> MULTI
                    else -> NONE
                }
            }
        }
    }

    /**
     * 新建子View
     */
    private fun newChildView(): TextView = TextView(context)

    /**
     * 标签点击监听
     */
    private val mOnClickListener: OnClickListener = OnClickListener { view ->
        if (view !is TextView)
            return@OnClickListener

        val position = views.indexOf(view)

        if (checkType == CheckType.SINGLE) {
            if (singleCheckedPosition >= 0) {
                if (view == views[singleCheckedPosition]) {
                    setItemChecked(view, false)
                } else {
                    val tmp = views[singleCheckedPosition]
                    setItemChecked(tmp, false)
                    onCheckChangeListener?.onCheckedChanged(tmp, getDataByTag(tmp), false)
                    setItemChecked(view, true)
                    singleCheckedPosition = position
                }
            } else {
                setItemChecked(view, true)
                singleCheckedPosition = position
            }
        } else if (checkType == CheckType.MULTI) {
            if (view.isSelected.not() && checkedViews.size == maxCheckedCount) {
                onLabelClickListener?.onLabelClick(view, getDataByTag(view), position)
                return@OnClickListener
            }
            toggleViewChecked(view)
        }

        onLabelClickListener?.onLabelClick(view, getDataByTag(view), position)
        onCheckChangeListener?.onCheckedChanged(view, getDataByTag(view), view.isSelected)
    }

    private fun getDataByTag(view: TextView): Any = view.getTag(KEY)

    private fun setDataByTag(view: TextView, data: Any) {
        view.setTag(KEY, data)
    }

    /**
     * 改变item的选中状态都应该调用该方法
     */
    private fun setItemChecked(view: TextView, isChecked: Boolean) {
        val intercept = onCheckedChangeInterceptor?.onCheckedChangeIntercept(
            view,
            getDataByTag(view),
            views.indexOf(view),
            view.isSelected,
            isChecked
        ) ?: false

        if (intercept) return

        view.isSelected = isChecked
        if (isChecked) checkedViews.add(view)
        else checkedViews.remove(view)
    }

    private fun toggleViewChecked(view: TextView) {
        setItemChecked(view, !view.isSelected)
    }

    /**
     * 确保view响应点击事件
     */
    private fun ensureItemViewClickable(view: View) {
        val isClickable = checkType != CheckType.NONE
        view.setOnClickListener(if (isClickable) mOnClickListener else null)
        view.isClickable = isClickable
    }

    /**
     * 添加标签
     */
    private fun <T> addLabelItem(data: T, position: Int, text: CharSequence) {
        val view = newChildView()
        view.run {
            this.text = text
            background = itemBackground?.constantState?.newDrawable()
            setTextColor(itemTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize.toFloat())
            setPadding(itemPaddingLeft, itemPaddingTop, itemPaddingRight, itemPaddingBottom)
        }
        setDataByTag(view, data as Any)

        ensureItemViewClickable(view)

        addView(view)
        views.add(position, view)
    }

    /**
     * 设置标签内边距，设置itemPadding*属性时调用
     */
    private fun setupItemPadding() {
        views.forEach {
            it.setPadding(
                itemPaddingLeft,
                itemPaddingTop,
                itemPaddingRight,
                itemPaddingBottom
            )
        }
    }

    /**
     * AnoLabelView可以存储任何类型的数据，
     * 非String类型数据的话使用TextProvider提供标签显示的文字
     */
    interface TextProvider<T> {
        fun getText(data: T, position: Int): CharSequence
    }

    /**
     * 点击监听
     */
    interface OnLabelClickListener {
        fun onLabelClick(view: TextView, data: Any, position: Int)
    }

    fun setOnLabelClickListener(listener: OnLabelClickListener) {
        onLabelClickListener = listener
    }

    /**
     * 状态改变监听
     */
    interface OnCheckedChangeListener {
        fun onCheckedChanged(view: TextView, data: Any, isChecked: Boolean)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        onCheckChangeListener = listener
    }

    /**
     * 状态改变监听拦截
     */
    interface OnCheckedChangeInterceptor {
        fun onCheckedChangeIntercept(
            view: TextView,
            data: Any,
            position: Int,
            oldChecked: Boolean,
            newChecked: Boolean
        ): Boolean
    }

    fun setOnCheckedChangeInterceptor(interceptor: OnCheckedChangeInterceptor) {
        onCheckedChangeInterceptor = interceptor
    }

    /**
     * 设置数据
     */
    fun setData(data: List<String>) {
        setData(data, object : TextProvider<String> {
            override fun getText(data: String, position: Int) = data
        })
    }

    /**
     * 设置数据
     */
    fun <T> setData(data: List<T>, textProvider: TextProvider<T>) {
        views.clear()
        removeAllViews()

        for (indexed in data.withIndex()) {
            addLabelItem(
                indexed.value,
                indexed.index,
                textProvider.getText(indexed.value, indexed.index)
            )
        }
        requestLayout()
    }

    /**
     * 获取选中的数据
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCheckedData(): List<T> {
        return checkedViews.map {
            getDataByTag(it) as T
        }
    }

    /**
     * 是否已达到最大选择数
     */
    fun isCheckedMax(): Boolean = views.size == maxCheckedCount

    /**
     * 获取已选标签的数量
     */
    fun getCheckedSize(): Int = checkedViews.size
}