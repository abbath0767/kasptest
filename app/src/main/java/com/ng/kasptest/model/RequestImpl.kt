package com.ng.kasptest.model

import android.util.Log

class RequestImpl(private val id: Int): Request {
    override fun getId() = id

    init {
        Log.d("TAG", "Generate new request: $id")
    }
}