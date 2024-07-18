@file:OptIn(ExperimentalComposeUiApi::class)

package presentation.screens

import ContentWidget
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import domain.entities.Project
import domain.entities.ProjectType
import domain.entities.SupportedPlatform
import org.koin.compose.koinInject
import presentation.viewmodels.MainViewModel

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: MainViewModel = koinInject()
        val projects by viewModel.projects
        val loading by viewModel.loading
        val error by viewModel.errorMessage
        val screenWidth = LocalWindowInfo.current.containerSize.width.dp

        if (projects.isEmpty() && loading.not() && error == null) {
            viewModel.loadProjects()
        }

        val isNarrowScreen = screenWidth in 0.dp..1199.dp

        if (isNarrowScreen) {
            ContentWidget(projects, showTopActions = true)
        } else {
            WideScreen(projects)
        }
    }
}

@Composable
fun WideScreen(projects: List<Project>) {
    // platform
    var androidChecked by remember { mutableStateOf(true) }
    var iosChecked by remember { mutableStateOf(true) }
    var desktopChecked by remember { mutableStateOf(true) }
    var webChecked by remember { mutableStateOf(true) }

    // type
    var libraryChecked by remember { mutableStateOf(true) }
    var showcaseChecked by remember { mutableStateOf(true) }
    var frameworkChecked by remember { mutableStateOf(true) }
    var toolChecked by remember { mutableStateOf(true) }
    var otherChecked by remember { mutableStateOf(true) }

    val platformItems = listOf(
        SupportedPlatform.Android.name to androidChecked,
        SupportedPlatform.iOS.name to iosChecked,
        SupportedPlatform.Desktop.name to desktopChecked,
        SupportedPlatform.Web.name to webChecked
    )

    val typeItems = listOf(
        ProjectType.Library.name to libraryChecked,
        ProjectType.Showcase.name to showcaseChecked,
        ProjectType.Framework.name to frameworkChecked,
        ProjectType.Tool.name to toolChecked,
        ProjectType.Other.name to otherChecked
    )

    PermanentNavigationDrawer(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(240.dp)) {
                Text("Platforms", modifier = Modifier.padding(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                platformItems.forEachIndexed { index, (platformName, checked) ->
                    CheckboxItem(label = platformName, checked = checked) {
                        when (index) {
                            0 -> androidChecked = it
                            1 -> iosChecked = it
                            2 -> desktopChecked = it
                            3 -> webChecked = it
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Types", modifier = Modifier.padding(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                typeItems.forEachIndexed { index, (typeName, checked) ->
                    CheckboxItem(label = typeName, checked = checked) {
                        when (index) {
                            0 -> libraryChecked = it
                            1 -> showcaseChecked = it
                            2 -> frameworkChecked = it
                            3 -> toolChecked = it
                            4 -> otherChecked = it
                        }
                    }
                }
            }
        }
    ) {
        // Screen content
        ContentWidget(projects, showTopActions = false)
    }
}


@Composable
fun CheckboxItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    NavigationDrawerItem(
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                Text(text = label)
            }
        },
        selected = false,
        onClick = { onCheckedChange(!checked) }
    )
}