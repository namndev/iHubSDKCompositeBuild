plugins {
    base
}

configurations.maybeCreate("default")
artifacts.add("default", file("eidsdk-release.aar"))

group = "com.ihub.eidsdk"
version = "1.0.0"