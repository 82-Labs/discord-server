plugins {
    `java-test-fixtures`
}

dependencies {
    api(project(":common"))
    
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    
    testImplementation(testFixtures(project(":domain")))
}