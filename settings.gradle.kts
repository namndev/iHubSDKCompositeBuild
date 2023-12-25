pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter() // fotoapparat 2.7.0
    }
}

rootProject.name = "Eid Demo"
include(":app")
includeBuild("eidscan-remote")

rootDir.run {
    listOf(
        "gradle.properties",
        "gradlew.bat",
        "gradlew",
        "gradle/wrapper/gradle-wrapper.jar",
        "gradle/wrapper/gradle-wrapper.properties"
    ).map { path ->
        resolve(path)
            .copyTo(
                target = rootDir.resolve("eidscan-remote").resolve(path),
                overwrite = true
            )
    }
}
