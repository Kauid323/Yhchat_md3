pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        
        // 阿里云Maven镜像

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Yhchat Canary"
include(":app")
