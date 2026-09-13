package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.SchoolDao
import com.example.data.dao.SupervisionVisitDao
import com.example.data.dao.SupervisorDao
import com.example.data.dao.TeacherDao
import com.example.data.entity.SchoolEntity
import com.example.data.entity.SupervisionVisitEntity
import com.example.data.entity.SupervisorEntity
import com.example.data.entity.TeacherEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SchoolEntity::class,
        TeacherEntity::class,
        SupervisorEntity::class,
        SupervisionVisitEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao
    abstract fun teacherDao(): TeacherDao
    abstract fun supervisorDao(): SupervisorDao
    abstract fun supervisionVisitDao(): SupervisionVisitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alarish_schools_db"
                )
                    .build()
                INSTANCE = instance
                scope.launch(Dispatchers.IO) {
                    try {
                        if (instance.schoolDao().getCount() == 0) {
                            InitialSeedData.seedDatabase(instance)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                instance
            }
        }
    }
}
