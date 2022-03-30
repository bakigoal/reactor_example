package com.bakigoal.reactor

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import reactor.util.function.Tuple2
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertEquals


class ErrorHandlingTest {

    private fun makeBoomException(i: Int): String = throw IllegalArgumentException("Boom!!!")

    @Test
    fun `static fallback value with onErrorReturn`() {
        val onErrorReturn = Flux.just(10)
            .map(this::makeBoomException)
            .onErrorReturn("Recovered")

        StepVerifier.create(onErrorReturn)
            .expectNext("Recovered")
            .verifyComplete()
    }

    @Test
    fun `static fallback value with onErrorReturn with predicate`() {
        val onErrorReturn = Flux.just(10)
            .map(this::makeBoomException)
            .onErrorReturn({ it.message == "Boom!!!" }, "Recovered")

        StepVerifier.create(onErrorReturn)
            .expectNext("Recovered")
            .verifyComplete()
    }

    private fun callExternalService(k: String): Flux<String> = when {
        k.startsWith("timeout") -> Flux.error(TimeoutException())
        k.startsWith("key") -> Flux.just("value of $k")
        else -> Flux.error(IllegalArgumentException("Bad request"))
    }

    @Test
    fun `fallback method`() {


        val flatMap = Flux.just("timeout1", "key1", "unknown", "key2")
            .flatMap { k: String ->
                callExternalService(k)
                    .onErrorResume { error ->
                        if (error is TimeoutException) {
                            return@onErrorResume Flux.just("value from cache $k")
                        } else {
                            return@onErrorResume Flux.error(error)
                        }
                    }
            }

        StepVerifier.create(flatMap)
            .expectNext("value from cache timeout1")
            .expectNext("value of key1")
            .expectError()
            .verify()
    }

    @Test
    fun `catch and rethrow`() {
        class MyBusinessException(msg: String) : RuntimeException(msg)

        val onErrorResume = Flux.just("timeout1")
            .flatMap { k: String -> callExternalService(k) }
            .onErrorResume { Flux.error(MyBusinessException("oops...")) }

        StepVerifier.create(onErrorResume)
            .expectErrorMessage("oops...")
            .verify()
    }

    @Test
    fun `log or react on the side`() {
        val counter = AtomicLong()
        val flatMap = Flux.just("timeout1")
            .flatMap { k: String ->
                callExternalService(k)
                    .doOnError {
                        val count = counter.incrementAndGet()
                        println("$it: $count")
                    }
            }

        StepVerifier.create(flatMap)
            .expectError(TimeoutException::class.java)
            .verify()

        Thread.sleep(100L)
        assertEquals(1L, counter.get())
    }

    @Test
    fun `do finally`() {
        class MyBusinessException(msg: String) : RuntimeException(msg)

        val onErrorResume = Flux.just("timeout1")
            .flatMap { k: String -> callExternalService(k) }
            .onErrorResume { Flux.error(MyBusinessException("oops...")) }
            .doFinally { println("finished...") }

        StepVerifier.create(onErrorResume)
            .expectErrorMatches {
                it is MyBusinessException && it.message == "oops..."
            }
            .verify()
    }

    @Test
    fun `retry test`() {
        val retryFlux = Flux.interval(Duration.ofMillis(250))
            .map { input: Long ->
                if (input < 3) return@map "tick $input"
                throw RuntimeException("boom")
            }
            .retry(1)
            .elapsed()

        StepVerifier.create(retryFlux)
            .expectNextCount(6)
            .expectError()
            .verify()
    }
}