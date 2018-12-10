package com.ng.kasptest

import android.annotation.SuppressLint
import android.util.Log
import com.ng.kasptest.model.Consumer
import com.ng.kasptest.model.Request
import com.ng.kasptest.model.Stopper
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


@SuppressLint("LogNotTimber")
object RequestHandler : Consumer {

    private var requestCountCallback: ((Int) -> Unit)? = null
    private val requestCounter = AtomicInteger(0)
    private val numCores: Int
    private val needStop = AtomicBoolean(false)
    private val random = Random()

    init {
        val processorCount = Runtime.getRuntime().availableProcessors()
        numCores =
                if (processorCount <= 2) 8
                else processorCount
    }

    private val executor = ThreadPoolExecutor(2, numCores, 60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())

    override fun processRequest(request: Request?, stopSignal: Stopper) {
        Log.d("TAG", "Process request: ${request?.getId()}, isStop? : ${stopSignal.isStop}")

        if (stopSignal.isStop) {
            //..process STOP all requests
            return
        }

        if (request == null)
            throw IllegalArgumentException("If process not stop request must not be null")

        val needDelay = random.nextBoolean()

        Log.d("TAG", "Need delay for process request (${request.getId()})? : $needDelay")

        if (needDelay) {
            Log.d("TAG", "Yes...sleep for ${request.getId()}")
            Thread.sleep(random.nextInt(Settings.requestExecuteDelayMaxTime).toLong())
            Log.d("TAG", "Execute: ${request.getId()}")
            executeRequest(request)
        } else {
            Log.d("TAG", "Nope, execute ${request.getId()}")
            executeRequest(request)
        }
    }

    private fun executeRequest(request: Request) {
        Log.d("TAG", "Add execute to thread pool: ${request.getId()}")

        requestCountCallback?.invoke(requestCounter.incrementAndGet())
        val runnable = RequestWrapper(request) {
            requestCountCallback?.invoke(requestCounter.decrementAndGet())
        }
        executor.execute(runnable)
    }

    fun subscribeToRequestCount(callback: (Int) -> Unit) {
        this.requestCountCallback = callback
    }

    class RequestWrapper(private val request: Request, private val finishCallback: () -> Unit) : Runnable {
        private val time = Random().nextInt(Settings.requestMaxTime).toLong()
        override fun run() {
            Log.d("TAG", "Start running request: ${request.getId()}, time: $time")
            Thread.sleep(time)
            Log.d("TAG", "Stop running request: ${request.getId()}, time: $time")
            finishCallback.invoke()
        }
    }
}