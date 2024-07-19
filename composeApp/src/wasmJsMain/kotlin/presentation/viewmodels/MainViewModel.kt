package presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import domain.entities.Project
import domain.entities.ProjectType
import domain.entities.SupportedPlatform
import domain.repositories.ProjectRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ProjectRepository) : ScreenModel {
    val fullProjects = mutableStateOf(emptyList<Project>())
    val filteredProjects = mutableStateOf(emptyList<Project>())
    val searchKeyword = mutableStateOf(TextFieldValue())
    val loading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val themeSwitched = mutableStateOf(false)
    val showFilterDialog = mutableStateOf(false)
    val platformChecks = mutableStateOf(PlatformCheckState())
    val typeChecks = mutableStateOf(TypeCheckState())
    val dialogSelectedPlatformChecks = mutableStateOf(PlatformCheckState())
    val dialogSelectedTypeChecks = mutableStateOf(TypeCheckState())

    fun loadProjects() {
        loading.value = true
        screenModelScope.launch {
            repository.fetchProjects().fold(
                onSuccess = {
                    fullProjects.value = it
                    filter()
                    loading.value = false
                },
                onFailure = {
                    errorMessage.value = "Failed to load projects"
                    loading.value = false
                    print(it.message)
                }
            )
        }
    }

    fun applyPlatformFilter(state: PlatformCheckState) {
        platformChecks.value = state
        dialogSelectedPlatformChecks.value = state
        filter()
    }

    fun applyTypeFilter(state: TypeCheckState) {
        typeChecks.value = state
        dialogSelectedTypeChecks.value = state
        filter()
    }

    private fun filter() {
        val selectedPlatforms = mutableListOf<SupportedPlatform>()
        if (platformChecks.value.androidChecked) selectedPlatforms.add(SupportedPlatform.Android)
        if (platformChecks.value.iosChecked) selectedPlatforms.add(SupportedPlatform.iOS)
        if (platformChecks.value.desktopChecked) selectedPlatforms.add(SupportedPlatform.Desktop)
        if (platformChecks.value.webChecked) selectedPlatforms.add(SupportedPlatform.Web)

        val selectedTypes = mutableListOf<ProjectType>()
        if (typeChecks.value.libraryChecked) selectedTypes.add(ProjectType.Library)
        if (typeChecks.value.showcaseChecked) selectedTypes.add(ProjectType.Showcase)
        if (typeChecks.value.frameworkChecked) selectedTypes.add(ProjectType.Framework)
        if (typeChecks.value.toolChecked) selectedTypes.add(ProjectType.Tool)
        if (typeChecks.value.otherChecked) selectedTypes.add(ProjectType.Other)

        val filteredProjects = fullProjects.value.filter {
            it.platforms.any { it in selectedPlatforms }
                    && it.types.any { it in selectedTypes }
        }
        val text = searchKeyword.value.text
        if (text.trim().isNotEmpty()) {
            this.filteredProjects.value = filteredProjects.filter {
                it.name.contains(text) ||
                        it.description?.contains(text) == true ||
                        it.tags.any { it.contains(text) }
            }
        } else {
            this.filteredProjects.value = filteredProjects
        }
    }

    fun applySearchKeyword(newValue: TextFieldValue) {
        searchKeyword.value = newValue
        filter()
    }

    fun submitSelectedPlatforms(state: PlatformCheckState) {
        dialogSelectedPlatformChecks.value = state
    }

    fun submitSelectedTypes(state: TypeCheckState) {
        dialogSelectedTypeChecks.value = state
    }

    fun applySelectedFilter() {
        platformChecks.value = dialogSelectedPlatformChecks.value
        typeChecks.value = dialogSelectedTypeChecks.value
        filter()
    }

    fun cancelFilter() {
        dialogSelectedPlatformChecks.value = platformChecks.value
        dialogSelectedTypeChecks.value = typeChecks.value
    }
}

data class PlatformCheckState(
    val androidChecked: Boolean = true,
    val iosChecked: Boolean = true,
    val desktopChecked: Boolean = true,
    val webChecked: Boolean = true,
)

data class TypeCheckState(
    val libraryChecked: Boolean = true,
    val toolChecked: Boolean = true,
    val frameworkChecked: Boolean = true,
    val showcaseChecked: Boolean = true,
    val otherChecked: Boolean = true,
)