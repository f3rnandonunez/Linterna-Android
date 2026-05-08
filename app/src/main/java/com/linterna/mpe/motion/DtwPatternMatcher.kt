package com.linterna.mpe.motion

import com.linterna.mpe.pattern.AccelVector
import com.linterna.mpe.pattern.MotionPattern
import kotlin.math.min

class DtwPatternMatcher : PatternMatcher {
    override fun similarity(recorded: MotionPattern, candidate: List<AccelVector>): Float {
        val pattern = recorded.values
        if (pattern.isEmpty() || candidate.isEmpty()) return 0f

        val rows = pattern.size
        val columns = candidate.size
        val costs = Array(rows + 1) { FloatArray(columns + 1) { Float.POSITIVE_INFINITY } }
        costs[0][0] = 0f

        for (i in 1..rows) {
            for (j in 1..columns) {
                val distance = pattern[i - 1].distanceTo(candidate[j - 1])
                val previous = min(costs[i - 1][j], min(costs[i][j - 1], costs[i - 1][j - 1]))
                costs[i][j] = distance + previous
            }
        }

        val normalizedDistance = costs[rows][columns] / (rows + columns).toFloat()
        return 1f / (1f + normalizedDistance)
    }
}
