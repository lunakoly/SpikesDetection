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
    implementation(project(":arrrgh"))
    testImplementation(libs.kotlin.test.junit)
}
