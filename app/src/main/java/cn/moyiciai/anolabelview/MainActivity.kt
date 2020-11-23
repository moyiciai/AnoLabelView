package cn.moyiciai.anolabelview

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.moyiciai.lib.AnoLabelView
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val labelView = findViewById<AnoLabelView>(R.id.label_view)
        val tvInfo = findViewById<TextView>(R.id.tv_info)

        findViewById<Button>(R.id.btn_show_label).setOnClickListener {
            val data = mutableListOf<String>()
            for (i in 0..20) {
                data.add("item $i")
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
                        Log.d("dx", "onClick=$data")
                    }
                })
            }
        }

        findViewById<Button>(R.id.btn_get_result).setOnClickListener {
            val data = labelView.getCheckedData<String>()
            tvInfo.text = "${data.size}"
        }
    }
}