package cn.jninber.filemanager.client

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import cn.jninber.lib.filemanager.ChooserManager
import cn.jninber.lib.filemanager.FileType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val chooser = ChooserManager.Builder()
                .withFileFilter(mutableListOf(FileType.APK, FileType.EXCEL))
                .setOnFileSelectListener { path -> paths.text = path }
                .setOnMultipleSelectListener { pathList: List<String> ->
                    val buffer = StringBuffer()
                    pathList.forEach {
                        buffer.append(it)
                        buffer.append("\n")
                    }
                    paths.text = buffer.toString()

                }.build()


        text.setOnClickListener {
            chooser.show(supportFragmentManager, "this")
        }
    }


}
