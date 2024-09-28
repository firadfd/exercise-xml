package fd.firad.exercisexml.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: ImageEntity)

    @Query("SELECT * FROM images ORDER BY id DESC LIMIT 1")
    suspend fun getLastImage(): ImageEntity?
}
