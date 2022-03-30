package com.bakigoal.reactor

import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

import reactor.core.scheduler.Schedulers


class ThreadingAndSchedulers {

    @Test
    fun `mono in a new thread`() {
        val mono = Mono.just("Hi there")
        println("Created mono in thread: ${currentThread()}")

        val thread = Thread {
            mono.map { "$it from a thread" }
                .subscribe { println("$it: ${currentThread()}") }
        }

        thread.start()
        thread.join()
    }

    private fun currentThread() = Thread.currentThread().name

    @Test
    fun `publishOn method test`() {
        val scheduler = Schedulers.newParallel("parallel-scheduler", 4)

        val flux = Flux
            .range(1, 2)
            .map { i: Int ->
                println("${currentThread()}: calculate $i")
                10 + i
            }
            .publishOn(scheduler)
            .map { i: Int ->
                println("${currentThread()}: change to string $i")
                "value $i"
            }

        val thread = Thread { flux.subscribe { x: String -> println("${currentThread()}: $x") } }
        thread.start()
        Thread.sleep(1000L)
    }

    @Test
    fun `subscribeOn method test`() {
        val scheduler = Schedulers.newParallel("parallel-scheduler", 4)

        val flux = Flux
            .range(1, 2)
            .map { i: Int ->
                println("${currentThread()}: calculate $i")
                10 + i
            }
            .subscribeOn(scheduler) // Changes the Thread from which the whole chain of operators subscribes
            .map { i: Int ->
                println("${currentThread()}: change to string $i")
                "value $i"
            }

        val thread = Thread { flux.subscribe { x: String -> println("${currentThread()}: $x") } }
        thread.start()
        Thread.sleep(1000L)
    }
}