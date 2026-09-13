package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.SchoolEntity
import com.example.data.entity.SupervisionVisitEntity
import com.example.data.entity.SupervisorEntity
import com.example.data.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(private val database: AppDatabase) {

    // Schools
    val allSchools: Flow<List<SchoolEntity>> = database.schoolDao().getAllSchools()

    suspend fun getSchoolById(id: Long): SchoolEntity? = database.schoolDao().getSchoolById(id)

    suspend fun updateSchool(school: SchoolEntity) = database.schoolDao().updateSchool(school)

    suspend fun insertSchool(school: SchoolEntity): Long = database.schoolDao().insertSchool(school)

    suspend fun deleteSchool(school: SchoolEntity) = database.schoolDao().deleteSchool(school)

    // Teachers
    val allTeachers: Flow<List<TeacherEntity>> = database.teacherDao().getAllTeachers()

    fun getTeachersBySchool(schoolId: Long): Flow<List<TeacherEntity>> =
        database.teacherDao().getTeachersBySchool(schoolId)

    suspend fun insertTeacher(teacher: TeacherEntity): Long = database.teacherDao().insertTeacher(teacher)

    suspend fun updateTeacher(teacher: TeacherEntity) = database.teacherDao().updateTeacher(teacher)

    suspend fun deleteTeacher(teacher: TeacherEntity) = database.teacherDao().deleteTeacher(teacher)

    suspend fun transferTeacher(teacherId: Long, targetSchoolId: Long) =
        database.teacherDao().transferTeacher(teacherId, targetSchoolId)

    // Supervisors
    val allSupervisors: Flow<List<SupervisorEntity>> = database.supervisorDao().getAllSupervisors()

    suspend fun insertSupervisor(supervisor: SupervisorEntity): Long =
        database.supervisorDao().insertSupervisor(supervisor)

    suspend fun updateSupervisor(supervisor: SupervisorEntity) =
        database.supervisorDao().updateSupervisor(supervisor)

    suspend fun deleteSupervisor(supervisor: SupervisorEntity) =
        database.supervisorDao().deleteSupervisor(supervisor)

    // Visits
    val allVisits: Flow<List<SupervisionVisitEntity>> = database.supervisionVisitDao().getAllVisits()

    fun getVisitsBySupervisor(supervisorId: Long): Flow<List<SupervisionVisitEntity>> =
        database.supervisionVisitDao().getVisitsBySupervisor(supervisorId)

    suspend fun insertVisit(visit: SupervisionVisitEntity): Long =
        database.supervisionVisitDao().insertVisit(visit)

    suspend fun updateVisit(visit: SupervisionVisitEntity) =
        database.supervisionVisitDao().updateVisit(visit)

    suspend fun deleteVisit(visit: SupervisionVisitEntity) =
        database.supervisionVisitDao().deleteVisit(visit)

    suspend fun updateVisitStatusAndReason(visitId: Long, status: String, reason: String) =
        database.supervisionVisitDao().updateStatusAndReason(visitId, status, reason)

    suspend fun rescheduleVisit(visitId: Long, newDate: String) =
        database.supervisionVisitDao().rescheduleVisit(visitId, newDate)
}
