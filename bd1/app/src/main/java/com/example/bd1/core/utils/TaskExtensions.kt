package com.example.bd1.core.utils

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Extensión para convertir Task de Firebase a suspending function
 */
suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation: CancellableContinuation<T> ->
        addOnSuccessListener { result ->
            continuation.resumeWith(Result.success(result))
        }
        addOnFailureListener { exception ->
            continuation.resumeWith(Result.failure(exception))
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
