package cn.jninber.lib.filemanager.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import cn.jninber.lib.filemanager.*
import cn.jninber.lib.filemanager.model.Storage
import kotlinx.android.synthetic.main.view_holder_item_folder.view.*
import java.io.File


/**
 * Created by jninber on 2017/11/9.
 *
 **/
class FileManagerAdapter(var max: Int = Int.MAX_VALUE
                         , var isMultiple: Boolean? = false) : RecyclerView.Adapter<FileManagerAdapter.FileManagerViewHolder>() {

    val handler: Handler
    val onResult: OnResultImage
    var onChangeListener: OnChangeListener? = null


    companion object {
        private var paths = hashSetOf<String>()
    }

    init {
        handler = Handler(Looper.getMainLooper())
        onResult = object : OnResultImage {
            override fun onResult(imageView: ImageView?, drawable: Drawable?) {
                handler.post {
                    if (drawable != null)
                        imageView?.setImageDrawable(drawable) else imageView?.setImageResource(FileType.APK.extensionImg)
                }
            }
        }
    }

    private var files = mutableListOf<Storage>()

    fun getPaths() = paths

    override fun onBindViewHolder(holder: FileManagerViewHolder?, position: Int) {
        val file = getFileItem(position)
        holder?.onBindFile(file)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FileManagerViewHolder {
        val viewLayout = LayoutInflater.from(parent?.context).inflate(R.layout.view_holder_item_folder, parent, false)
        return FileManagerViewHolder(max, isMultiple, viewLayout, onResult, onChangeListener)
    }

    override fun getItemCount() = files.size

    fun getFileItem(position: Int): File {
        return File(getItem(position).storagePath)
    }

    fun getItem(position: Int): Storage {
        return files.get(position)
    }

    fun addAll(files: Collection<Storage>) {
        this.files.clear()
        this.files.addAll(files)
        notifyDataSetChanged()

    }


    class FileManagerViewHolder(var max: Int = Int.MAX_VALUE
                                , var isMultiple: Boolean? = false, var item: View, val onResultImage: OnResultImage, val onChangeListener: OnChangeListener?) : RecyclerView.ViewHolder(item) {
        fun onBindFile(file: File) {
            itemView.path_folder_icon.tag = file.path
            itemView.checkBox.visibility = if (isMultiple == true && file.isFile) View.VISIBLE else View.GONE
            if (isMultiple == true) {
                Log.d("paths", "path is checked=${paths.contains(file.path)} =${file.path in paths}")
                itemView.checkBox.isChecked = paths.contains(file.path)
            }
            if (file.isDirectory) {
                itemView.path_folder_icon.setImageResource(R.drawable.ic_folder)
            } else if (file.isFile) {
                if (FileType.getFilType(file.extension) == FileType.APK) {
                    itemView.path_folder_icon.setImageResource(file.extensionImage)
                    Thread {
                        val apkIcon = getApkIcon(itemView.context, file.path)
                        onResultImage.onResult(itemView.findViewWithTag<ImageView>(file.path), apkIcon)
                    }.start()
                } else {
                    itemView.path_folder_icon.setImageResource(file.extensionImage)
                }
            }
            itemView.checkBox.setOnClickListener {
                val isChecked = itemView.checkBox.isChecked
                val isMax = paths.size < max
                onChangeListener?.onChange(file.path, isChecked)
                Log.d("操作前 paths", "./gradlew app:dependenciessize=${paths.size} ")
                if (isChecked) {
                    Log.d("paths=action add", "path＝＝＝＝${file.path},name=${file.name} ")
                    if (isMax) {
                        paths.add(file.path)
                    } else {
                        itemView.checkBox.isChecked = !isChecked
                        Toast.makeText(itemView.context, "你最多可以选择${max}个", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    paths.remove(file.path)
                    Log.d("paths=action remove", "path＝＝＝＝${file.path},name=${file.name} ")
                }

                Log.d("操作后 paths=", "size=${paths.size} ")

            }
            itemView.storage_size.visibility = if (file.isFile) View.GONE else View.VISIBLE
            Log.d("FileManagerViewHolder", "path=${file.path} ,name=${file.name} ")
            itemView.storage_name.text = file.name
            itemView.storage_size.max = file.totalSpaceSize.toInt()
            itemView.storage_size.progress = (file.totalSpaceSize - file.availableSpaceSize).toInt()


            if (isMultiple == true) {
                itemView.item.setOnClickListener {
                    if (!file.isFile)
                        onChangeListener?.onSelect(file)
                }
            } else {
                itemView.item.setOnClickListener {
                    onChangeListener?.onSelect(file)
                }
            }

        }

        private fun getApkIcon(context: Context, apkPath: String): Drawable? {
            val pm = context.packageManager
            val info = pm.getPackageArchiveInfo(apkPath,
                    PackageManager.GET_ACTIVITIES)
            if (info != null) {
                val appInfo = info.applicationInfo
                appInfo.sourceDir = apkPath
                appInfo.publicSourceDir = apkPath
                try {
                    return appInfo.loadIcon(pm)
                } catch (e: OutOfMemoryError) {
                    Log.e("ApkIconLoader", e.toString())
                }

            }
            return null
        }

    }

    interface OnResultImage {
        fun onResult(imageView: ImageView?, drawable: Drawable?)
    }

    interface OnChangeListener {
        fun onChange(path: String, isAdd: Boolean)
        fun onSelect(path: File)
    }
}

