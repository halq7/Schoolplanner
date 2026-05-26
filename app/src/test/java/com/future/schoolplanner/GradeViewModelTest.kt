package com.future.schoolplanner

import androidx.compose.ui.graphics.Color
import com.future.schoolplanner.data.Subject
import com.future.schoolplanner.ui.GradeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
    fun `test add new subject`() = runBlocking {
        val initialCount = viewModel.subjects.first().size

        val newSubject = Subject(
            id = UUID.randomUUID().toString(),
            name = "New Test Subject",
            color = Color.Red,
            schoolYearId = UUID.randomUUID().toString()
        )

        viewModel.addSubject(newSubject)

        val updatedCount = viewModel.subjects.first().size
        assertEquals("Should have one more subject", initialCount + 1, updatedCount)
    }
}
