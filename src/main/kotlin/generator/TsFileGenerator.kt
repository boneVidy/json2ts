package generator

import parser.toTypescript
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class TsFileGenerator {
    fun generateFromJson(json: String, fileName: String) {
        val tsCode = toTypescript(json)
        val file = File("$fileName.ts")
        if (!file.exists()) {
            file.createNewFile()
        }
        val out = BufferedWriter(FileWriter(file))
        try {
            out.write(tsCode)
            out.flush()
            out.close()
        } finally {
            out.close()
        }

    }
}