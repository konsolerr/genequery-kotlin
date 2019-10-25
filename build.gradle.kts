plugins {
    base
    kotlin("jvm") version "1.3.50" apply false
}

apply(plugin = "idea")

allprojects {
    group = "ru.ifmo.genequery"
    version = "1.1-SNAPSHOT"

    repositories {
        jcenter()
    }

}