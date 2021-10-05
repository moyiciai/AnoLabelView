package cn.moyiciai.anolabelview

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_test -> start(TestActivity::class.java)
        }
    }

    private fun <T> start(clz: Class<T>) {
        startActivity(Intent(this, clz))
    }
}