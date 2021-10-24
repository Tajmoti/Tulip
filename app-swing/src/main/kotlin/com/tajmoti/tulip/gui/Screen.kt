package com.tajmoti.tulip.gui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import java.awt.Component

abstract class Screen<GUI : Component> {
    /**
     * Coroutine scope that lives from the creation of the screen until [cleanup] is called.
     * Coroutines are dispatched on the Swing (AWT) thread.
     */
    val screenScope = CoroutineScope(Dispatchers.Swing)

    /**
     * Whether the screen is already released ([cleanup] was called).
     */
    private var released = false

    /**
     * Root component of this screen to be added to the GUI or shown directly.
     */
    val root: GUI by lazy {
        val screen = initialize()
        bindFlows()
        screen
    }

    /**
     * Root component initialized by the implementation of this screen.
     */
    protected abstract fun initialize(): GUI


    /**
     * Collection of flow bindings - flows paired with their desired consumers.
     */
    protected open val flowBindings: Collection<FlowBinding<*>> = emptyList()


    /**
     * Performs cleanup. Must be called exactly once when this view
     * is being removed from the user interface.
     */
    open fun cleanup() {
        if (released) return
        released = true
        screenScope.cancel()
    }

    /**
     * JVM method used as a last resort, do not remove.
     */
    protected fun finalize() {
        cleanup()
    }


    private fun bindFlows() {
        for (binding in flowBindings) {
            @Suppress("UNCHECKED_CAST")
            binding as FlowBinding<Any?>
            screenScope.launch {
                binding.flow.collect {
                    binding.consumer(it)
                }
            }
        }
    }


    protected class FlowBinding<T : Any?>(
        val flow: Flow<T>,
        val consumer: suspend (T) -> Unit
    )

    protected infix fun <T> Flow<T>.flowTo(consumer: suspend (T) -> Unit): FlowBinding<T> {
        return FlowBinding(this, consumer)
    }
}