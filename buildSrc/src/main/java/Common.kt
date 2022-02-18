import org.gradle.api.Project
import java.util.*

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