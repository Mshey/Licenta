package com.example.licenta

import kotlinx.coroutines.Deferred

interface Cache<Key : Any, Value : Any> {
    fun get(key: Key): Deferred<Value?>
    fun set(key: Key, value: Value): Deferred<Unit>
}