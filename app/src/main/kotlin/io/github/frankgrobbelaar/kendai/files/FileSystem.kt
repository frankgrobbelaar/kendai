package io.github.frankgrobbelaar.kendai.files

/** A file or folder, as a file system lists it. */
data class Entry(
    val name: String,
    val path: String,
    val isFolder: Boolean,
    val size: Long,
    /** When it last changed, in milliseconds since the epoch. */
    val modified: Long,
)

/**
 * Where books live: the device's storage now, and SMB and SFTP servers later. The library finds
 * books through it; nothing above the library touches files directly.
 */
interface FileSystem {
    /** What's in the folder at [path]. */
    suspend fun list(path: String): List<Entry>
}

/** What went wrong reaching a file or folder, in terms the UI can put into words. */
sealed class FileSystemException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {
    /** Kendai isn't allowed in: no All files access, or a folder kept closed. */
    class AccessDenied(path: String) : FileSystemException("No access to $path")

    /** Nothing at that path. */
    class NotFound(path: String) : FileSystemException("Nothing at $path")

    /** It's there, but couldn't be read. */
    class Unreadable(path: String, cause: Throwable? = null) :
        FileSystemException("Couldn't read $path", cause)
}

/** The last part of [path]: its file or folder name. */
fun nameOf(path: String): String = path.trimEnd('/').substringAfterLast('/')

/** The folder [path] is in. */
fun parentOf(path: String): String = path.trimEnd('/').substringBeforeLast('/', "")
