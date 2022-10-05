package com.example.realstateapp.utils

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap

class GetImageExtension {
    companion object{
        fun getFileExtension(activity: Activity,imageUri: Uri): String? {
            if (activity != null) {
                val cr: ContentResolver? = activity.contentResolver
                val mime: MimeTypeMap = MimeTypeMap.getSingleton()
                return mime.getExtensionFromMimeType(cr?.getType(imageUri))
            }
            return null
        }
    }
}