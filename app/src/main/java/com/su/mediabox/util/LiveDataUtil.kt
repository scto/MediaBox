package com.su.mediabox.util

import androidx.arch.core.internal.SafeIterableMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.removeAllObserver() {
    LiveData::class.java.geMemberOrNull<SafeIterableMap<Observer<T>, *>>("mObservers", this)
        ?.forEach {
            removeObserver(it.key)
        }
}

fun <T> MutableLiveData<T>.toLiveData(): LiveData<T> = this