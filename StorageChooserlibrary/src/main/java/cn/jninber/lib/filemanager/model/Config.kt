package cn.jninber.lib.filemanager.model

import cn.jninber.lib.filemanager.ChooserManager
import cn.jninber.lib.filemanager.adapter.FileManagerAdapter
import cn.jninber.lib.filemanager.FileType
import java.io.FileFilter
import java.io.Serializable

/**
 * Created by jninber on 2017/11/15.
 *
 **/
class Config() : Serializable {
    var onFileSelectListener: ChooserManager.OnFileSelectListener? = null
    var onFileMultipleSelectListener: ChooserManager.OnFileMultipleSelectListener? = null
    var isShowHide: Boolean = false
    var fileFilters: Collection<FileType>? = null
    var customFileFilter: FileFilter? = null
    var maxNumber: Int = Int.MAX_VALUE
    var isMultiple = false
}