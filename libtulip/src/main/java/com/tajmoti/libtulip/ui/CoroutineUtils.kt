package com.tajmoti.libtulip.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty0

fun CoroutineScope.doCancelableJob(
    job: KMutableProperty0<Job?>,
    state: MutableStateFlow<Boolean>?,
    task: suspend () -> Unit
) {
    job.get()?.cancel()
    val newJob = launch {
        state?.value = true
        try {
            task()
        } finally {
            state?.value = false
        }
    }
    job.set(newJob)
}