import org.gradle.api.Project
import java.util.Properties

fun getEnvOrLocalSecret(project: Project, name: String, secretName: String): String {
    return System.getenv()[name]
        ?: getLocalSecret(project, secretName)
        ?: throw IllegalArgumentException("Variable '$name' nor '$secretName' not found!")

}

private fun getLocalSecret(project: Project, secretName: String): String? {
    val props = Properties()
    project.file("secrets.properties").inputStream().use { props.load(it) }
    return props.getProperty(secretName)
}