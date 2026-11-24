package io.github.frankgrobbelaar.kendai.files

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.core.net.toUri

/** Whether kendai has All files access, so it can read books wherever they are. */
fun interface StorageAccess {
    fun granted(): Boolean
}

/** All files access as Android grants it. */
val DeviceStorageAccess = StorageAccess { Environment.isExternalStorageManager() }

/** The top of the device's shared storage, where suggested folders are looked for. */
fun deviceStorageRoot(): String = Environment.getExternalStorageDirectory().path

/** Opens Android's All files access setting: kendai's own page, or the list where it's missing. */
fun openAllFilesAccess(context: Context) {
    val ownPage =
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            "package:${context.packageName}".toUri(),
        )
    try {
        context.startActivity(ownPage)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
    }
}

/**
 * The path of a folder picked with Android's folder picker, for reading it with All files access:
 * `primary:Books` is the Books folder at the top of the device's storage, `1234-ABCD:Comics` the
 * Comics folder on that SD card. Null for a folder that isn't on the device, such as a cloud one.
 */
fun pathOfPicked(tree: Uri, root: String = deviceStorageRoot()): String? {
    if (tree.authority != EXTERNAL_STORAGE) return null
    val id = DocumentsContract.getTreeDocumentId(tree)
    val volume = id.substringBefore(':')
    val inside = id.substringAfter(':', "").trim('/')
    val top = if (volume == "primary") root else "/storage/$volume"
    return if (inside.isEmpty()) top else "$top/$inside"
}

private const val EXTERNAL_STORAGE = "com.android.externalstorage.documents"
