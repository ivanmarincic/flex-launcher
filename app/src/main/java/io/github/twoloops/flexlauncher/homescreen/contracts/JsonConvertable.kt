package io.github.twoloops.flexlauncher.homescreen.contracts


interface JsonConvertable<out ContentType> {
    fun toJson(): String
    fun fromJson(json: String): ContentType
}