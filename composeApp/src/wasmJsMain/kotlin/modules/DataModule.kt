package modules

import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    single { Json { ignoreUnknownKeys = true } }
    single { HttpClient() }
}