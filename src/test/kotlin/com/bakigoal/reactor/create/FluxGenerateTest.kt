package com.bakigoal.reactor.create

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.SynchronousSink
import reactor.test.StepVerifier
import java.util.concurrent.atomic.AtomicLong

class FluxGenerateTest {

    @Test
    fun `generate custom flux 1`() {
        val flux = Flux.generate(
            { 0 },
            { state: Int, sink: SynchronousSink<String> ->
                sink.next("3 x $state = ${3 * state}")
                if (state == 4) sink.complete()
                state + 1
            }
        )

        StepVerifier.create(flux)
            .expectNext("3 x 0 = 0")
            .expectNext("3 x 1 = 3")
            .expectNext("3 x 2 = 6")
            .expectNext("3 x 3 = 9")
            .expectNext("3 x 4 = 12")
            .verifyComplete()
    }

    @Test
    fun `generate custom flux 2`() {
        val flux = Flux.generate(
            { AtomicLong() },
            { state: AtomicLong, sink: SynchronousSink<String> ->
                val i = state.getAndIncrement()
                sink.next("3 x $i = ${3 * i}")
                if (i == 4L) sink.complete()
                state
            },
            { state ->
                println("State = $state")
            }
        )

        StepVerifier.create(flux)
            .expectNext("3 x 0 = 0")
            .expectNext("3 x 1 = 3")
            .expectNext("3 x 2 = 6")
            .expectNext("3 x 3 = 9")
            .expectNext("3 x 4 = 12")
            .verifyComplete()
    }
}