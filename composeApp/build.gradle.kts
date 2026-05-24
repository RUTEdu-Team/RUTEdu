//import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier // optional
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.sqldelight)
}

kotlin {
    android {
        namespace = "prz.rutedu.app.library"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources {
            enable = true
        }
        withHostTest {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.ui.tooling.preview)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.matheclipse.core)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.material.icons.extended)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}


sqldelight {
    databases {
        create("Database") {
            packageName.set("prz.rutedu.app")
            generateAsync.set(true)
        }
    }
}

dokka {
    moduleName.set("RUTEdu")

    dokkaSourceSets.configureEach {
        perPackageOption {
            matchingRegex.set("rutedu\\.composeapp\\.generated\\..*")
            suppress.set(true)
        }
        perPackageOption {
            matchingRegex.set("prz\\\\.rutedu\\\\.app\\\\.Database.*")
            suppress.set(true)
        }

        /*documentedVisibilities.set(setOf(
            VisibilityModifier.Public,
            VisibilityModifier.Private,
            VisibilityModifier.Protected,
            VisibilityModifier.Internal
        ))*/

        jdkVersion.set(17)

        reportUndocumented.set(true)
    }
}