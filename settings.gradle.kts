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
    }
}

rootProject.name = "SIE-Android-App"
include(":app")
include(":core:common")
include(":core:data")
include(":core:model")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":feature:home")
include(":feature:study")
include(":feature:exam")
include(":feature:stats")
include(":feature:settings")
include(":feature:chapter")
