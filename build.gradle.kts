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
    dependsOn(gradle.includedBuilds.map { it.task(":publish") })
}
