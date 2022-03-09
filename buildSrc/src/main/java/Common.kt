import org.gradle.api.Project
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

fun getEnvOrLocalSecret(project: Project, name: String, secretName: String): String {
    return System.getenv()[name]
        ?: getLocalSecret(project, secretName)
        ?: getLocalDefaultSecret(project, secretName)
        ?: throw IllegalArgumentException("Variable '$name' or '$secretName' not found!")
}

fun getLocalProp(project: Project, file: String, secretName: String): String? {
    val props = loadProps(project, file)
    return props.getProperty(secretName)
}

fun loadProps(project: Project, file: String): Properties {
    return loadProps(project.file(file))
}

fun loadProps(file: File): Properties {
    val props = Properties()
    runCatching { file.inputStream().use { props.load(it) } }
    return props
}

fun Project.loadPropsIfExists(file: String): Properties? {
    return loadPropsIfExists(project.file(file))
}

fun loadPropsIfExists(file: File): Properties? {
    return file
        .takeIf { it.exists() }
        ?.let { loadProps(it) }
}

private fun getLocalSecret(project: Project, secretName: String): String? {
    return getLocalProp(project, "secrets.properties", secretName)
}

private fun getLocalDefaultSecret(project: Project, secretName: String): String? {
    return getLocalProp(project, "default.secrets.properties", secretName)
}

fun getGitCommitHash(): String {
    val proc = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor(5, TimeUnit.SECONDS)
    return proc.inputStream.bufferedReader().readText()
}