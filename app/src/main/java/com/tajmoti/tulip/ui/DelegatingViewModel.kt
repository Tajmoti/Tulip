package com.tajmoti.tulip.ui

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

abstract class DelegatingViewModel<T> : ViewModel() {
    abstract val impl: T
}

@MainThread
inline fun <T, reified VM : DelegatingViewModel<T>> Fragment.viewModelsDelegated(
    noinline ownerProducer: () -> ViewModelStoreOwner = { this },
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = object : Lazy<T> {
    val realLazy = createViewModelLazy(
        VM::class,
        { ownerProducer().viewModelStore },
        factoryProducer
    )

    override val value: T
        get() = realLazy.value.impl

    override fun isInitialized(): Boolean {
        return realLazy.isInitialized()
    }
}

@MainThread
inline fun <T, reified VM : DelegatingViewModel<T>> ComponentActivity.viewModelsDelegated(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<T> = object : Lazy<T> {
    val realLazy = run {
        val factoryPromise = factoryProducer ?: {
            defaultViewModelProviderFactory
        }

        ViewModelLazy(VM::class, { viewModelStore }, factoryPromise)
    }

    override val value: T
        get() = realLazy.value.impl

    override fun isInitialized(): Boolean {
        return realLazy.isInitialized()
    }
}