package com.ng.kasptest.model

interface Consumer {
    // обрабатывает запрос, завершает обработку досрочно, если
    // объект stopSignal указывает на необходимость остановки
    fun processRequest(request: Request?, stopSignal: Stopper)
}