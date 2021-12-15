package cn.moyiciai.lib

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children

/**
 * Created by moyiciai on 2020/11/14
 *
 * v1.1.11
 */
class AnoLabelView : ViewGroup {

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
    var itemTextColor: ColorStateList? = ColorStateList.valueOf(0xFF000000.toInt())

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
     * AnoLabelView可以存储任何类型的数据，
     * 非String类型数据的话使用TextProvider提供标签显示的文字
     */
    var textProvider: ((Any) -> CharSequence) = { it.toString() }

    /**
     * 当前行数
     */
    private var lines = 0

    /**
     * 记录子控件位置
     */
    private val childrenLayoutCache: SparseArray<Array<Int>> = SparseArray()

    /**
     * 标签选中状态改变时回调
     */
    private var onCheckChangeListener: ((TextView, Any, Int, Boolean) -> Unit)? = null

    /**
     * 标签被点击时回调
     */
    private var onLabelClickListener: ((TextView, Any, Int, Boolean) -> Unit)? = null

    /**
     * 标签选中状态拦截器，返回true表示拦截标签的状态改变
     */
    private var onCheckedChangeInterceptor: ((TextView, Any, Int, Boolean, Boolean) -> Boolean)? =
        null

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

    /**
     * 预览时，显示的标签个数
     */
    private var previewItemCount = 10

    constructor(context: Context) : super(context) {
        preview()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttrs(context, attrs)
        preview()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttrs(context, attrs)
        preview()
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
                R.styleable.AnoLabelView_label_item_preview_count -> {
                    previewItemCount = ta.getInt(attr, previewItemCount)
                }
            }
        }
        ta.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        childrenLayoutCache.clear()

        // 记录整体高度
        var overallHeight = paddingTop
        // 记录当前行中item的最大高度
        var curRowMaxHeight = 0
        // 记录最宽的行宽，初始化为（paddingStart + paddingEnd），可以在没有数据的时候让paddingStart也生效
        var maxRowWidth = paddingStart + paddingEnd
        // 记录当前行宽，初始化为paddingStart，可以在没有数据的时候让paddingStart也生效
        var curRowWidth = paddingStart
        // 最大宽度，如果超过则需要换行
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        // 记录当前行数
        var lines = 1
        var isNewRow = true
        var isMaxLines = false

        for (indexedValue in children.withIndex()) {
            val childView = indexedValue.value

            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
            curRowMaxHeight = curRowMaxHeight.coerceAtLeast(childView.measuredHeight)

            if (isNewRow) {
                curRowWidth = paddingStart
            }

            // 新的一行
            if (curRowWidth + horizontalSpace + childView.measuredWidth + paddingEnd > maxWidth) {
//                curRowWidth -= horizontalSpace
                maxRowWidth = maxRowWidth.coerceAtLeast(curRowWidth + paddingEnd)
                overallHeight += curRowMaxHeight
                if (maxLines <= 0 || lines < maxLines) {
                    overallHeight += verticalSpace
                    lines++
                } else {
                    this.lines = lines
                    isMaxLines = true
                    break
                }
                isNewRow = true
                curRowWidth = paddingStart
            }

            val itemLeft = if (isNewRow) paddingStart else curRowWidth + horizontalSpace
            val itemTop = overallHeight
            val itemRight = itemLeft + childView.measuredWidth
            val itemBottom = itemTop + childView.measuredHeight
            val layoutCache = arrayOf(itemLeft, itemTop, itemRight, itemBottom)
            childrenLayoutCache.put(indexedValue.index, layoutCache)

            curRowWidth = itemRight
            isNewRow = false
            this.lines = lines
        }

        if (isMaxLines) {
            overallHeight += paddingBottom
        } else {
            // 记录最后一行
            maxRowWidth = maxRowWidth.coerceAtLeast(curRowWidth + paddingEnd)
            overallHeight += curRowMaxHeight + paddingBottom
        }

        val measureWidth = measureWidth(widthMeasureSpec, maxRowWidth)

        setMeasuredDimension(
            measureWidth(widthMeasureSpec, measureWidth),
            measureHeight(heightMeasureSpec, overallHeight)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childrenLayoutCache.size()) {
            val layoutCache = childrenLayoutCache[i]
            getChildAt(i).layout(layoutCache[0], layoutCache[1], layoutCache[2], layoutCache[3])
        }
    }

    /**
     * 提供xml预览
     */
    private fun preview() {
        if (isInEditMode.not()) return

        val list = mutableListOf<String>()
        for (i in 0 until previewItemCount) {
            list.add("item $i")
        }
        setData(list)
    }

    /**
     * 测量自身宽度
     */
    private fun measureWidth(measureSpec: Int, contentWidth: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        var resultSize: Int

        when (mode) {
            MeasureSpec.EXACTLY -> {
                resultSize = if (contentWidth < size) contentWidth else size
            }
            else -> {
                resultSize = contentWidth
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
    private fun newChildView(): TextView = TextView(context).apply {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.END
    }

    /**
     * 标签点击监听
     */
    private val mOnClickListener: OnClickListener = OnClickListener { view ->
        if (view !is TextView)
            return@OnClickListener

        val position = views.indexOf(view)

        if (checkType == CheckType.SINGLE) {
            if (singleCheckedPosition >= 0) {
                singleCheckedPosition = if (view == views[singleCheckedPosition]) {
                    setItemChecked(view, false)
                    -1
                } else {
                    val tmp = views[singleCheckedPosition]
                    setItemChecked(tmp, false)
                    onCheckChangeListener?.invoke(
                        tmp,
                        getDataByTag(tmp),
                        singleCheckedPosition,
                        false
                    )
                    setItemChecked(view, true)
                    position
                }
            } else {
                setItemChecked(view, true)
                singleCheckedPosition = position
            }
        } else if (checkType == CheckType.MULTI) {
            if (view.isSelected.not() && checkedViews.size == maxCheckedCount) {
                onLabelClickListener?.invoke(view, getDataByTag(view), position, view.isSelected)
                return@OnClickListener
            }
            toggleViewChecked(view)
        }
        onLabelClickListener?.invoke(view, getDataByTag(view), position, view.isSelected)
        onCheckChangeListener?.invoke(view, getDataByTag(view), position, view.isSelected)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getDataByTag(view: TextView): Any = view.getTag(KEY)

    private fun setDataByTag(view: TextView, data: Any) {
        view.setTag(KEY, data)
    }

    private fun provideText(data: Any): CharSequence {
        return textProvider.invoke(data)
    }

    /**
     * 改变item的选中状态都应该调用该方法
     */
    private fun setItemChecked(view: TextView, isChecked: Boolean) {
        val intercept = onCheckedChangeInterceptor?.invoke(
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

    /**
     * 切换item选中状态
     */
    private fun toggleViewChecked(view: TextView) {
        if (view.isSelected.not() && checkedViews.size == maxCheckedCount) {
            return
        }
        setItemChecked(view, !view.isSelected)
    }

    /**
     * 切换item选中状态
     */
    private fun toggleViewChecked(position: Int) {
        toggleViewChecked(views[position])
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
    private fun addLabelItem(data: Any, position: Int, text: CharSequence) {
        val view = newChildView()
        view.run {
            this.text = text
            background = itemBackground?.constantState?.newDrawable()
            setTextColor(itemTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, itemTextSize.toFloat())
            setPadding(itemPaddingLeft, itemPaddingTop, itemPaddingRight, itemPaddingBottom)
        }
        setDataByTag(view, data)

        ensureItemViewClickable(view)

        views.add(position, view)
        addView(view, position)
    }

    /**
     * 移除标签
     */
    private fun removeLabelItem(position: Int) {
        if (position < 0 || position >= views.size) return
        val removedView = views.removeAt(position)
        if (checkedViews.contains(removedView)) {
            checkedViews.remove(removedView)
            if (checkType == CheckType.SINGLE) {
                singleCheckedPosition = -1
            }
        }
        removeViewAt(position)
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
     * TextView 中的文字是否全部显示，
     * 通过判断是否显示省略号，所以需要设置 ellipsize 属性
     */
    private fun isAllTextShowed(tv: TextView): Boolean {
        tv.ellipsize
        var ret = false
        tv.layout?.let { layout ->
            val lines = layout.lineCount
            ret = !(lines > 0 && layout.getEllipsisCount(lines - 1) > 0)
        }
        return ret
    }

    fun setOnLabelClickListener(listener: (view: TextView, data: Any, position: Int, isChecked: Boolean) -> Unit) {
        onLabelClickListener = listener
    }

    fun setOnCheckedChangeListener(listener: (view: TextView, data: Any, position: Int, isChecked: Boolean) -> Unit) {
        onCheckChangeListener = listener
    }

    fun setOnCheckedChangeInterceptor(
        interceptor: (
            view: TextView,
            data: Any,
            position: Int,
            oldChecked: Boolean,
            newChecked: Boolean
        ) -> Boolean
    ) {
        onCheckedChangeInterceptor = interceptor
    }

    /**
     * 清空数据
     */
    fun clearData() {
        views.clear()
        checkedViews.clear()
        removeAllViews()
        singleCheckedPosition = -1
    }

    /**
     * 设置数据
     */
    fun setData(data: List<Any>?) {
        clearData()

        if (data == null) return

        for (indexed in data.withIndex()) {
            addLabelItem(indexed.value, indexed.index, provideText(indexed.value))
        }
    }

    /**
     * 添加数据
     */
    fun addData(data: Any) {
        addLabelItem(data, views.size, provideText(data))
    }

    /**
     * 添加数据
     */
    fun addData(position: Int, data: Any) {
        addLabelItem(data, position, provideText(data))
    }

    /**
     * 移除数据
     */
    fun removeData(position: Int) {
        removeLabelItem(position)
    }

    /**
     * 修改数据
     */
    fun editData(position: Int, data: Any) {
        if (position < 0 || position >= views.size) {
            throw IllegalArgumentException()
        }
        views[position].text = provideText(data)
        setDataByTag(views[position], data)
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
     * 根据位置设置选中
     */
    fun setChecked(position: Int) {
        if (position < 0 || position >= views.size) return
        toggleViewChecked(position)
    }

    /**
     * 是否已达到最大选择数
     */
    fun isCheckedMax(): Boolean = checkedViews.size == maxCheckedCount

    /**
     * 获取已选标签的数量
     */
    fun getCheckedSize(): Int = checkedViews.size

    /**
     * 清空所有选中
     */
    fun clearChecked() {
        if (checkType == CheckType.SINGLE) {
            singleCheckedPosition = -1
        }
        checkedViews.toList().forEach {
            setItemChecked(it, false)
        }
    }

    /**
     * 全选
     */
    fun checkAll() {
        views.forEach { setItemChecked(it, true) }
    }

    /**
     * 反选
     */
    fun checkInvert() {
        views.forEach { setItemChecked(it, it.isSelected.not()) }
    }

    /**
     * 获取显示行数
     */
    fun getLines() = lines

    /**
     * 标签是否全部显示
     */
    fun isFullDisplay() = childCount == childrenLayoutCache.size()

    companion object {
        private val KEY = R.id.ano_label_view_key
    }
}