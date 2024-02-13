plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

group = "lunakoly.frequencies"
version = "1.0.0"

application {
    mainClass.set("lunakoly.frequencies.MainKt")
}

dependencies {
    implementation(libs.kandy)
    runtimeOnly(libs.slf4j.nop)

    implementation(project(":arrrgh"))
    testImplementation(libs.kotlin.test.junit)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    )
}
