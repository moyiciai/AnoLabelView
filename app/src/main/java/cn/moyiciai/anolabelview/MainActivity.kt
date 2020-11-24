package cn.moyiciai.anolabelview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.moyiciai.lib.AnoLabelView

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var labelView: AnoLabelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        labelView = findViewById(R.id.label_view)

        initControlView()

        findViewById<Button>(R.id.btn_show_label).setOnClickListener {
            val data = mutableListOf<String>()
            for (i in 0..20) {
                if (i == 5) {
                    data.add("不可点击")
                } else {
                    data.add("item $i")
                }
            }

            labelView.run {
                setData(data)
                setOnCheckedChangeListener(object : AnoLabelView.OnCheckedChangeListener {
                    override fun onCheckedChanged(view: TextView, data: Any, isChecked: Boolean) {
                        Log.d("dx", "${data as String}, isChecked=$isChecked")
                    }
                })
                setOnLabelClickListener(object : AnoLabelView.OnLabelClickListener {
                    override fun onLabelClick(view: TextView, data: Any, position: Int) {
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
                setOnCheckedChangeInterceptor(object : AnoLabelView.OnCheckedChangeInterceptor {
                    override fun onCheckedChangeIntercept(
                        view: TextView,
                        data: Any,
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
    }
}