package com.ng.kasptest.model

interface Producer {
    // возвращает null, если объект stopSignal указывает на необходимость остановки,
    // либо новый запрос в противном случае
    fun getRequest(stopSignal: Stopper): Request?
}