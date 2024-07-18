@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import domain.entities.Project
import domain.entities.ProjectType
import domain.entities.SupportedPlatform
import kmp_project.composeapp.generated.resources.*
import kmp_project.composeapp.generated.resources.Res
import kmp_project.composeapp.generated.resources.brightness
import kmp_project.composeapp.generated.resources.github
import kmp_project.composeapp.generated.resources.search
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import presentation.viewmodels.MainViewModel
import ui.theme.darkScheme
import ui.theme.lightScheme

class MainScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: MainViewModel = koinInject()
        val fullProjects by viewModel.fullProjects
        val filteredProject by viewModel.filteredProjects
        val loading by viewModel.loading
        val error by viewModel.errorMessage
        val themeSwitched by viewModel.themeSwitched
        val screenWidth = LocalWindowInfo.current.containerSize.width.dp

        if (fullProjects.isEmpty() && loading.not() && error == null) {
            viewModel.loadProjects()
        }

        val isNarrowScreen = screenWidth in 0.dp..1199.dp

        val colors = if (isSystemInDarkTheme() && themeSwitched.not()) {
            darkScheme
        } else {
            lightScheme
        }

        MaterialTheme(colorScheme = colors) {
            if (isNarrowScreen) {
                ContentWidget(filteredProject, showTopAction = true)
            } else {
                WideScreen(filteredProject)
            }
        }
    }
}

@Composable
fun WideScreen(projects: List<Project>) {
    val viewModel: MainViewModel = koinInject()
    val platformChecks by viewModel.platformChecks
    val typeChecks by viewModel.typeChecks

    val platformItems = listOf(
        SupportedPlatform.Android to platformChecks.androidChecked,
        SupportedPlatform.iOS to platformChecks.iosChecked,
        SupportedPlatform.Desktop to platformChecks.desktopChecked,
        SupportedPlatform.Web to platformChecks.webChecked
    )

    val typeItems = listOf(
        ProjectType.Library to typeChecks.libraryChecked,
        ProjectType.Showcase to typeChecks.showcaseChecked,
        ProjectType.Framework to typeChecks.frameworkChecked,
        ProjectType.Tool to typeChecks.toolChecked,
        ProjectType.Other to typeChecks.otherChecked
    )

    PermanentNavigationDrawer(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(240.dp)) {
                Text("Platforms", modifier = Modifier.padding(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                platformItems.forEach { (platform, checked) ->
                    DrawerCheckboxItem(label = platform.name, checked = checked) { newCheckState ->
                        when (platform) {
                            SupportedPlatform.Android -> viewModel.applyPlatformFilter(
                                platformChecks.copy(
                                    androidChecked = newCheckState
                                )
                            )

                            SupportedPlatform.iOS -> viewModel.applyPlatformFilter(platformChecks.copy(iosChecked = newCheckState))
                            SupportedPlatform.Desktop -> viewModel.applyPlatformFilter(
                                platformChecks.copy(
                                    desktopChecked = newCheckState
                                )
                            )

                            SupportedPlatform.Web -> viewModel.applyPlatformFilter(platformChecks.copy(webChecked = newCheckState))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Types", modifier = Modifier.padding(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                typeItems.forEach { (type, checked) ->
                    DrawerCheckboxItem(label = type.name, checked = checked) { newCheckState ->
                        when (type) {
                            ProjectType.Library -> viewModel.applyTypeFilter(typeChecks.copy(libraryChecked = newCheckState))
                            ProjectType.Showcase -> viewModel.applyTypeFilter(typeChecks.copy(showcaseChecked = newCheckState))
                            ProjectType.Framework -> viewModel.applyTypeFilter(typeChecks.copy(frameworkChecked = newCheckState))
                            ProjectType.Tool -> viewModel.applyTypeFilter(typeChecks.copy(toolChecked = newCheckState))
                            ProjectType.Other -> viewModel.applyTypeFilter(typeChecks.copy(otherChecked = newCheckState))
                        }
                    }
                }
            }
        }
    ) {
        // Screen content
        ContentWidget(projects, showTopAction = false)
    }
}


@Composable
fun DrawerCheckboxItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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

@Composable
fun ContentWidget(projects: List<Project>, showTopAction: Boolean) {
    val viewModel = koinInject<MainViewModel>()
    val showFilterDialog by viewModel.showFilterDialog
    val searchKeyword by viewModel.searchKeyword
    Scaffold(topBar = { MainTopBar(showTopAction) }, bottomBar = { MainBottomBar() }) { paddingValues ->
        Column(
            Modifier.fillMaxWidth().padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = searchKeyword,
                onValueChange = { newValue -> viewModel.applySearchKeyword(newValue) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                singleLine = true,
                shape = CircleShape,
                label = { Text("Type to search...") },
                leadingIcon = { Icon(painterResource(Res.drawable.search), null) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            LazyColumn {
                items(projects) {
                    ItemWidget(it)
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog()
        }
    }
}

@Composable
fun DialogCheckboxItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onCheckedChange(!checked) }) {
        Checkbox(checked, onCheckedChange = onCheckedChange)
        Text(label, modifier = Modifier.padding(end = 16.dp))
    }
}

@Composable
fun FilterDialog() {
    val viewModel = koinInject<MainViewModel>()
    val dialogPlatformChecks = viewModel.dialogSelectedPlatformChecks
    val dialogSelectedTypes = viewModel.dialogSelectedTypeChecks
    BasicAlertDialog(
        modifier = Modifier.defaultMinSize(minWidth = 280.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(28.dp)),
        onDismissRequest = {},
        properties = DialogProperties()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Filters", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
            Spacer(modifier = Modifier.height(8.dp))
            // platform
            DialogCheckboxItem(SupportedPlatform.Android.name, dialogPlatformChecks.value.androidChecked) { newState ->
                viewModel.submitSelectedPlatforms(dialogPlatformChecks.value.copy(androidChecked = newState))
            }
            DialogCheckboxItem(SupportedPlatform.iOS.name, dialogPlatformChecks.value.iosChecked) { newState ->
                viewModel.submitSelectedPlatforms(dialogPlatformChecks.value.copy(iosChecked = newState))
            }
            DialogCheckboxItem(SupportedPlatform.Desktop.name, dialogPlatformChecks.value.desktopChecked) { newState ->
                viewModel.submitSelectedPlatforms(dialogPlatformChecks.value.copy(desktopChecked = newState))
            }
            DialogCheckboxItem(SupportedPlatform.Web.name, dialogPlatformChecks.value.webChecked) { newState ->
                viewModel.submitSelectedPlatforms(dialogPlatformChecks.value.copy(webChecked = newState))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            // type
            DialogCheckboxItem(ProjectType.Library.name, dialogSelectedTypes.value.libraryChecked) { newState ->
                viewModel.submitSelectedTypes(dialogSelectedTypes.value.copy(libraryChecked = newState))
            }
            DialogCheckboxItem(ProjectType.Showcase.name, dialogSelectedTypes.value.showcaseChecked) { newState ->
                viewModel.submitSelectedTypes(dialogSelectedTypes.value.copy(showcaseChecked = newState))
            }
            DialogCheckboxItem(ProjectType.Framework.name, dialogSelectedTypes.value.frameworkChecked) { newState ->
                viewModel.submitSelectedTypes(dialogSelectedTypes.value.copy(frameworkChecked = newState))
            }
            DialogCheckboxItem(ProjectType.Tool.name, dialogSelectedTypes.value.toolChecked) { newState ->
                viewModel.submitSelectedTypes(dialogSelectedTypes.value.copy(toolChecked = newState))
            }
            DialogCheckboxItem(ProjectType.Other.name, dialogSelectedTypes.value.otherChecked) { newState ->
                viewModel.submitSelectedTypes(dialogSelectedTypes.value.copy(otherChecked = newState))
            }
            Row(modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = {
                    viewModel.showFilterDialog.value = false
                    viewModel.cancelFilter()
                }) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    viewModel.showFilterDialog.value = false
                    viewModel.applySelectedFilter()
                }) { Text("Apply") }
            }
        }
    }
}

@Composable
fun MainBottomBar() {
    val viewModel = koinInject<MainViewModel>()
    val handler = LocalUriHandler.current
    BottomAppBar {
        IconButton(onClick = { handler.openUri("https://github.com/KMP-Hub") }) {
            Icon(
                painterResource(Res.drawable.github),
                "GitHub"
            )
        }
        IconButton(onClick = { viewModel.themeSwitched.apply { value = value.not() } }) {
            Icon(
                painterResource(Res.drawable.brightness),
                "Switch theme"
            )
        }
    }
}

@Composable
fun MainTopBar(showTopAction: Boolean) {
    val viewModel = koinInject<MainViewModel>()
    val showFilterDialog = viewModel.showFilterDialog
    TopAppBar(
        title = {
            Text(
                "KMP Projects Hub",
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        actions = {
            if (showTopAction) {
                IconButton(onClick = { showFilterDialog.apply { value = value.not() } }) {
                    Icon(
                        painterResource(Res.drawable.adjustment),
                        "Filter adjustment"
                    )
                }
            }
        }
    )
}

@Composable
fun ItemWidget(project: Project) {
    val uriHandler = LocalUriHandler.current
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp), onClick = {
        uriHandler.openUri(project.url)
    }) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                project.name,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )
            Text(project.description, fontSize = MaterialTheme.typography.bodyLarge.fontSize)
            Text(
                project.tags.joinToString(prefix = "#", separator = ", #"),
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
            LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, top = 8.dp)) {
                items(project.platforms) {
                    PlatformChip(it.name)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun PlatformChip(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            style = trimmedTextStyle
        )
    }
}

val trimmedTextStyle = TextStyle(
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Proportional,
        trim = LineHeightStyle.Trim.Both
    )
)