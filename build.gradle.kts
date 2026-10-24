plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.0"
}
group = "com.minecraftcitiesnetwork"
version = "3.0.0-SNAPSHOT"
allprojects {
    group = "com.minecraftcitiesnetwork"
    version = rootProject.version
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
    }
}
dependencies { implementation(project(":plugin")) }
tasks.shadowJar {
    archiveClassifier.set("")
    relocate("org.incendo", "com.minecraftcitiesnetwork.musicbox.internal.cloud")
    relocate("org.bstats", "com.minecraftcitiesnetwork.musicbox.internal.bstats")
    relocate("net.kyori.adventure.nbt", "com.minecraftcitiesnetwork.musicbox.internal.nbt")
    mergeServiceFiles()
}
tasks.jar { archiveClassifier.set("thin") }
tasks.assemble { dependsOn(tasks.shadowJar) }
