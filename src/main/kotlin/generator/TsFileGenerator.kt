package generator

import parser.toTypescript
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class TsFileGenerator {
    fun generateFromJson(json: String, fileName: String) {
        val tsCode = toTypescript(json)
        val file = File(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val out = BufferedWriter(FileWriter(file,true))
        try {
            out.append("\n\n")
            out.append(tsCode)
            out.flush()
//            out.close()
        } finally {
            out.close()
        }

    }
}