package com.newhome.app

import com.google.android.gms.tasks.*
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.Assert.*

class TestUtils {
    companion object {
        inline fun <reified E> assertThrowsAsync(message: String, testFunc: () -> Unit) : E {
            lateinit var ex: Throwable

            var exceptionThrown = false
            try {
                testFunc()
            } catch (e: Throwable) {
                ex = e
                exceptionThrown = e is E
            }

            assertTrue(message, exceptionThrown)
            return ex as E
        }

        inline fun <reified E> assertThrowsAsync(testFunc: () -> Unit) : E =
            assertThrowsAsync("Exception was not thrown", testFunc)

        inline fun <T, reified Ts : Task<T>> createSuccessTask(result: T): Ts {
            val task = mockk<Ts>()
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns result

            return task
        }


        inline fun <reified Ts : Task<Void>> createVoidSuccessTask(): Ts {
            val task = mockk<Ts>()
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            justRun { task.result }

            return task
        }

        inline fun <T, reified E : Exception, reified Ts : Task<T>> createFailureTask(exception: E): Ts {
            val task = mockk<Ts>()
            every { task.isComplete } returns true
            every { task.exception } returns exception
            every { task.isCanceled } returns false
            justRun { task.result }

            return task
        }
    }
}