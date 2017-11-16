package cn.jninber.lib.filemanager.model

import android.support.annotation.DrawableRes
import cn.jninber.lib.filemanager.*
import java.io.File

/**
 * A model that is used as the element to store any data regarding storage volumes/disks
 */
class Storage {
    var storageTitle: String? = null
    var storagePath: String? = null
    var memoryTotalSize: String? = null
    var memoryAvailableSize: String? = null
    var memoryTotalSizeLong: Long = 0
    var memoryAvailableLong: Long = 0
    var isFile: Boolean = false
    var ext: String = ""
    @DrawableRes
    var extImage: Int = -1

    constructor()

    constructor(file: File) : this() {
        this.storagePath = file.absolutePath
        this.storageTitle = file.name
        this.memoryTotalSize = file.totalSpaceValue
        this.memoryAvailableSize = file.availableSpaceValue
        this.memoryTotalSizeLong = file.totalSpaceSize
        this.memoryAvailableLong = file.availableSpaceSize
        this.isFile = file.isFile
        this.ext = file.extension
        this.extImage = file.extensionImage
    }

}
