package org.androidaudioplugin.aapapkinstaller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dev.atsushieno.cipackageinstaller.AppModel
import dev.atsushieno.cipackageinstaller.CIPackageInstallerActivity
import dev.atsushieno.cipackageinstaller.GitHubRepositoryInformation
import dev.atsushieno.cipackageinstaller.GitHubRepositoryStore

class MainActivity : CIPackageInstallerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupApkList()
        AppModel.applicationStore.initialize(this)
        AppModel.findExistingPackages = { context -> queryInstalledAudioPluginPackages(context) }
    }

    @Suppress("SpellCheckingInspection")
    private fun setupApkList() {
        val store = AppModel.githubApplicationStore
        store.githubRepositories.addAll(listOf(
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-mda",
                "org.androidaudioplugin.ports.lv2.mda_lv2", "AAP MDA-LV2"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-sfizz",
                "org.androidaudioplugin.ports.lv2.sfizz", "AAP Sfizz"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-fluidsynth",
                "org.androidaudioplugin.ports.lv2.aap_fluidsynth", "AAP Fluidsynth"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-guitarix",
                "org.androidaudioplugin.ports.lv2.guitarix", "AAP Guitarix"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-dragonfly-reverb",
                "org.androidaudioplugin.ports.lv2.dragonfly_reverb", "AAP Dragonfly Reverb"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-string-machine",
                "org.androidaudioplugin.ports.lv2.string_machine", "AAP String Machine"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-obxd",
                "org.androidaudioplugin.ports.juce.obxd", "AAP OB-Xd"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-dexed",
                "org.androidaudioplugin.ports.juce.dexed", "AAP Dexed"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-adlplug",
                "org.androidaudioplugin.ports.juce.adlplug", "AAP ADLplug"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-hera",
                "org.androidaudioplugin.ports.juce.hera", "AAP Hera"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-odin2",
                "org.androidaudioplugin.ports.juce.odin2", "AAP Odin2"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-vital",
                "org.androidaudioplugin.ports.juce.vital", "AAP Vitaloid"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-os251",
                "org.androidaudioplugin.ports.juce.os251", "AAP OS-251"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-simple-reverb",
                "org.androidaudioplugin.ports.juce.simple_reverb", "AAP SimpleReverb"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-chow-phaser",
                "org.androidaudioplugin.ports.juce.chow_phaser", "AAP ChowPhaser"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-frequalizer",
                "org.androidaudioplugin.ports.juce.frequalizer", "AAP Frequalizer"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-byod",
                "org.androidaudioplugin.ports.juce.byod", "AAP BYOD"),
        ))
    }

    private fun queryInstalledAudioPluginPackages(context: Context, packageNameFilter: String? = null): List<String> {
        val AAP_ACTION_NAME = "org.androidaudioplugin.AudioPluginService.V2"

        val intent = Intent(AAP_ACTION_NAME)
        if (packageNameFilter != null)
            intent.setPackage(packageNameFilter)
        return context.packageManager.queryIntentServices(intent, 0).map { it.serviceInfo.packageName }
            .distinct()
    }
}