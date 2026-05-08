package com.linterna.mpe.pattern

import android.content.Context
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PatternRepository(
    context: Context,
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    },
) {
    private val directory = File(context.filesDir, "patterns")

    fun save(pattern: MotionPattern) {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        fileFor(pattern.id).writeText(json.encodeToString(pattern))
    }

    fun load(id: String): MotionPattern? {
        val file = fileFor(id)
        if (!file.exists()) return null
        return json.decodeFromString<MotionPattern>(file.readText())
    }

    fun delete(id: String): Boolean = fileFor(id).delete()

    private fun fileFor(id: String): File {
        val safeId = id.replace(Regex("[^A-Za-z0-9_.-]"), "_")
        return File(directory, "$safeId.json")
    }
}
