package domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Project(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String?,
    @SerialName("platforms")
    val platforms: List<SupportedPlatform>,
    @SerialName("types")
    val types: List<ProjectType>,
    @SerialName("html_url")
    val url: String,
    @SerialName("topics")
    val tags: List<String>,
    @SerialName("stargazers_count")
    val starCount: Int,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("created_at")
    val createdAt: String,
)

enum class SupportedPlatform {
    Android, iOS, Desktop, Web
}

enum class ProjectType {
    Library, Showcase, Framework, Tool, Other
}