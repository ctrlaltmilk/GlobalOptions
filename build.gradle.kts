plugins {
    id("net.neoforged.gradle.userdev") version "7.0+"
}

version = "0.1.0"
group = "net.ctrlaltmilk"

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

runs {
    configureEach {
        systemProperty("terminal.jline", "true")
    }
}

dependencies {
    implementation("net.neoforged:neoforge:21.1.115")
}

tasks {
    processResources {
        exclude("*.kra") // Original Krita textures
    }

    jar {
        manifest {
            attributes("Implementation-Version" to project.version)
        }
    }
}
