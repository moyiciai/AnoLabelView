package cn.moyiciai.lib

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView

/**
 * Created by dx on 2020/11/22
 */
class LabelItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), Checkable {

    private var isChecked: Boolean = false

    override fun setChecked(checked: Boolean) {
        isChecked = checked
    }

    override fun isChecked() = isChecked

    override fun toggle() {
        setChecked(!isChecked)
    }
}