plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
}

kotlin {


    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SejmierzShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":domain"))
            api(project(":data"))
            api(project(":ui"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
    }
}
