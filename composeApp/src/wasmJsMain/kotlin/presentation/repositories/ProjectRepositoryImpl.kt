package presentation.repositories

import domain.entities.Project
import domain.repositories.ProjectRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json

class ProjectRepositoryImpl(private val json: Json, private val client: HttpClient) : ProjectRepository {
    override suspend fun fetchProjects(): Result<List<Project>> {
        try {
            val response = client.get(JSON_URL).body<List<Project>>()
            return Result.success(response)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    companion object {
        const val JSON_URL = "https://raw.githubusercontent.com/KMP-Hub/Project-Hub-Data/main/projects.json"
    }
}