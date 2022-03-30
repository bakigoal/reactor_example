package com.bakigoal.reactor.create

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import reactor.core.publisher.SynchronousSink
import reactor.test.StepVerifier

class FluxHandleTest {

    private fun alphabet(letterNumber: Int): String? {
        if (letterNumber < 1 || letterNumber > 26) {
            return null
        }
        val letterIndexAscii = 'A'.code + letterNumber - 1
        return "${letterIndexAscii.toChar()}"
    }

    @Test
    fun `Flux handle sink test`() {
        val source = Flux.just(-1, 30, 13, 9, 20)
            .handle { i, sink: SynchronousSink<String> ->
                val letter = alphabet(i)
                if (letter != null) {
                    sink.next(letter)
                }
            }
        StepVerifier.create(source)
            .expectNext("M", "I", "T")
            .verifyComplete()
    }
}