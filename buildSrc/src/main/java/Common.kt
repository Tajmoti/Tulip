import org.gradle.api.Project
import java.util.*
import java.util.concurrent.TimeUnit

fun getEnvOrLocalSecret(project: Project, name: String, secretName: String): String {
    return System.getenv()[name]
        ?: getLocalSecret(project, secretName)
        ?: getLocalDefaultSecret(project, secretName)
        ?: throw IllegalArgumentException("Variable '$name' or '$secretName' not found!")
}

private fun getLocalProp(project: Project, secretName: String, file: String): String? {
    val props = Properties()
    runCatching { project.file(file).inputStream().use { props.load(it) } }
    return props.getProperty(secretName)
}

private fun getLocalSecret(project: Project, secretName: String): String? {
    return getLocalProp(project, secretName, "secrets.properties")
}

private fun getLocalDefaultSecret(project: Project, secretName: String): String? {
    return getLocalProp(project, secretName, "default.secrets.properties")
}

fun getGitCommitHash(): String {
    val proc = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    proc.waitFor(5, TimeUnit.SECONDS)
    return proc.inputStream.bufferedReader().readText()
}