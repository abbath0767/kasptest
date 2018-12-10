package com.ng.kasptest

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
        if (stopSignal.isStop)
            return null

        val needDelay = random.nextBoolean()

        return if (needDelay) {
            if (Settings.requestGenerateMaxtime > 0)
                Thread.sleep(random.nextInt(Settings.requestGenerateMaxtime).toLong())
            RequestImpl(counter.getAndIncrement())
        } else {
            RequestImpl(counter.getAndIncrement())
        }
    }
}