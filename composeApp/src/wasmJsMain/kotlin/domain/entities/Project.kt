package domain.entities

class Project(
    val name: String,
    val description: String,
    val platforms: List<SupportedPlatform>,
    val url: String,
    val tags: List<String>,
)

enum class SupportedPlatform {
    Android, iOS, Desktop, Web
}
