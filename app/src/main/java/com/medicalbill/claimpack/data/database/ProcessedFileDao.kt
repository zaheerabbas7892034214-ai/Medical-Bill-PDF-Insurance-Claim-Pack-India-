package com.medicalbill.claimpack.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedFileDao {
    @Query("SELECT * FROM processed_files ORDER BY processedDate DESC")
    fun getAllFiles(): Flow<List<ProcessedFile>>
    
    @Query("SELECT * FROM processed_files WHERE id = :id")
    suspend fun getFileById(id: Long): ProcessedFile?
    
    @Insert
    suspend fun insert(file: ProcessedFile): Long
    
    @Update
    suspend fun update(file: ProcessedFile)
    
    @Delete
    suspend fun delete(file: ProcessedFile)
    
    @Query("DELETE FROM processed_files")
    suspend fun deleteAll()
}
