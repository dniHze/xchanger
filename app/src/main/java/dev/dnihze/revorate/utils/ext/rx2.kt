package dev.dnihze.revorate.utils.ext

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

fun <T : Any> Single<T>.retryWithExponentialBackoff(numOfRetries: Long = 3, delayInSec: Long = 2): Single<T> {
    return this.retryWhen { errors: Flowable<Throwable> ->
        errors.zipWith(
            Flowable.range(1, numOfRetries.toInt() + 1), BiFunction<Throwable, Int, Int> { error: Throwable, retryCount: Int ->
                if (retryCount > numOfRetries) {
                    throw error
                } else {
                    retryCount
                }
            }
        ).flatMap { retryCount: Int ->
            Flowable.timer(
                Math.pow(delayInSec.toDouble(), retryCount.toDouble()).toLong(),
                TimeUnit.SECONDS
            )
        }
    }
}