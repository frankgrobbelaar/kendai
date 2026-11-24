package io.github.frankgrobbelaar.kendai.library

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** A book, known by its fingerprint wherever its files are. Kept when its files go. */
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val fingerprint: String,
    val title: String,
    val author: String,
    val series: String,
    val seriesIndex: Double?,
    val format: BookFormat,
    val kind: BookKind,
    val size: Long,
    /** When it was first found, in milliseconds since the epoch. */
    val addedAt: Long,
    val hasCover: Boolean,
    /** A plain-text guide's first lines, for its drawn cover. */
    val excerpt: String,
)

/** Where one of a book's files is, and the library folder it was found in. */
@Entity(tableName = "copies", indices = [Index("fingerprint")])
data class CopyEntity(
    @PrimaryKey val path: String,
    val fingerprint: String,
    val folder: String,
    val size: Long,
    val modified: Long,
)

/** A folder the user added to the library. */
@Entity(tableName = "folders")
data class FolderEntity(@PrimaryKey val path: String, val addedAt: Long)

/** A book, with where one of its files is. */
data class ShelvedBook(@Embedded val book: BookEntity, val path: String)

@Dao
abstract class LibraryDao {
    @Query("SELECT path FROM folders ORDER BY path") abstract fun folders(): Flow<List<String>>

    @Query("SELECT path FROM folders ORDER BY path") abstract suspend fun folderList(): List<String>

    /** Adds [added] to the library's folders and takes [gone] out, together. */
    @Transaction
    open suspend fun changeFolders(gone: List<String>, added: List<FolderEntity>) {
        removeFolders(gone)
        addFolders(added)
    }

    /** Takes [folder] out of the library, with the files found in it. */
    @Transaction
    open suspend fun removeFolder(folder: String) {
        removeFolders(listOf(folder))
        removeCopiesIn(folder)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun addFolders(folders: List<FolderEntity>)

    @Query("DELETE FROM folders WHERE path IN (:paths)")
    protected abstract suspend fun removeFolders(paths: List<String>)

    @Query("DELETE FROM copies WHERE folder = :folder")
    protected abstract suspend fun removeCopiesIn(folder: String)

    @Query("SELECT * FROM copies") abstract suspend fun copies(): List<CopyEntity>

    @Query("SELECT fingerprint FROM books") abstract suspend fun fingerprints(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE) abstract suspend fun addBook(book: BookEntity)

    @Upsert abstract suspend fun putCopy(copy: CopyEntity)

    @Query("DELETE FROM copies WHERE path IN (:paths)")
    abstract suspend fun removeCopies(paths: List<String>)

    /** The books found most recently in the library's folders, each once, with one of its files. */
    @Query(
        "SELECT b.*, MIN(c.path) AS path FROM copies c " +
            "JOIN books b ON b.fingerprint = c.fingerprint JOIN folders f ON f.path = c.folder " +
            "GROUP BY b.fingerprint ORDER BY b.addedAt DESC, b.title LIMIT :limit"
    )
    abstract fun recentlyAdded(limit: Int): Flow<List<ShelvedBook>>

    /** Every file of every book, for the folder view. */
    @Query("SELECT b.*, c.path AS path FROM copies c JOIN books b ON b.fingerprint = c.fingerprint")
    abstract fun shelved(): Flow<List<ShelvedBook>>
}

@Database(entities = [BookEntity::class, CopyEntity::class, FolderEntity::class], version = 1)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun library(): LibraryDao
}
