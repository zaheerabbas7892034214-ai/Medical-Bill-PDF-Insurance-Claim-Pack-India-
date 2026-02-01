package com.medicalbill.claimpack.data.repository

import com.medicalbill.claimpack.data.database.ProcessedFile
import com.medicalbill.claimpack.data.database.ProcessedFileDao
import kotlinx.coroutines.flow.Flow

class FileRepository(private val fileDao: ProcessedFileDao) {
    
    val allFiles: Flow<List<ProcessedFile>> = fileDao.getAllFiles()
    
    suspend fun getFileById(id: Long): ProcessedFile? {
        return fileDao.getFileById(id)
    }
    
    suspend fun insertFile(file: ProcessedFile): Long {
        return fileDao.insert(file)
    }
    
    suspend fun updateFile(file: ProcessedFile) {
        fileDao.update(file)
    }
    
    suspend fun deleteFile(file: ProcessedFile) {
        fileDao.delete(file)
    }
    
    suspend fun deleteAllFiles() {
        fileDao.deleteAll()
    }
}
