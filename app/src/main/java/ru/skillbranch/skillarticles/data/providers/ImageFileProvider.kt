package ru.skillbranch.skillarticles.data.providers

import android.net.Uri
import androidx.core.content.FileProvider

class ImageFileProvider : FileProvider() {
    override fun getType(uri: Uri): String? =
        "image/jpeg" // принудительно выставляем тип для изображений по кешированному uri (которые имеют расширение .0 ... не jpeg)
}