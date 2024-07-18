@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

package presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import domain.entities.Project
import domain.entities.ProjectType
import domain.entities.SupportedPlatform
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
        val projects by viewModel.projects
        val loading by viewModel.loading
        val error by viewModel.errorMessage
        val themeSwitched by viewModel.themeSwitched
        val screenWidth = LocalWindowInfo.current.containerSize.width.dp

        if (projects.isEmpty() && loading.not() && error == null) {
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
                ContentWidget(projects, showTopActions = true)
            } else {
                WideScreen(projects)
            }
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
        SupportedPlatform.Android to androidChecked,
        SupportedPlatform.iOS to iosChecked,
        SupportedPlatform.Desktop to desktopChecked,
        SupportedPlatform.Web to webChecked
    )

    val typeItems = listOf(
        ProjectType.Library to libraryChecked,
        ProjectType.Showcase to showcaseChecked,
        ProjectType.Framework to frameworkChecked,
        ProjectType.Tool to toolChecked,
        ProjectType.Other to otherChecked
    )

    PermanentNavigationDrawer(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(240.dp)) {
                Text("Platforms", modifier = Modifier.padding(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                platformItems.forEachIndexed { index, (platform, checked) ->
                    CheckboxItem(label = platform.name, checked = checked) {
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
                typeItems.forEachIndexed { index, (type, checked) ->
                    CheckboxItem(label = type.name, checked = checked) {
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

@Composable
fun ContentWidget(projects: List<Project>, showTopActions: Boolean) {
    val inputValue = remember { mutableStateOf(TextFieldValue()) }
    Scaffold(topBar = { MainTopBar() }) { paddingValues ->
        Column(
            Modifier.fillMaxWidth().padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            ), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = inputValue.value,
                onValueChange = { newValue -> inputValue.value = newValue },
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
    }
}

@Composable
fun MainTopBar() {
    val viewModel = koinInject<MainViewModel>()
    val handler = LocalUriHandler.current
    TopAppBar(
        title = { Text("KMP Projects", color = MaterialTheme.colorScheme.onBackground) },
        actions = {
            IconButton(onClick = { handler.openUri("https://github.com/KMP-Hub") }) {
                Icon(
                    painterResource(Res.drawable.github),
                    "GitHub repo"
                )
            }
            IconButton(onClick = { viewModel.themeSwitched.apply { value = value.not() } }) {
                Icon(
                    painterResource(Res.drawable.brightness),
                    "GitHub repo"
                )
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
                project.tags.joinToString("  "),
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