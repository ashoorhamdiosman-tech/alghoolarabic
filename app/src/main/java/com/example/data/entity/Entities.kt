package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val classesPrimary: Int = 6,     // فصول 1-3
    val classesUpper: Int = 6,       // فصول 4-6
    val teachersAssistant: Int = 3,  // معلم مساعد / معلم (نصاب 24)
    val teachersFirst: Int = 2,      // معلم أول (نصاب 22)
    val teachersFirstA: Int = 2,     // معلم أول أ (نصاب 20)
    val teachersExpert: Int = 1,     // معلم خبير (نصاب 18)
    val teachersSenior: Int = 1,     // كبير معلمين (نصاب 16)
    val referenceQuota: Int = 24,    // النصاب المرجعي الافتراضي
    
    // بيانات الإدارة المدرسية
    val principalName: String = "",
    val principalPhone: String = "",
    val principalJobTitle: String = "مدير مدرسة",
    val principalDateAssumed: String = "2024/09/01",
    
    val vicePrincipal1Name: String = "",
    val vicePrincipal1Stage: String = "صفوف أولية (1-3)",
    val vicePrincipal1Phone: String = "",
    
    val vicePrincipal2Name: String = "",
    val vicePrincipal2Stage: String = "صفوف عليا (4-6)",
    val vicePrincipal2Phone: String = "",
    
    // الموجه المتابع
    val supervisorId: Long? = null,
    val supervisorName: String = "",
    val supervisorSubject: String = "لغة عربية وتربية دينية",
    val supervisorPhone: String = "",
    val supervisorVisitDays: String = "الأحد - الثلاثاء",
    
    // الموقع الجغرافي
    val latitude: Double? = 31.1321,
    val longitude: Double? = 33.8033,
    val address: String = "العريش - شمال سيناء"
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val schoolId: Long,
    val fullName: String,
    val teacherCode: String,
    val nationalId: String,
    val cadreDegree: String,         // معلم مساعد / معلم، معلم أول، معلم أول أ، معلم خبير، كبير معلمين
    val subject: String,             // لغة عربية / تربية دينية
    val qualification: String,       // المؤهل الدراسي
    val qualificationDate: String,   // تاريخ الحصول عليه
    val phone: String,               // رقم الموبايل
    val address: String,             // العنوان
    val weeklyPeriods: Int           // جدول الحصص الأسبوعية
)

@Entity(tableName = "supervisors")
data class SupervisorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val subject: String = "لغة عربية",
    val phone: String = "",
    val title: String = "موجه مادة"
)

@Entity(tableName = "supervision_visits")
data class SupervisionVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supervisorId: Long,
    val supervisorName: String,
    val schoolId: Long,
    val schoolName: String,
    val visitDate: String,           // YYYY-MM-DD
    val visitTime: String = "08:30 ص",
    val month: String,               // أكتوبر، نوفمبر، ...
    val academicYear: String = "2026/2027",
    val visitType: String = "ميدانية دورية", // دورية / عاجلة / متابعة أنصبة / تقويم فني
    val objective: String = "متابعة أنصبة وسد العجز", // متابعة أنصبة، سد عجز، تقويم فني، مراجعة تقييمات
    val status: String = "SCHEDULED", // SCHEDULED, COMPLETED, DELAYED, POSTPONED
    val delayReason: String = "",
    val notes: String = "",
    val supervisorSignature: String = "",
    val principalSignature: String = "",
    val evidenceDocUri: String = ""
)
