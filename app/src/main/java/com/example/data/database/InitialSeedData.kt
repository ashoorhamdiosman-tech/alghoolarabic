package com.example.data.database

import com.example.data.entity.SchoolEntity
import com.example.data.entity.SupervisionVisitEntity
import com.example.data.entity.SupervisorEntity
import com.example.data.entity.TeacherEntity

object InitialSeedData {

    suspend fun seedDatabase(database: AppDatabase) {
        val schoolDao = database.schoolDao()
        val supervisorDao = database.supervisorDao()
        val teacherDao = database.teacherDao()
        val visitDao = database.supervisionVisitDao()

        // 1. Initial Supervisors
        val supervisors = listOf(
            SupervisorEntity(1, "أ. محمود عبد الرحمن سالم", "لغة عربية", "01012345671", "موجه أول لغة عربية"),
            SupervisorEntity(2, "أ. خالد إبراهيم الشوربجي", "لغة عربية", "01098765432", "موجه لغة عربية"),
            SupervisorEntity(3, "أ. سمير فتحي النجار", "تربية دينية إسلامية", "01123456783", "موجه تربية دينية"),
            SupervisorEntity(4, "أ. أحمد سليمان الحجاوي", "لغة عربية", "01234567894", "موجه لغة عربية"),
            SupervisorEntity(5, "أ. ياسر محمد الكاشف", "لغة عربية", "01065432195", "موجه لغة عربية"),
            SupervisorEntity(6, "أ. هاني عطية الفيروز", "تربية دينية إسلامية", "01187654326", "موجه تربية دينية")
        )
        supervisorDao.insertAll(supervisors)

        // 2. 65 Schools of Al-Arish (الـ 65 مدرسة بإدارة العريش التعليمية)
        val schoolNames = listOf(
            "مدرسة العريش الابتدائية المشتركة",
            "مدرسة الشهيد الرائد محمد الزملوط",
            "مدرسة الشهيد علاء الدين الابتدائية",
            "مدرسة أبي صقل الابتدائية بنين",
            "مدرسة أبي صقل الابتدائية بنات",
            "مدرسة الريسة الابتدائية المشتركة",
            "مدرسة السيدة خديجة الابتدائية بنات",
            "مدرسة فاطمة الزهراء الابتدائية",
            "مدرسة ضاحية السلام الابتدائية",
            "مدرسة السالمية الابتدائية المشتركة",
            "مدرسة رفاعة الطهطاوي الابتدائية",
            "مدرسة طه حسين الابتدائية بنين",
            "مدرسة الشهيد النقيب محمود صلاح",
            "مدرسة اللغات الرسمية التجريبية بالعريش",
            "مدرسة الشهيد العقيد أحمد منسي",
            "مدرسة الصفا الابتدائية المشتركة",
            "مدرسة ابن سينا الابتدائية بنين",
            "مدرسة عمرو بن العاص الابتدائية",
            "مدرسة الشهيد أحمد عسكر بنين",
            "مدرسة الأمل للصم وضعاف السمع بالعريش",
            "مدرسة الكوثر الابتدائية المشتركة",
            "مدرسة النصر الابتدائية بنات",
            "مدرسة علي بن أبي طالب الابتدائية",
            "مدرسة الشهيد شريف محمد عمر",
            "مدرسة السلام الرسمية لغات",
            "مدرسة الحرية الابتدائية المشتركة",
            "مدرسة السكاسكة الابتدائية",
            "مدرسة الميدان الابتدائية المشتركة",
            "مدرسة العبور الابتدائية بنين",
            "مدرسة الزهور الابتدائية بنين",
            "مدرسة الزهور الابتدائية بنات",
            "مدرسة عاطف السادات الابتدائية",
            "مدرسة أحمد عرابي الابتدائية",
            "مدرسة الشهيد هشام شاهين",
            "مدرسة الفاتح الابتدائية المشتركة",
            "مدرسة الخلفاء الراشدين الابتدائية",
            "مدرسة سعد زغلول الابتدائية",
            "مدرسة الوادي الابتدائية المشتركة",
            "مدرسة كرم أبو نجيلة الابتدائية",
            "مدرسة السمران الابتدائية المشتركة",
            "مدرسة البطل أحمد عبد العزيز",
            "مدرسة الأندلس الابتدائية بنين",
            "مدرسة اليرموك الابتدائية بنات",
            "مدرسة حطين الابتدائية المشتركة",
            "مدرسة القادسية الابتدائية بنين",
            "مدرسة المستقبل التجريبية للغات",
            "مدرسة أم المؤمنين الابتدائية بنات",
            "مدرسة صلاح الدين الابتدائية المشتركة",
            "مدرسة طارق بن زياد الابتدائية",
            "مدرسة عمر بن الخطاب الابتدائية",
            "مدرسة خالد بن الوليد الابتدائية",
            "مدرسة أبو بكر الصديق الابتدائية",
            "مدرسة الشهيد محمد الكفراوي",
            "مدرسة العريش الجديدة الابتدائية",
            "مدرسة الأبطال الابتدائية بنين",
            "مدرسة الرواد الابتدائية المشتركة",
            "مدرسة النور للمكفوفين بالعريش",
            "مدرسة الشهداء الابتدائية بنات",
            "مدرسة الفيروز الابتدائية المشتركة",
            "مدرسة الشروق الابتدائية بنين",
            "مدرسة درة سيناء الرسمية للغات",
            "مدرسة الفرسان الابتدائية المشتركة",
            "مدرسة السلام الابتدائية بنين",
            "مدرسة الواحة الابتدائية بالعريش",
            "مدرسة النخيل الابتدائية المشتركة"
        )

        val principals = listOf(
            "أ. جمال عبد المنعم راشد", "أ. محمد عبد الله العزازي", "أ. عبد الحميد سالمان الصوالحة",
            "أ. ناصر سالم الكاشف", "أ. سوسن محمود الكردي", "أ. إبراهيم حمدان التايه",
            "أ. هناء عثمان رضوان", "أ. ماجدة توفيق حسنين", "أ. هشام كمال الأخرس",
            "أ. أحمد فؤاد عابدين", "أ. سامي مصطفى الشوربجي", "أ. فتحي عبد الهادي البنا"
        )

        val vicePrincipals = listOf(
            "أ. عادل سليمان الشريف", "أ. رأفت إبراهيم الجندي", "أ. ممدوح حسن العيسوي",
            "أ. هدى أحمد قنديل", "أ. فاطمة سعيد المرسي", "أ. طارق عبد العزيز فودة"
        )

        val schoolsList = ArrayList<SchoolEntity>()

        for (i in 0 until 65) {
            val name = schoolNames[i]
            val sup = supervisors[i % supervisors.size]
            val pName = principals[i % principals.size]
            val vp1 = vicePrincipals[(i * 2) % vicePrincipals.size]
            val vp2 = vicePrincipals[(i * 2 + 1) % vicePrincipals.size]

            // Dynamic classes variation
            val cPrim = 4 + (i % 5)       // 4 to 8 classes
            val cUpp = 4 + ((i + 2) % 5)   // 4 to 8 classes

            // Teachers distribution per cadre
            val tAssist = 2 + (i % 3)
            val tFirst = 1 + ((i + 1) % 3)
            val tFirstA = 1 + ((i + 2) % 2)
            val tExpert = if (i % 2 == 0) 1 else 0
            val tSenior = if (i % 3 == 0) 1 else 0

            // Al-Arish realistic coordinates
            val baseLat = 31.1250 + (i * 0.0015)
            val baseLng = 33.7950 + (i * 0.0012)

            schoolsList.add(
                SchoolEntity(
                    id = (i + 1).toLong(),
                    name = name,
                    classesPrimary = cPrim,
                    classesUpper = cUpp,
                    teachersAssistant = tAssist,
                    teachersFirst = tFirst,
                    teachersFirstA = tFirstA,
                    teachersExpert = tExpert,
                    teachersSenior = tSenior,
                    referenceQuota = 24,
                    principalName = pName,
                    principalPhone = "010${(20000000 + i * 111111) % 90000000 + 10000000}",
                    principalJobTitle = "مدير مدرسة",
                    principalDateAssumed = "2024/09/01",
                    vicePrincipal1Name = vp1,
                    vicePrincipal1Stage = "صفوف أولية (1-3)",
                    vicePrincipal1Phone = "011${(30000000 + i * 222222) % 90000000 + 10000000}",
                    vicePrincipal2Name = vp2,
                    vicePrincipal2Stage = "صفوف عليا (4-6)",
                    vicePrincipal2Phone = "012${(40000000 + i * 333333) % 90000000 + 10000000}",
                    supervisorId = sup.id,
                    supervisorName = sup.name,
                    supervisorSubject = sup.subject,
                    supervisorPhone = sup.phone,
                    supervisorVisitDays = if (i % 2 == 0) "الأحد - الثلاثاء" else "الإثنين - الأربعاء",
                    latitude = baseLat,
                    longitude = baseLng,
                    address = "حي ${when (i % 5) { 0 -> "الريسة"; 1 -> "ضاحية السلام"; 2 -> "وسط المدينة"; 3 -> "أبي صقل"; else -> "المساعيد" }}، العريش"
                )
            )
        }
        schoolDao.insertAll(schoolsList)

        // 3. Sample initial teachers for schools
        val sampleTeachers = listOf(
            TeacherEntity(
                id = 1,
                schoolId = 1,
                fullName = "محمد إبراهيم حسن عاشور",
                teacherCode = "1049281",
                nationalId = "28503141801234",
                cadreDegree = "معلم أول",
                subject = "لغة عربية",
                qualification = "ليسانس آداب وتربية - قسم لغة عربية",
                qualificationDate = "2008/06/15",
                phone = "01012398471",
                address = "حي الفواخرية، العريش",
                weeklyPeriods = 22
            ),
            TeacherEntity(
                id = 2,
                schoolId = 1,
                fullName = "أحمد رضوان كمال الشريف",
                teacherCode = "1083921",
                nationalId = "29107221800987",
                cadreDegree = "معلم أول أ",
                subject = "تربية دينية إسلامية",
                qualification = "بكالوريوس دار العلوم والدراسات الإسلامية",
                qualificationDate = "2013/05/20",
                phone = "01198374612",
                address = "شارع 23 يوليو، العريش",
                weeklyPeriods = 20
            ),
            TeacherEntity(
                id = 3,
                schoolId = 1,
                fullName = "ياسين محمود سالم الشوربجي",
                teacherCode = "1129034",
                nationalId = "29509111802345",
                cadreDegree = "معلم مساعد / معلم",
                subject = "لغة عربية",
                qualification = "تربية ابتدائي لغة عربية",
                qualificationDate = "2018/07/01",
                phone = "01283746192",
                address = "ضاحية السلام، العريش",
                weeklyPeriods = 24
            ),
            TeacherEntity(
                id = 4,
                schoolId = 2,
                fullName = "عبد الله فتحي مصطفى النجار",
                teacherCode = "1028374",
                nationalId = "27902151804567",
                cadreDegree = "معلم خبير",
                subject = "لغة عربية",
                qualification = "ليسانس لغة عربية ودراسات إسلامية",
                qualificationDate = "2002/06/10",
                phone = "01039485716",
                address = "حي كرم أبو نجيلة، العريش",
                weeklyPeriods = 18
            ),
            TeacherEntity(
                id = 5,
                schoolId = 2,
                fullName = "سالم حمدان سلامة التايه",
                teacherCode = "1067283",
                nationalId = "28811201809182",
                cadreDegree = "معلم أول",
                subject = "تربية دينية إسلامية",
                qualification = "أصول دين - جامعة الأزهر",
                qualificationDate = "2010/06/25",
                phone = "01182736451",
                address = "حي أبي صقل، العريش",
                weeklyPeriods = 22
            ),
            TeacherEntity(
                id = 6,
                schoolId = 3,
                fullName = "محمود عادل عطوة الفيروز",
                teacherCode = "1104829",
                nationalId = "29012011803456",
                cadreDegree = "معلم أول",
                subject = "لغة عربية",
                qualification = "ليسانس آداب وتربية",
                qualificationDate = "2011/05/30",
                phone = "01023456789",
                address = "حي السمران، العريش",
                weeklyPeriods = 22
            ),
            TeacherEntity(
                id = 7,
                schoolId = 3,
                fullName = "عمر سليمان حسان الأطرش",
                teacherCode = "1019283",
                nationalId = "27508191802918",
                cadreDegree = "كبير معلمين",
                subject = "لغة عربية",
                qualification = "ليسانس لغة عربية - جامعة الأزهر",
                qualificationDate = "1998/06/15",
                phone = "01156789123",
                address = "المساعيد، العريش",
                weeklyPeriods = 16
            ),
            TeacherEntity(
                id = 8,
                schoolId = 4,
                fullName = "طارق إسماعيل رفاعي البنا",
                teacherCode = "1138492",
                nationalId = "29406121804567",
                cadreDegree = "معلم مساعد / معلم",
                subject = "لغة عربية",
                qualification = "تربية عام لغة عربية",
                qualificationDate = "2019/07/10",
                phone = "01234567890",
                address = "حي أبي صقل، العريش",
                weeklyPeriods = 24
            ),
            TeacherEntity(
                id = 9,
                schoolId = 5,
                fullName = "فاطمة الزهراء كمال عثمان",
                teacherCode = "1092834",
                nationalId = "29204151806789",
                cadreDegree = "معلم أول أ",
                subject = "تربية دينية إسلامية",
                qualification = "دراسات إسلامية وعربية - الأزهر",
                qualificationDate = "2014/06/20",
                phone = "01098765432",
                address = "شارع أسيوط، العريش",
                weeklyPeriods = 20
            ),
            TeacherEntity(
                id = 10,
                schoolId = 6,
                fullName = "خالد مصطفى بدران الريسة",
                teacherCode = "1056712",
                nationalId = "28607181801290",
                cadreDegree = "معلم أول",
                subject = "لغة عربية",
                qualification = "ليسانس آداب قسم لغة عربية",
                qualificationDate = "2009/06/05",
                phone = "01123459876",
                address = "منطقة الريسة، العريش",
                weeklyPeriods = 22
            ),
            TeacherEntity(
                id = 11,
                schoolId = 7,
                fullName = "سناء عبد الحميد القصاص",
                teacherCode = "1038475",
                nationalId = "28011231804321",
                cadreDegree = "معلم خبير",
                subject = "لغة عربية",
                qualification = "ليسانس آداب وتربية",
                qualificationDate = "2003/05/25",
                phone = "01287654321",
                address = "حي الزهور، العريش",
                weeklyPeriods = 18
            ),
            TeacherEntity(
                id = 12,
                schoolId = 8,
                fullName = "أشرف متولي سعيد الكاشف",
                teacherCode = "1118273",
                nationalId = "29309081809876",
                cadreDegree = "معلم مساعد / معلم",
                subject = "تربية دينية إسلامية",
                qualification = "أصول دين - تفسير وحديث",
                qualificationDate = "2017/06/30",
                phone = "01034567891",
                address = "شارع الفاتح، العريش",
                weeklyPeriods = 24
            )
        )
        teacherDao.insertAll(sampleTeachers)

        // 4. Sample supervision visits (Upcoming 48h, delayed, completed)
        val sampleVisits = listOf(
            // Upcoming in next 48h
            SupervisionVisitEntity(
                id = 1,
                supervisorId = 1,
                supervisorName = "أ. محمود عبد الرحمن سالم",
                schoolId = 1,
                schoolName = "مدرسة العريش الابتدائية المشتركة",
                visitDate = "2026-09-14",
                visitTime = "08:30 ص",
                month = "سبتمبر",
                academicYear = "2026/2027",
                visitType = "ميدانية دورية",
                objective = "متابعة أنصبة وسد عجز",
                status = "SCHEDULED",
                notes = "مراجعة جداول الحصص الأسبوعية ودفاتر التحضير"
            ),
            SupervisionVisitEntity(
                id = 2,
                supervisorId = 2,
                supervisorName = "أ. خالد إبراهيم الشوربجي",
                schoolId = 2,
                schoolName = "مدرسة الشهيد الرائد محمد الزملوط",
                visitDate = "2026-09-15",
                visitTime = "09:00 ص",
                month = "سبتمبر",
                academicYear = "2026/2027",
                visitType = "تقويم فني",
                objective = "تقويم فني ومراجعة سجلات",
                status = "SCHEDULED",
                notes = "فحص دفاتر التقييمات الأسبوعية وتوزيع المناهج"
            ),
            // Delayed visit (requires attention!)
            SupervisionVisitEntity(
                id = 3,
                supervisorId = 1,
                supervisorName = "أ. محمود عبد الرحمن سالم",
                schoolId = 3,
                schoolName = "مدرسة الشهيد علاء الدين الابتدائية",
                visitDate = "2026-09-10",
                visitTime = "09:30 ص",
                month = "سبتمبر",
                academicYear = "2026/2027",
                visitType = "ميدانية دورية",
                objective = "متابعة أنصبة وسد العجز",
                status = "DELAYED",
                delayReason = "ظروف طارئة وتعذر المواصلات - بحاجة لإعادة جدولة",
                notes = "تأجلت بسبب ظرف طارئ"
            ),
            // Completed visit with signature
            SupervisionVisitEntity(
                id = 4,
                supervisorId = 3,
                supervisorName = "أ. سمير فتحي النجار",
                schoolId = 4,
                schoolName = "مدرسة أبي صقل الابتدائية بنين",
                visitDate = "2026-09-08",
                visitTime = "08:30 ص",
                month = "سبتمبر",
                academicYear = "2026/2027",
                visitType = "ميدانية دورية",
                objective = "متابعة أنصبة وسد عجز",
                status = "COMPLETED",
                notes = "تمت الزيارة بنجاح، وجميع أنصبة التربية الدينية مكتملة ومطابقة للقانون 156.",
                supervisorSignature = "تم التوقيع الإلكتروني - أ. سمير فتحي النجار (08-09-2026)",
                principalSignature = "تم الاعتماد والتوقيع - مدير مدرسة أبي صقل"
            )
        )
        visitDao.insertAll(sampleVisits)
    }
}
