plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11"

stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

    constants["v1214plus"] = current.version == "1.21.4" || current.version == "1.21.11"
    constants["v12111plus"] = current.version == "1.21.11"
}
