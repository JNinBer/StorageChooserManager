package cn.jninber.lib.filemanager

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.jninber.lib.filemanager.adapter.FileManagerAdapter
import cn.jninber.lib.filemanager.model.Config
import cn.jninber.lib.filemanager.model.Constant
import cn.jninber.lib.filemanager.model.Storage
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_chooser_file.*
import java.io.File
import java.io.FileFilter

/**
 * Created by jninber on 2017/11/13.
 *
 **/
object ChooserManager {


    class Builder() {
        private val config: Config

        init {
            config = Config()
        }

        fun withFileFilter(collection: Collection<FileType>): Builder {
            config.fileFilters = collection
            return this
        }

        fun withMaxNumber(max: Int): Builder {
            config.maxNumber = max
            config.isMultiple = max > 1
            return this
        }

        fun withShowHideFinder(isShow: Boolean): Builder {
            config.isShowHide = isShow
            return this
        }

        fun withCustomFileFilter(fileFilter: FileFilter): Builder {
            config.customFileFilter = fileFilter
            return this
        }

        fun withMultipleType(isMultiple: Boolean): Builder {
            config.isMultiple = isMultiple
            return this
        }

        fun setOnFileSelectListener(selectListener: OnFileSelectListener): Builder {
            config.onFileSelectListener = selectListener
            return this
        }

        fun setOnFileSelectListener(selectListener: (path: String) -> Unit): Builder {
            config.onFileSelectListener = object : OnFileSelectListener {
                override fun onFileSelect(path: String) {
                    selectListener.invoke(path)
                }
            }
            return this
        }

        fun setOnMultipleSelectListener(multipleSelectListener: OnFileMultipleSelectListener): Builder {
            config.onFileMultipleSelectListener = multipleSelectListener
            return this
        }

        fun setOnMultipleSelectListener(multipleSelectListener: (paths: List<String>) -> Unit): Builder {
            config.onFileMultipleSelectListener = object : OnFileMultipleSelectListener {
                override fun onFileMultipleSelect(paths: List<String>) {
                    multipleSelectListener.invoke(paths)
                }
            }
            return this
        }

        fun build(): ChooserFileFragment {
            return ChooserFileFragment.newInstance(config)
        }
    }

    class ChooserFileFragment @SuppressLint("ValidFragment")
    internal constructor() : DialogFragment(), DialogInterface.OnKeyListener {
        override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
            return if (keyCode == KeyEvent.KEYCODE_BACK && KeyEvent.ACTION_UP == event?.action) {
                onBackPressed()
                true // pretend we've processed it
            } else
                false
        }

        private lateinit var adapter: FileManagerAdapter
        private var mutiple = mutableListOf<String>()
        private var config: Config? = null
        private val internalStoragePath = Environment.getExternalStorageDirectory().absolutePath
        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog?.setOnKeyListener(this)
            return inflater!!.inflate(R.layout.fragment_chooser_file, container, false)
        }

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            config = arguments.getSerializable(ARGUMENTS_EXTRA) as Config?
            adapter = FileManagerAdapter(config?.maxNumber ?: Int.MAX_VALUE, config?.isMultiple)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val rxPermissions = RxPermissions(activity)
                rxPermissions.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).subscribe { permission ->
                    when {
                        permission.granted -> initStorageList(internalStoragePath)
                        permission.shouldShowRequestPermissionRationale -> Toast.makeText(activity, getString(R.string.permission_tips), Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(activity, getString(R.string.permission_tips), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                initStorageList(internalStoragePath)
            }
            mRecyclerView.layoutManager = LinearLayoutManager(this.activity)
            mRecyclerView.adapter = adapter
            adapter.onChangeListener = object : FileManagerAdapter.OnChangeListener {
                override fun onChange(path: String, isAdd: Boolean) {
                    if (isAdd) {
                        if (!mutiple.contains(path)) {
                            mutiple.add(path)
                        }
                    } else {
                        if (mutiple.contains(path)) {
                            mutiple.remove(path)
                        }
                    }
                }

                override fun onSelect(path: File) {
                    if (path.isDirectory) {
                        initStorageList(path.absolutePath)
                    } else {
                        config?.onFileSelectListener?.onFileSelect(path.path)
                        dismissAllowingStateLoss()
                    }
                }
            }

            back_button.setOnClickListener {
                onBackPressed()
            }
            mLoading.setOnClickListener { }
            select_button.visibility = if (config?.isMultiple == true) View.VISIBLE else View.GONE
            select_button.setOnClickListener {
                if (mutiple.isEmpty()) {
                    Toast.makeText(this.activity, "no chooser ", Toast.LENGTH_SHORT).show()
                } else {
                    config?.onFileMultipleSelectListener?.onFileMultipleSelect(mutiple)
                    adapter.getPaths().clear()
                    dismissAllowingStateLoss()
                }
            }
        }

        private fun onBackPressed() {
            var path = path_chosen.text.toString()
            if (path.split("/").size > 2) {
                path = path.substring(0, path.lastIndexOf("/"))
                initStorageList(path)
            } else {
                dismissAllowingStateLoss()
            }
        }

        fun setConfig(config: Config) {
            this.config = config
        }


        private fun initStorageList(thisPath: String?) {
            path_chosen.text = thisPath
            mLoading?.visibility = View.VISIBLE
            Observable.create<Array<File>> { sub ->
                val listFiles =
                        when {
                            config?.customFileFilter != null -> File(thisPath).listFiles(config?.customFileFilter)
                            config?.fileFilters != null -> File(thisPath).listFiles(fileFilter)
                            else -> File(thisPath).listFiles()
                        }
                if (listFiles != null) sub.onNext(listFiles) else sub.onError(RuntimeException("listFiles is Null"))

            }.map { list ->
                val listStorage = mutableListOf<Storage>()
                list.forEach {
                    if (it.name != Constant.SELF_DIR_NAME
                            && it.name != Constant.EMULATED_DIR_KNOX
                            && it.name != Constant.EMULATED_DIR_NAME
                            && it.name != Constant.SDCARD0_DIR_NAME
                            && it.name != Constant.CONTAINER) {
                        if (config?.isShowHide == true) {
                            listStorage.add(Storage(it))
                        } else {
                            if (!it.name.startsWith(".")) {
                                listStorage.add(Storage(it))
                            }
                        }
                    }
                }
                listStorage
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    {
                        mLoading?.visibility = View.GONE
                        adapter.addAll(it)
                    }, {
                mLoading?.visibility = View.GONE
                adapter.addAll(mutableListOf())
            }
            )
        }

        private val fileFilter = FileFilter { pathname ->
            if (pathname?.isDirectory == true) {
                findInDirectory(pathname)
            } else {
                fileFilter(pathname)
            }
        }


        private fun fileFilter(pathname: File?): Boolean {
            return FileType.getFilType(pathname?.extension) in config?.fileFilters!!
        }

        private fun findInDirectory(dir: File): Boolean {
            val sub = ArrayList<File>()
            val indexInList = dir.listFiles { file ->
                if (file.isFile) {
                    if (".nomedia" == file.name) false else fileFilter(file)
                } else if (file.isDirectory) {
                    sub.add(file)
                    false
                } else
                    false
            }?.size ?: 0
            if (indexInList > 0) {
                Log.i("", "findInDirectory => " + dir.name + " return true for => " + indexInList)
                return true
            }

            for (subDirectory in sub) {
                if (findInDirectory(subDirectory)) {
                    Log.i("", "findInDirectory => " + subDirectory.toString())
                    return true
                }
            }
            return false

        }


        override fun onStart() {
            super.onStart()
            if (dialog != null) {
                val win = dialog.window
                val dm = DisplayMetrics()
                activity.windowManager.defaultDisplay.getMetrics(dm)
                val params = win.attributes
                params.width = (dm.widthPixels * 0.9).toInt()
                params.height = (dm.heightPixels * 0.9).toInt()
                win.attributes = params
            }

        }

        companion object {

            const val ARGUMENTS_EXTRA = "Config"
            internal fun newInstance(config: Config): ChooserManager.ChooserFileFragment {
                var fragment = ChooserFileFragment()
                val argments = Bundle()
                argments.putSerializable(ARGUMENTS_EXTRA, config)
                fragment.arguments = argments
                return fragment
            }
        }

    }

    interface OnFileSelectListener {
        fun onFileSelect(path: String)
    }

    interface OnFileMultipleSelectListener {
        fun onFileMultipleSelect(paths: List<String>)
    }

}