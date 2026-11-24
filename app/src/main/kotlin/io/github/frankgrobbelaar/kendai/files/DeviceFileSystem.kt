package io.github.frankgrobbelaar.kendai.files

import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.DirectoryIteratorException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.name
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** The device's own storage, read with `java.nio` under All files access. */
class DeviceFileSystem(private val io: CoroutineDispatcher = Dispatchers.IO) : FileSystem {

    override suspend fun list(path: String): List<Entry> =
        withContext(io) {
            mapErrors(path) {
                Files.newDirectoryStream(Paths.get(path)).use { children ->
                    // A big folder takes a while: stop as soon as nobody wants the listing.
                    children.mapNotNull { child ->
                        ensureActive()
                        entryOf(child)
                    }
                }
            }
        }

    /** The entry for [path]; null for one that's gone, as a file deleted mid-listing is. */
    private fun entryOf(path: Path): Entry? =
        try {
            val attributes =
                Files.readAttributes(
                    path,
                    BasicFileAttributes::class.java,
                    LinkOption.NOFOLLOW_LINKS,
                )
            Entry(
                name = path.name,
                path = path.invariantSeparatorsPathString,
                isFolder = attributes.isDirectory,
                size = if (attributes.isDirectory) 0 else attributes.size(),
                modified = attributes.lastModifiedTime().toMillis(),
            )
        } catch (_: NoSuchFileException) {
            null
        }

    private inline fun <T> mapErrors(path: String, block: () -> T): T =
        try {
            block()
        } catch (_: AccessDeniedException) {
            throw FileSystemException.AccessDenied(path)
        } catch (_: NoSuchFileException) {
            throw FileSystemException.NotFound(path)
        } catch (e: NotDirectoryException) {
            throw FileSystemException.Unreadable(path, e)
        } catch (e: DirectoryIteratorException) {
            // A listing that fails part-way, as when an SD card is taken out mid-walk.
            throw FileSystemException.Unreadable(path, e)
        } catch (e: IOException) {
            throw FileSystemException.Unreadable(path, e)
        } catch (_: SecurityException) {
            throw FileSystemException.AccessDenied(path)
        }
}
