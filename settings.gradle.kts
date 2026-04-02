pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        maven { url = uri("https://artifactory.rnd.meizu.com/artifactory/all") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven {
            url = uri("http://artifactory.rnd.meizu.com/artifactory/XJSD_ALL")
            isAllowInsecureProtocol = true
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://artifactory.rnd.meizu.com/artifactory/all") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven {
            url = uri("http://artifactory.rnd.meizu.com/artifactory/XJSD_ALL")
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "LoginApp"
include(":app")
