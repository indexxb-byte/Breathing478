package com.example.breathing478

enum class BreathingPhase {
    INHALE, HOLD_IN, EXHALE, HOLD_OUT, IDLE
}

enum class BreathingMode(val label: String, val inhale: Int, val holdIn: Int, val exhale: Int, val holdOut: Int) {
    MODE_478("4-7-8", 4, 7, 8, 0),
    MODE_4444("4-4-4-4", 4, 4, 4, 4)
}