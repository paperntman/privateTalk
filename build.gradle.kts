plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.github.cdimascio:java-dotenv:5.2.2")
    implementation("net.dv8tion:JDA:5.1.2")
    implementation("org.jetbrains.trove4j:trove4j:20160824")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    finalizedBy(tasks.shadowJar) // shadowJar 작업을 완료한 후 실행
    manifest {
        attributes("Main-Class" to "org.example.Main")
    }
    from(configurations.compileClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE // 필요에 따라 설정
}

tasks.shadowJar {
    archiveBaseName.set("privateTalk")
    archiveVersion.set("1.0-SNAPSHOT")
    archiveClassifier.set("")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}