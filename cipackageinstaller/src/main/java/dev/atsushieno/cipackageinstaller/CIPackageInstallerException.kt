package dev.atsushieno.cipackageinstaller

@Suppress("unused")
class CIPackageInstallerException : Exception {
    constructor() : this("CIPackageInstallerException occurred")
    constructor(message: String) : this (message, null)
    constructor(message: String, innerException: Exception?) : super(message, innerException)
}