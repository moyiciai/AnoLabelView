# AnoLabelView

# 使用

**1.引入依赖：**

```
implementation 'cn.moyiciai:ano-labelview:1.1.6'
```

**2.编写xml布局**

```xml
<cn.moyiciai.lib.AnoLabelView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:label_horizontalSpace="10dp"
    app:label_verticalSpace="10dp"
    app:label_item_textSize="20sp"
    app:label_item_background="@drawable/tag_bg"
    ...
    />
```

**3.设置 textProvider**

注意，因为`AnoLabelView`可以添加任意类型的数据，所以使用时如果你的标签数据不是`String`类型，那么你需要设置`textProvider`，通过`textProvider`获取显示在标签上的文字，例如：

```kotlin
labelView.textProvider = {
    (it as Person).name
}
```

如果你的标签数据是`String`就不用设置，默认情况下`textProvider`调用了标签数据的`toString()`方法。


**4.XML属性**

| name                     |     format      | description                                           |
| :----------------------- | :-------------: | :---------------------------------------------------- |
| label_horizontalSpace    |    dimension    | 标签间的横向间隔                                      |
| label_verticalSpace      |    dimension    | 标签间的纵向间隔                                      |
| label_maxLines           |     integer     | 最大显示行数，为负数或0则不限制                       |
| label_item_background    | reference,color | 标签项背景色                                          |
| label_item_textColor     | reference,color | 标签项文字颜色                                        |
| label_item_textSize      |    dimension    | 标签项文字大小                                        |
| label_item_padding       |    dimension    | 标签项内边距                                          |
| label_item_paddingLeft   |    dimension    | 标签项左内边距                                        |
| label_item_paddingTop    |    dimension    | 标签项上内边距                                        |
| label_item_paddingRight  |    dimension    | 标签项右内边距                                        |
| label_item_paddingBottom |    dimension    | 标签项下内边距                                        |
| label_item_preview_count |     integer     | 标签项预览的标签个数                                  |
| label_check_type         |      enum       | 可选项：none（不可选）、single（单选）、multi（多选） |
| label_check_maxNum       |     integer     | 多选时有效，最大选择数量                              |

**5.设置监听**

5.1 点击监听

```kotlin
/**
 * view: 标签项（TextView）
 * data: 设置的数据
 * position: 标签位置
 * isChecked: 标签是否被选中
 */
labelView.setOnLabelClickListener { view, data, position, isChecked ->

}
```

5.2 标签选中状态改变监听

```kotlin
/**
 * view: 标签项控件（TextView）
 * data: 设置的数据
 * position: 标签位置
 * isChecked: 标签是否被选中
 */
labelView.setOnCheckedChangeListener { view, data, position, isChecked -> 

}
```

5.3 拦截标签选中状态改变

```kotlin
/**
 * view: 标签项控件（TextView）
 * data: 设置的数据
 * position: 标签位置
 * oldChecked: 旧的选中状态（也就是当前的选中状态）
 * newChecked: 新的选中状态
 *
 * return: 返回true会拦截这次状态改变，改变为newChecked。
 */
labelView.setOnCheckedChangeInterceptor { view, data, position, oldChecked, newChecked ->
    false
}
```

**6.常见方法**

```kotlin

// 设置标签数据
setData(data: List<Any>)

// 在标签列表末尾添加标签
addData(data: Any)

// 在标签列表指定位置添加标签
addData(position: Int, data: Any)

// 移除指定位置上的标签
removeData(position: Int)

// 修改指定位置上的标签数据
editData(position: Int, data: Any)

// 获取选中的标签数据
getCheckedData(): List<T>

// 是否已达到最大选择数
isCheckedMax(): Boolean

// 获取已选标签数量
getCheckedSize(): Int

// 清空所有选中
clearChecked()

// 全选
checkAll()

// 反选
checkInvert()

// 获取选中行数
getLines()

// 标签是否全部显示
isFullDisplay()

// 清空数据
clearData()
```

**7.可设置的属性**

```kotlin
// 标签间的横向间隔
horizontalSpace = 0

// 标签间的纵向间隔
verticalSpace = 0

// 标签背景
itemBackground: Drawable? = null

// 标签文字颜色
itemTextColor: ColorStateList? = ColorStateList.valueOf(0xFF000000.toInt())

// 标签的文字大小（像素）
itemTextSize: Int = 30

// 修改为不可选时会清空所有的已选项；
// 单选改为多选时，如果有标签被选中并且maxCheckedCount小于1，那么maxCheckedCount会被置为1；
// 多选改为单选时，会清空所有的已选项。
checkType: CheckType = CheckType.NONE

// 最大选择数；
// 在多选模式下，如果设置的最大选择数小于已选数量，设置将不会有效。
maxCheckedCount

// 最大显示行数，为负数或0则不限制
maxLines

// 标签内边距
itemPaddingLeft
itemPaddingTop
itemPaddingRight
itemPaddingBottom
```