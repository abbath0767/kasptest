package com.ng.kasptest

import com.ng.kasptest.model.Consumer
import com.ng.kasptest.model.Request
import com.ng.kasptest.model.Stopper
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


object RequestHandler : Consumer {

    private var requestCountCallback: ((Int) -> Unit)? = null
    private var countDownCallback: ((Int, Boolean) -> Unit)? = null
    private val requestCounter = AtomicInteger(0)
    private val numCores: Int
    private val isStopped = AtomicBoolean(false)
    private val stopStarted = AtomicBoolean(false)
    private val countdownStarted = AtomicBoolean(false)
    private var countdownThread: Thread? = null
    private val random = Random()

    init {
        val processorCount = Runtime.getRuntime().availableProcessors()
        numCores =
                if (processorCount <= 2) 8
                else processorCount
    }

    private var executor = ThreadPoolExecutor(2, numCores, 60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())

    private fun reinitExecutorAftetShutdown() {
        executor = ThreadPoolExecutor(2, numCores, 60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())
    }

    override fun processRequest(request: Request?, stopSignal: Stopper) {
        if (stopSignal.isStop) {
            stopAllProcess()
            return
        }

        if (isStopped.get()) {
            return
        }

        if (!countdownStarted.get()) {
            startCountDownTimer()
        }

        if (request == null)
            throw IllegalArgumentException("If process not stop request must not be null")

        val needDelay = random.nextBoolean()

        if (needDelay) {
            if (Settings.requestExecuteDelayMaxTime > 0)
                Thread.sleep(random.nextInt(Settings.requestExecuteDelayMaxTime).toLong())
            if (isStopped.get()) {
                return
            }
            executeRequest(request)
        } else {
            executeRequest(request)
        }
    }

    private fun startCountDownTimer() {
        countdownStarted.set(true)

        countdownThread = Thread(Runnable {
            try {
                var counter = Settings.countDownTime

                while (true) {
                    countDownCallback?.invoke(counter--, isStopped.get())
                    Thread.sleep(1000L)
                    if (counter == 0) {
                        //                    stopAllProcess()
                        processRequest(null, Stopper(true))
                        countdownStarted.set(false)
                        countDownCallback?.invoke(Settings.countDownTime, isStopped.get())
                        break
                    }
                }
            } catch (e: Exception) {
                countdownStarted.set(false)
            }
        }).apply {
            start()
        }
    }

    private fun executeRequest(request: Request) {
        if (executor.isShutdown || executor.isTerminating) {
            if (!isStopped.get()) {
                reinitExecutorAftetShutdown()
            }
        }

        requestCountCallback?.invoke(requestCounter.incrementAndGet())
        val runnable = RequestWrapper(request) {
            requestCountCallback?.invoke(requestCounter.decrementAndGet())
        }

        try {
            executor.execute(runnable)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun subscribeToRequestCount(callback: (Int) -> Unit) {
        this.requestCountCallback = callback
    }

    fun subscribeToCountDown(callback: (Int, Boolean) -> Unit) {
        this.countDownCallback = callback
    }

    private fun stopAllProcess() {
        if (stopStarted.get())
            return

        stopStarted.set(true)
        isStopped.set(true)

        executor.shutdownNow()
        countdownThread?.interrupt()
        countDownCallback?.invoke(Settings.countDownTime, isStopped.get())

        requestCounter.set(0)
        requestCountCallback?.invoke(0)

        isStopped.set(false)
        stopStarted.set(false)
    }

    class RequestWrapper(private val request: Request, private val finishCallback: () -> Unit) : Runnable {
        private var time = 0L

        init {
            time = if (Settings.requestMaxTime > 0) Random().nextInt(Settings.requestMaxTime).toLong() else 1000L
        }

        override fun run() {
            try {
                Thread.sleep(time)
                finishCallback.invoke()
            } catch (e: Exception) {
            }
        }
    }
}