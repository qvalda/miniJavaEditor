package helpers

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class ThrottleCallTest {

    @Test
    fun testThrottleCall() {
        runBlocking {
            val callCount = AtomicInteger()
            val signal = CountDownLatch(1);
            val tc = ThrottleCall(100) {
                callCount.incrementAndGet()
                signal.countDown()
            }
            tc.invoke()
            delay(50)
            tc.invoke()
            assertEquals(0, callCount.get())
            tc.invoke()
            signal.await()
            assertEquals(1, callCount.get())
            delay(150)
            assertEquals(1, callCount.get())
        }
    }
}