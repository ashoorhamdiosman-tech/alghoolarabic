package com.example

import com.example.data.entity.SchoolEntity
import com.example.util.QuotaCalculations
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testQuotaFormulas_PrimaryAndUpper() {
    // School with 6 primary classes and 6 upper classes
    // Arabic = (6*9) + (6*9) = 54 + 54 = 108
    // Religion = (6*5) + (6*4) = 30 + 24 = 54
    // Total Required = 108 + 54 = 162
    val school = SchoolEntity(
        id = 1,
        name = "مدرسة تجريبية بالعريش",
        classesPrimary = 6,
        classesUpper = 6,
        teachersAssistant = 4, // 4 * 24 = 96
        teachersFirst = 2,     // 2 * 22 = 44
        teachersFirstA = 1,    // 1 * 20 = 20
        teachersExpert = 0,
        teachersSenior = 0,
        referenceQuota = 24
    )
    val result = QuotaCalculations.calculateSchool(school)

    assertEquals(108, result.arabicRequired)
    assertEquals(54, result.religionRequired)
    assertEquals(162, result.totalRequired)

    // Available: 96 + 44 + 20 = 160
    assertEquals(160, result.availablePeriods)

    // Period Gap: 160 - 162 = -2 (Deficit of 2 periods)
    assertEquals(-2, result.periodGap)
    assertTrue(result.isDeficit)
    assertFalse(result.isSurplus)

    // Teacher equivalent: -2 / 24 = -0.08
    assertEquals(-0.08, result.teacherEquivalent, 0.01)
  }
}
