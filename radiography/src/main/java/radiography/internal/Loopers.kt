package radiography.internal

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

internal fun <T : Any> runningOnLooper(looper: Looper, block: () -> T): T? {
  if (looper.thread == Thread.currentThread()) {
    return block()
  }

  val latch = CountDownLatch(1)
  lateinit var result: T
  Handler(looper).post {
    result = block()
    latch.countDown()
  }
  if (!latch.await(5, SECONDS)) {
    return null
  }
  return result
}
