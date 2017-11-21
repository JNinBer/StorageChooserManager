package cn.jninber.lib.filemanager

import android.os.Build
import android.os.StatFs
import android.support.annotation.DrawableRes
import java.io.File

/**
 * Created by jninber on 2017/11/9.
 *
 **/

enum class FileType(var extensions: String, @DrawableRes var extensionImg: Int) {
    CONSTANT("txt,log,cev,prop", R.drawable.ic_log),
    VIDEO("mp4,mov,avi,mkv", R.drawable.ic_mov),
    AUDIO("mp3", R.drawable.ic_audio),
    IMAGE("jpeg,jpg,png,gif,svg", R.drawable.ic_pic),
    APK("apk", R.drawable.ic_apk),
    ARCHIVE("zip,rar,gz,tar", R.drawable.ic_zip),
    DOC("doc", R.drawable.ic_doc),
    EXCEL("xls,xlsx", R.drawable.ic_excel),
    PPT("ppt", R.drawable.ic_ppt),
    PDF("pdf", R.drawable.ic_pdf),
    TORRENT("torrent", R.drawable.ic_torrent),
    WEB("html,php,css,js,crdownload", R.drawable.ic_html),
    FOLDER("folder", R.drawable.ic_folder),
    UNKnOWN("", R.drawable.ic_unknown);

    companion object {
        fun getFilType(ext: String?): FileType {
            values().forEach {
                val extensions = it
                extensions.extensions.split(",")?.forEach {
                    if (ext == it) return extensions
                }
            }
            return UNKnOWN
        }
    }


}

val File.extensionImage: Int
    get() {
        return if (this.isDirectory) FileType.FOLDER.extensionImg else FileType.getFilType(this.extension).extensionImg
    }

val File.totalSpaceSize: Long
    get() {
        var st = StatFs(this.path)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            st.blockSizeLong * st.blockCountLong
        } else {
            (st.blockCount * st.blockSize).toLong()
        }
    }
val File.availableSpaceSize: Long
    get() {
        val st = StatFs(this.path)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            st.availableBlocksLong * st.blockSizeLong
        } else {
            (st.availableBlocks * st.blockSize).toLong()
        }
    }

val File.totalSpaceValue: String
    get() {
        var suffix: String? = null
        var totalSpaceSize = this.totalSpaceSize
        if (totalSpaceSize >= 1024) {
            suffix = "KB"
            totalSpaceSize /= 1024
            if (totalSpaceSize >= 1024) {
                suffix = "MB"
                totalSpaceSize /= 1024
                if (totalSpaceSize >= 1024) {
                    suffix = "GB"
                    totalSpaceSize /= 1024
                }
            }
        }
        val resultBuffer = StringBuilder(totalSpaceSize.toString())
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

val File.availableSpaceValue: String
    get() {
        var suffix: String? = null
        var availableSize = this.availableSpaceSize
        if (availableSize >= 1024) {
            suffix = "KB"
            availableSize /= 1024
            if (availableSize >= 1024) {
                suffix = "MB"
                availableSize /= 1024
                if (availableSize >= 1024) {
                    suffix = "GB"
                    availableSize /= 1024
                }
            }
        }
        val resultBuffer = StringBuilder(availableSize.toString())
        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }
        if (suffix != null) resultBuffer.append(suffix)
        return resultBuffer.toString()
    }

