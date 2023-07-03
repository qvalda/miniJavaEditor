package helpers

import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing

class ThrottleCall(private val delay: Int, private val statement: () -> Unit) {
    @Volatile
    private var lastCallTime = 0L
    private var job: Job? = null

    operator fun invoke() {
        lastCallTime = System.currentTimeMillis()
        if (job == null || job!!.isCompleted) {
            job = CoroutineScope(Dispatchers.Default).launch {
                while (true) {
                    val diff = System.currentTimeMillis() - lastCallTime
                    if (diff >= delay) break
                    delay(delay - diff)
                }
                CoroutineScope(Dispatchers.Swing).launch {
                    statement()
                }
            }
        }
    }
}