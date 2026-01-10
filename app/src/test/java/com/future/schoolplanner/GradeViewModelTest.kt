package com.future.schoolplanner

import androidx.compose.ui.graphics.Color
import com.future.schoolplanner.data.Grade
import com.future.schoolplanner.data.GradeInputMethod
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.ui.GradeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class GradeViewModelTest {

    private lateinit var viewModel: GradeViewModel

    @Before
    fun setup() {
        viewModel = GradeViewModel()
    }

    @Test
    fun `test initial subjects are loaded`() = runBlocking {
        val subjects = viewModel.subjects.first()
        assert(subjects.isNotEmpty()) { "Initial subjects should not be empty" }
    }

    @Test
    fun `test add grade to subject`() = runBlocking {
        // Get initial subjects
        val initialSubjects = viewModel.subjects.first()
        val testSubject = initialSubjects.first()

        // Add a new grade
        val newGrade = Grade(
            id = UUID.randomUUID().toString(),
            value = 2.5,
            description = "Test grade",
            date = "2024-11-01"
        )

        viewModel.addGrade(testSubject.id, newGrade)

        // Verify the grade was added
        val updatedSubjects = viewModel.subjects.first()
        val updatedSubject = updatedSubjects.find { it.id == testSubject.id }

        assert(updatedSubject != null) { "Subject should still exist" }
        assert(updatedSubject?.grades?.size == testSubject.grades.size + 1) {
            "Subject should have one more grade"
        }
    }

    @Test
    fun `test calculate average`() {
        val subject = Subject(
            id = UUID.randomUUID().toString(),
            name = "Test Subject",
            color = Color.Blue,
            grades = listOf(
                Grade(UUID.randomUUID().toString(), 1.0, 1.0),
                Grade(UUID.randomUUID().toString(), 2.0, 1.0),
                Grade(UUID.randomUUID().toString(), 3.0, 1.0)
            )
        )

        val average = viewModel.calculateAverage(subject)
        assertEquals(2.0, average, 0.01) { "Average should be 2.0" }
    }

    @Test
    fun `test calculate weighted average`() {
        val subject = Subject(
            id = UUID.randomUUID().toString(),
            name = "Test Subject",
            color = Color.Blue,
            grades = listOf(
                Grade(UUID.randomUUID().toString(), 1.0, 2.0), // Weighted as 2.0
                Grade(UUID.randomUUID().toString(), 2.0, 1.0), // Weighted as 2.0
                Grade(UUID.randomUUID().toString(), 3.0, 1.0)  // Weighted as 3.0
            )
        )

        val average = viewModel.calculateAverage(subject)
        val expected = (1.0 * 2.0 + 2.0 * 1.0 + 3.0 * 1.0) / (2.0 + 1.0 + 1.0) // = 7/4 = 1.75
        assertEquals(expected, average, 0.01) { "Weighted average should be 1.75" }
    }

    @Test
    fun `test add new subject`() = runBlocking {
        val initialCount = viewModel.subjects.first().size

        val newSubject = Subject(
            id = UUID.randomUUID().toString(),
            name = "New Test Subject",
            color = Color.Red
        )

        viewModel.addSubject(newSubject)

        val updatedCount = viewModel.subjects.first().size
        assertEquals(initialCount + 1, updatedCount) { "Should have one more subject" }
    }

    @Test
    fun `test calculate overall average`() = runBlocking {
        // Clear existing subjects and add test subjects
        val subjects = viewModel.subjects.first().toMutableList()
        subjects.clear()

        val subject1 = Subject(
            id = UUID.randomUUID().toString(),
            name = "Subject1",
            color = Color.Blue,
            grades = listOf(Grade(UUID.randomUUID().toString(), 1.0), Grade(UUID.randomUUID().toString(), 3.0))
        )
        val subject2 = Subject(
            id = UUID.randomUUID().toString(),
            name = "Subject2",
            color = Color.Red,
            grades = listOf(Grade(UUID.randomUUID().toString(), 2.0))
        )

        viewModel.addSubject(subject1)
        viewModel.addSubject(subject2)

        val overallAverage = viewModel.calculateOverallAverage()
        val expected = (2.0 + 2.0) / 2.0 // Average of subject averages: (2.0 + 2.0) / 2
        assertEquals(expected, overallAverage, 0.01) { "Overall average should be correct" }
    }

    @Test
    fun `test parse whole grade input`() {
        viewModel.setGradeInputMethod(GradeInputMethod.WHOLE)

        assertEquals(2.0, viewModel.parseGradeInput("2", GradeInputMethod.WHOLE))
        assertEquals(1.0, viewModel.parseGradeInput("1", GradeInputMethod.WHOLE))
        assertEquals(6.0, viewModel.parseGradeInput("6", GradeInputMethod.WHOLE))
        assertNull(viewModel.parseGradeInput("7", GradeInputMethod.WHOLE))
        assertNull(viewModel.parseGradeInput("0", GradeInputMethod.WHOLE))
        assertNull(viewModel.parseGradeInput("2.5", GradeInputMethod.WHOLE))
    }

    @Test
    fun `test parse decimal grade input`() {
        viewModel.setGradeInputMethod(GradeInputMethod.DECIMAL)

        assertEquals(2.5, viewModel.parseGradeInput("2.5", GradeInputMethod.DECIMAL))
        assertEquals(1.0, viewModel.parseGradeInput("1.0", GradeInputMethod.DECIMAL))
        assertEquals(6.0, viewModel.parseGradeInput("6.0", GradeInputMethod.DECIMAL))
        assertNull(viewModel.parseGradeInput("6.5", GradeInputMethod.DECIMAL))
        assertNull(viewModel.parseGradeInput("0.5", GradeInputMethod.DECIMAL))
    }

    @Test
    fun `test parse tendency grade input`() {
        viewModel.setGradeInputMethod(GradeInputMethod.TENDENCY)

        assertEquals(2.0, viewModel.parseGradeInput("2", GradeInputMethod.TENDENCY))
        assertEquals(1.7, viewModel.parseGradeInput("2+", GradeInputMethod.TENDENCY))
        assertEquals(2.3, viewModel.parseGradeInput("2-", GradeInputMethod.TENDENCY))
        assertEquals(1.0, viewModel.parseGradeInput("1+", GradeInputMethod.TENDENCY))
        assertEquals(1.3, viewModel.parseGradeInput("1-", GradeInputMethod.TENDENCY))
        assertNull(viewModel.parseGradeInput("7", GradeInputMethod.TENDENCY))
        assertNull(viewModel.parseGradeInput("2++", GradeInputMethod.TENDENCY))
    }

    @Test
    fun `test parse fifteen point input`() {
        viewModel.setGradeInputMethod(GradeInputMethod.FIFTEEN_POINT)

        assertEquals(1.0, viewModel.parseGradeInput("15", GradeInputMethod.FIFTEEN_POINT))
        assertEquals(6.0, viewModel.parseGradeInput("0", GradeInputMethod.FIFTEEN_POINT))
        assertEquals(3.5, viewModel.parseGradeInput("7", GradeInputMethod.FIFTEEN_POINT)) // (6.0 - (7/15*5))
        assertNull(viewModel.parseGradeInput("16", GradeInputMethod.FIFTEEN_POINT))
        assertNull(viewModel.parseGradeInput("-1", GradeInputMethod.FIFTEEN_POINT))
    }

    @Test
    fun `test format grade for display`() {
        assertEquals("2", viewModel.formatGradeForDisplay(2.0, GradeInputMethod.WHOLE))
        assertEquals("2.5", viewModel.formatGradeForDisplay(2.5, GradeInputMethod.DECIMAL))
        assertEquals("2", viewModel.formatGradeForDisplay(2.0, GradeInputMethod.TENDENCY))
        assertEquals("2-", viewModel.formatGradeForDisplay(2.3, GradeInputMethod.TENDENCY))
        assertEquals("2+", viewModel.formatGradeForDisplay(1.7, GradeInputMethod.TENDENCY))
        assertEquals("15", viewModel.formatGradeForDisplay(1.0, GradeInputMethod.FIFTEEN_POINT))
        assertEquals("0", viewModel.formatGradeForDisplay(6.0, GradeInputMethod.FIFTEEN_POINT))
        assertEquals("7", viewModel.formatGradeForDisplay(3.5, GradeInputMethod.FIFTEEN_POINT))
    }
}
