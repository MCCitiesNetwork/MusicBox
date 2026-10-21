plugins { `java-library` }
java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8"; options.release.set(25) }
dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.206-stable")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }
    compileOnly("com.github.koca2000:NoteBlockAPI:1.6.3") { isTransitive = false }
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    implementation("org.incendo:cloud-paper:2.0.1")
    compileOnly("net.kyori:adventure-nbt:4.26.1")
    testImplementation("net.kyori:adventure-nbt:4.26.1")
    testImplementation("com.github.MilkBowl:VaultAPI:1.7") { isTransitive = false }
    implementation("org.bstats:bstats-bukkit:3.0.2")
    testImplementation(platform("org.junit:junit-bom:5.14.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.papermc.paper:paper-api:26.3.build.206-stable")
    testImplementation("com.github.koca2000:NoteBlockAPI:1.6.3") { isTransitive = false }
    testImplementation("org.xerial:sqlite-jdbc:3.50.3.0")
}
tasks.test { useJUnitPlatform { excludeTags("paper-runtime") } }
tasks.processResources {
    exclude("songs/**")
    filesMatching("paper-plugin.yml") { expand("version" to rootProject.version) }
}

// Optional integration check against an unpacked Paper runtime and its library jars.
// The classpath file contains one absolute jar path per line; no server is started.
tasks.register<Test>("paperRuntimeTest") {
    group = "verification"
    description = "Initializes Cloud's native Paper mappings against supplied runtime jars without starting a server"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath + files(
        providers.gradleProperty("paperRuntimeClasspathFile").map { file(it).readLines().filter(String::isNotBlank) }
    )
    useJUnitPlatform { includeTags("paper-runtime") }
    val probeDirectory = providers.gradleProperty("paperRuntimeWorkingDirectory")
        .map { file(it) }.getOrElse(layout.buildDirectory.dir("paper-runtime-test").get().asFile)
    workingDir = probeDirectory
    doFirst { probeDirectory.mkdirs() }
    maxHeapSize = "2g"
}
