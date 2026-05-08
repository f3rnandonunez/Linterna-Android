package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelVector
import com.linterna.mpe.pattern.MotionPattern

interface PatternMatcher {
    fun similarity(recorded: MotionPattern, candidate: List<AccelVector>): Float
}
