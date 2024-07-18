@file:OptIn(
    ExperimentalMaterial3Api::class
)

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import domain.entities.Project
import kmp_project.composeapp.generated.resources.Res
import kmp_project.composeapp.generated.resources.github
import kmp_project.composeapp.generated.resources.search
import modules.dataModule
import modules.repositoryModule
import modules.useCaseModule
import modules.viewModelModule
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import presentation.screens.MainScreen
import ui.theme.darkScheme
import ui.theme.lightScheme


@Composable
fun App() {
    KoinApplication(application = {
        modules(
            listOf(
                dataModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        )
    }) {
        val colors = if (isSystemInDarkTheme()) {
            darkScheme
        } else {
            lightScheme
        }

        MaterialTheme(colorScheme = colors) {
            Navigator(MainScreen())
        }
    }
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