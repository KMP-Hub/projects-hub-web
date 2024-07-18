package modules

import domain.repositories.ProjectRepository
import org.koin.dsl.module
import presentation.repositories.ProjectRepositoryImpl

val repositoryModule = module {
    single<ProjectRepository> { ProjectRepositoryImpl(get(), get()) }
}