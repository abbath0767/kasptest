package com.ng.kasptest

import android.util.Log
import com.ng.kasptest.model.Producer
import com.ng.kasptest.model.Request
import com.ng.kasptest.model.RequestImpl
import com.ng.kasptest.model.Stopper
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

object RequestGenerator : Producer {

    private var counter = AtomicInteger()
    private var random = Random()

    override fun getRequest(stopSignal: Stopper): Request? {
        Log.d("TAG", "Generate request. isStop? ${stopSignal.isStop}")

        if (stopSignal.isStop)
            return null

        val needDelay = random.nextBoolean()

        Log.d("TAG", "Need delay for generate request? $needDelay")
        return if (needDelay) {
            Log.d("TAG", "yes...sleep")
            Thread.sleep(random.nextInt(Settings.requestGenerateMaxtime).toLong())
            Log.d("TAG", "Generate!")
            RequestImpl(counter.getAndIncrement())
        } else {
            Log.d("TAG", "Nope, generate")
            RequestImpl(counter.getAndIncrement())
        }
    }
}