package cn.moyiciai.anolabelview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import cn.moyiciai.lib.AnoLabelView

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var labelView: AnoLabelView<String>

    private var count = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        labelView = findViewById(R.id.label_view)
        labelView.textProvider = object : AnoLabelView.TextProvider<String> {
            override fun getText(data: String): CharSequence = data
        }

        initControlView()

        findViewById<Button>(R.id.btn_show_label).setOnClickListener {
            val data = mutableListOf<String>()
            for (i in 0 until count) {
                if (i == 5) {
                    data.add("不可点击")
                } else {
                    data.add("item $i")
                }
            }

            labelView.run {
                setData(data)
                setOnCheckedChangeListener(object : AnoLabelView.OnCheckedChangeListener<String> {
                    override fun onCheckedChanged(view: TextView, data: String, isChecked: Boolean) {
                        Log.d("dx", "${data}, isChecked=$isChecked")
                    }
                })
                setOnLabelClickListener(object : AnoLabelView.OnLabelClickListener<String> {
                    override fun onLabelClick(view: TextView, data: String, position: Int) {
                        if (position == 5) {
                            Toast.makeText(
                                this@MainActivity,
                                "当前标签被设置为不可点击",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.d("dx", "onClick=$data")
                    }
                })
                setOnCheckedChangeInterceptor(object : AnoLabelView.OnCheckedChangeInterceptor<String> {
                    override fun onCheckedChangeIntercept(
                        view: TextView,
                        data: String,
                        position: Int,
                        oldChecked: Boolean,
                        newChecked: Boolean
                    ): Boolean {
                        return position == 5
                    }
                })
            }
        }
    }

    private fun initControlView() {
        val tvHSpace = findViewById<TextView>(R.id.tv_horizontal_space)
        val tvVSpace = findViewById<TextView>(R.id.tv_vertical_space)
        val tvTextSize = findViewById<TextView>(R.id.tv_text_size)
        val tvMaxLines = findViewById<TextView>(R.id.tv_max_lines)
        val tvPadding = findViewById<TextView>(R.id.tv_padding)

        findViewById<SeekBar>(R.id.seekBar_horizontal_space).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                labelView.horizontalSpace = progress
                tvHSpace.text = "横向间隔（$progress）"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<SeekBar>(R.id.seekBar_vertical_space).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                labelView.verticalSpace = progress
                tvVSpace.text = "纵向间隔（$progress）"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<SeekBar>(R.id.seekBar_text_size).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                labelView.itemTextSize = progress
                tvTextSize.text = "文字大小（$progress）"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<SeekBar>(R.id.seekBar_max_lines).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                labelView.maxLines = progress
                tvMaxLines.text = "最大行数（$progress）"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<SeekBar>(R.id.seekBar_padding).setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                labelView.itemPaddingLeft = progress
                labelView.itemPaddingTop = progress
                labelView.itemPaddingRight = progress
                labelView.itemPaddingBottom = progress
                tvPadding.text = "内边距（$progress）"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        findViewById<Button>(R.id.btn_new_first).setOnClickListener {
            labelView.addData(0, "item $count")
            count++
        }

        findViewById<Button>(R.id.btn_new_mid).setOnClickListener {
            val pos = (labelView.size / 2).coerceAtLeast(0)
            labelView.addData(pos, "item $count")
            count++
        }

        findViewById<Button>(R.id.btn_new_last).setOnClickListener {
            labelView.addData(labelView.size, "item $count")
            count++
        }

        findViewById<Button>(R.id.btn_remove_first).setOnClickListener {
            labelView.removeData(0)
        }

        findViewById<Button>(R.id.btn_remove_mid).setOnClickListener {
            val pos = (labelView.size / 2).coerceAtLeast(0)
            labelView.removeData(pos)
        }

        findViewById<Button>(R.id.btn_remove_last).setOnClickListener {
            labelView.removeData(labelView.size - 1)
        }

        findViewById<Button>(R.id.btn_edit_first).setOnClickListener {
            labelView.editData(0, "修改后的数据")
        }
    }
}