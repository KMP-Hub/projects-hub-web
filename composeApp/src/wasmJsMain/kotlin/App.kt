import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import modules.dataModule
import modules.repositoryModule
import modules.useCaseModule
import modules.viewModelModule
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
        Navigator(MainScreen())
    }
}
