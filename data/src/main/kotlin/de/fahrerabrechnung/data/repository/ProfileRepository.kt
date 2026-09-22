package de.fahrerabrechnung.data.repository

import de.fahrerabrechnung.data.db.ProfileDao
import de.fahrerabrechnung.data.db.ProfileEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ProfileRepository @Inject constructor(private val dao: ProfileDao) {
    fun observeProfiles(): Flow<List<ProfileEntity>> = dao.observeAll()
    suspend fun get(id: String): ProfileEntity? = dao.getById(id)
    suspend fun insert(entity: ProfileEntity) = dao.insert(entity)
}
