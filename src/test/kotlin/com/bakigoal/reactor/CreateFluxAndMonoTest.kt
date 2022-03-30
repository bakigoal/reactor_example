package com.bakigoal.reactor

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration

internal class CreateFluxAndMonoTest {

    @Test
    fun `create Flux with just() method`() {
        StepVerifier.create(Flux.just("foo", "bar", "foobar"))
            .expectNext("foo")
            .expectNext("bar")
            .expectNext("foobar")
            .expectComplete()
            .verify()
    }

    @Test
    fun `create Flux with iterable`() {
        StepVerifier.create(Flux.fromIterable(listOf("foo", "bar", "foobar")))
            .expectNextCount(3)
            .expectComplete()
            .verify()
    }

    @Test
    fun `test error in flux`() {
        val fluxFromIterable = Flux.fromIterable(listOf("foo", "bar", "foobar"))
        val error = Mono.error<String>(IllegalArgumentException("Bad Argument"))
        StepVerifier.create(fluxFromIterable.concatWith(error))
            .expectNextCount(3)
            .expectErrorMatches { it is IllegalArgumentException && it.message == "Bad Argument" }
            .verify()
    }

    @Test
    fun `create flux with range()`() {
        StepVerifier.create(Flux.range(5, 3))
            .expectNext(5, 6, 7)
            .expectComplete()
            .verify()
    }

    @Test
    fun `testing time-based publisher`() {
        StepVerifier.withVirtualTime { Flux.interval(Duration.ofDays(1)).take(3) }
            .expectSubscription()
            .expectNoEvent(Duration.ofDays(1))
            .expectNext(0L)
            .thenAwait(Duration.ofDays(1))
            .expectNext(1L)
            .thenAwait(Duration.ofDays(1))
            .expectNext(2L)
            .verifyComplete()
    }
}