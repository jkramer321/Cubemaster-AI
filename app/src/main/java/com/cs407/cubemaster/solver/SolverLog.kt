package com.cs407.cubemaster.solver

import android.util.Log

interface LogStrategy {
    fun d(tag: String, msg: String)
    fun e(tag: String, msg: String)
}

object AndroidLogStrategy : LogStrategy {
    override fun d(tag: String, msg: String) { 
        try {
            Log.d(tag, msg) 
        } catch (e: RuntimeException) {
            // Fallback if mocked/stubbed Log fails (though usually it just throws)
            println("DEBUG: $tag: $msg")
        }
    }
    override fun e(tag: String, msg: String) { 
        try {
            Log.e(tag, msg) 
        } catch (e: RuntimeException) {
            println("ERROR: $tag: $msg")
        }
    }
}

object ConsoleLogStrategy : LogStrategy {
    override fun d(tag: String, msg: String) { println("DEBUG: $tag: $msg") }
    override fun e(tag: String, msg: String) { println("ERROR: $tag: $msg") }
}

object SolverLog {
    var strategy: LogStrategy = AndroidLogStrategy

    fun d(tag: String, msg: String) = strategy.d(tag, msg)
    fun e(tag: String, msg: String) = strategy.e(tag, msg)
}
