package domain.interactors

import domain.entities.Project
import domain.repositories.ProjectRepository

class GetProjects(private val repository: ProjectRepository) {
    suspend operator fun invoke(): Result<List<Project>> {
        return repository.fetchProjects()
    }
}