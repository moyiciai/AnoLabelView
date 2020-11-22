package cn.moyiciai.anolabelview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import cn.moyiciai.lib.AnoLabelView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn).setOnClickListener {
            val data = mutableListOf<String>()
            for (i in 0..20) {
                data.add("item$i")
            }

            val labelView = findViewById<AnoLabelView>(R.id.label_view)
            labelView.run {
                setData(data)
//                onCheckChangeListener = object : AnoLabelView.OnCheckedChangeListener {
//                    override fun onCheckedChanged(labelView: TextView, isChecked: Boolean) {
//
//                    }
//                }
//                onCheckChangeListener = object : AnoLabelView.OnCheckedChangeListener() {
//
//                }
            }
        }
    }
}