package com.example.licenta.model


interface GenericCallback<T> {
    fun onResponseReady(generic: T)
}