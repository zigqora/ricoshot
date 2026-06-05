plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.4"

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    constants["v1214plus"] = current.parsed >= "1.21.4"
}
