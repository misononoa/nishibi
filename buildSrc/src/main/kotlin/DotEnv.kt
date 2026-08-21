import java.io.File

object DotEnv {
    fun load(path: String): Map<String, String> {
        val file = File(path)
        if(!file.exists()) return emptyMap()
        
        return file.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .mapNotNull { line ->
                    val idx = line.indexOf('=')
                    if (idx == -1) return@mapNotNull null
                    val k = line.substring(0, idx).trim()
                    var v = line.substring(idx + 1).trim()
                    if ((v.startsWith("\"") && v.endsWith("\""))
                            || (v.startsWith("'") && v.endsWith("'"))) {
                        v = v.substring(1, v.length -1)
                    }
                    k to v
                }
                .toMap()
    }
}