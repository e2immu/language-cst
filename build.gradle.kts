tasks.register("test") {
    dependsOn(gradle.includedBuild("e2immu-external-support").task(":test"))
    dependsOn(gradle.includedBuild("e2immu-internal-graph").task(":test"))
    dependsOn(gradle.includedBuild("e2immu-cst-impl").task(":test"))
    dependsOn(gradle.includedBuild("e2immu-cst-io").task(":test"))
    dependsOn(gradle.includedBuild("e2immu-cst-print").task(":test"))
}
tasks.register("clean") {
    dependsOn(gradle.includedBuilds.map { it.task(":clean") })
}
tasks.register("publish") {
    dependsOn(gradle.includedBuild("e2immu-cst-api").task(":publish"))
    dependsOn(gradle.includedBuild("e2immu-cst-io").task(":publish"))
    dependsOn(gradle.includedBuild("e2immu-cst-impl").task(":publish"))
    dependsOn(gradle.includedBuild("e2immu-cst-print").task(":publish"))
    dependsOn(gradle.includedBuild("e2immu-cst-analysis").task(":publish"))
}
tasks.register("publishToMavenLocal") {
    dependsOn(gradle.includedBuild("e2immu-cst-api").task(":publishToMavenLocal"))
    dependsOn(gradle.includedBuild("e2immu-cst-io").task(":publishToMavenLocal"))
    dependsOn(gradle.includedBuild("e2immu-cst-impl").task(":publishToMavenLocal"))
    dependsOn(gradle.includedBuild("e2immu-cst-print").task(":publishToMavenLocal"))
    dependsOn(gradle.includedBuild("e2immu-cst-analysis").task(":publishToMavenLocal"))
}
