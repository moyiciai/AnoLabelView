package cn.moyiciai.anolabelview

import android.content.res.Resources
import android.content.res.TypedArray
import android.util.TypedValue
import java.lang.reflect.TypeVariable

/**
 * Created by moyiciai on 2020/11/19
 */
fun Float.dp2px(): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    Resources.getSystem().displayMetrics
)

