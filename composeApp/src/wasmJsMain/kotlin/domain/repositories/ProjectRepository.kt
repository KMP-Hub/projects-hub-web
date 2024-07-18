package domain.repositories

import domain.entities.Project

interface ProjectRepository {
    suspend fun fetchProjects(): Result<List<Project>>
}