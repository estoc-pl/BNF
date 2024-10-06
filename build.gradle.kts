plugins {
    kotlin("multiplatform")
}

val kotlinVersion: String by properties

kotlin {
    linuxX64()

    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")

    when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64()
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64()
        hostOs == "Linux" -> linuxX64()
        else -> throw GradleException("Host OS is not supported")
    }

    sourceSets {
        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test-common:$kotlinVersion")
        }
    }
}