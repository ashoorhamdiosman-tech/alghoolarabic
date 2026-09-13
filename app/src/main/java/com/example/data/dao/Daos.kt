package com.example.data.dao

import androidx.room.*
import com.example.data.entity.SchoolEntity
import com.example.data.entity.SupervisionVisitEntity
import com.example.data.entity.SupervisorEntity
import com.example.data.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools ORDER BY id ASC")
    fun getAllSchools(): Flow<List<SchoolEntity>>

    @Query("SELECT * FROM schools WHERE id = :id")
    suspend fun getSchoolById(id: Long): SchoolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: SchoolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schools: List<SchoolEntity>)

    @Update
    suspend fun updateSchool(school: SchoolEntity)

    @Delete
    suspend fun deleteSchool(school: SchoolEntity)

    @Query("SELECT COUNT(*) FROM schools")
    suspend fun getCount(): Int
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY id DESC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE schoolId = :schoolId ORDER BY id ASC")
    fun getTeachersBySchool(schoolId: Long): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(teachers: List<TeacherEntity>)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherEntity)

    @Query("UPDATE teachers SET schoolId = :targetSchoolId WHERE id = :teacherId")
    suspend fun transferTeacher(teacherId: Long, targetSchoolId: Long)
}

@Dao
interface SupervisorDao {
    @Query("SELECT * FROM supervisors ORDER BY id ASC")
    fun getAllSupervisors(): Flow<List<SupervisorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupervisor(supervisor: SupervisorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(supervisors: List<SupervisorEntity>)

    @Update
    suspend fun updateSupervisor(supervisor: SupervisorEntity)

    @Delete
    suspend fun deleteSupervisor(supervisor: SupervisorEntity)

    @Query("SELECT COUNT(*) FROM supervisors")
    suspend fun getCount(): Int
}

@Dao
interface SupervisionVisitDao {
    @Query("SELECT * FROM supervision_visits ORDER BY visitDate ASC")
    fun getAllVisits(): Flow<List<SupervisionVisitEntity>>

    @Query("SELECT * FROM supervision_visits WHERE supervisorId = :supervisorId ORDER BY visitDate ASC")
    fun getVisitsBySupervisor(supervisorId: Long): Flow<List<SupervisionVisitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(visit: SupervisionVisitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(visits: List<SupervisionVisitEntity>)

    @Update
    suspend fun updateVisit(visit: SupervisionVisitEntity)

    @Delete
    suspend fun deleteVisit(visit: SupervisionVisitEntity)

    @Query("UPDATE supervision_visits SET status = :status, delayReason = :reason WHERE id = :visitId")
    suspend fun updateStatusAndReason(visitId: Long, status: String, reason: String)

    @Query("UPDATE supervision_visits SET visitDate = :newDate, status = 'SCHEDULED' WHERE id = :visitId")
    suspend fun rescheduleVisit(visitId: Long, newDate: String)
}
