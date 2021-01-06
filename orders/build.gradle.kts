plugins {
    java
    application
}

group = "com.gql.microservices.orders"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

var cdkVersion = "1.74.0"

dependencies {
    testCompile("junit", "junit", "4.12")

    // This dependency is used by the application.
    implementation(group = "software.amazon.awscdk", name = "core", version = cdkVersion)
    implementation(group = "software.amazon.awscdk", name = "appsync", version = cdkVersion)

    implementation(group = "software.amazon.awssdk", name = "cloudformation", version = "2.15.33")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    // Define the main class for the application.
    mainClassName = "com.gql.microservices.orders.Manager"
}