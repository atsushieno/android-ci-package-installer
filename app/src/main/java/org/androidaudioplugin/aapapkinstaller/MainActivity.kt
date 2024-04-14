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
        // FIXME: this should include host app packages by some means
        AppModel.findExistingPackages = { context -> queryInstalledAudioPluginPackages(context) }
    }

    @Suppress("SpellCheckingInspection")
    private fun setupApkList() {
        val store = AppModel.githubApplicationStore
        store.githubRepositories.addAll(listOf(
            GitHubRepositoryInformation(store, "atsushieno", "android-ci-package-installer",
                "org.androidaudioplugin.aapapkinstaller", "AAP APK Installer"),
            GitHubRepositoryInformation(store, "atsushieno", "resident-midi-keyboard",
                "org.androidaudioplugin.resident_midi_keyboard", "Resident MIDI Keyboard"),
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
            GitHubRepositoryInformation(store, "atsushieno", "aap-lv2-aida-x",
                "org.androidaudioplugin.ports.lv2.aida_x", "AAP AIDA-X"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-plugin-host",
                "org.androidaudioplugin.ports.juce.audiopluginhost", "AAP AudioPluginHost"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-simple-host",
                "org.androidaudioplugin.samples.simple_plugin_host", "AAP SimpleHost"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-helio",
                "org.androidaudioplugin.ports.juce.helio", "AAP Helio"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-obxd",
                "org.androidaudioplugin.ports.juce.ob_xd", "AAP OB-Xd"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-dexed",
                "org.androidaudioplugin.ports.juce.dexed", "AAP Dexed"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-adlplug-ae",
                "org.androidaudioplugin.ports.juce.adlplug_ae", "AAP ADLplug-AE"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-hera",
                "org.androidaudioplugin.ports.juce.hera", "AAP Hera"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-odin2",
                "org.androidaudioplugin.ports.juce.odin2", "AAP Odin2"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-vital",
                "org.androidaudioplugin.ports.juce.vital", "AAP Vitaloid"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-os251",
                "org.androidaudioplugin.ports.juce.os251", "AAP OS-251"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-witte-eq",
                "org.androidaudioplugin.ports.juce.witte_ew", "AAP witte/Eq"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-simple-reverb",
                "org.androidaudioplugin.ports.juce.simple_reverb", "AAP SimpleReverb"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-chow-phaser",
                "org.androidaudioplugin.ports.juce.chow_phaser", "AAP ChowPhaser"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-frequalizer",
                "org.androidaudioplugin.ports.juce.frequalizer", "AAP Frequalizer"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-byod",
                "org.androidaudioplugin.ports.juce.byod", "AAP BYOD"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-xenos",
                "org.androidaudioplugin.ports.juce.xenos", "AAP Xenos"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-ddsp",
                "org.androidaudioplugin.ports.juce.ddsp_vst", "AAP DDSP-VST"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-surge",
                "org.androidaudioplugin.ports.juce.surge", "AAP Surge-XT"),
            GitHubRepositoryInformation(store, "atsushieno", "aap-juce-audible-planets",
                "org.androidaudioplugin.ports.juce.audibleplanets", "AAP Audible Planets"),
        ))
    }

    private fun queryInstalledAudioPluginPackages(context: Context, packageNameFilter: String? = null): List<String> {
        val AAP_ACTION_NAME = "org.androidaudioplugin.AudioPluginService.V3"

        val intent = Intent(AAP_ACTION_NAME)
        if (packageNameFilter != null)
            intent.setPackage(packageNameFilter)
        return context.packageManager.queryIntentServices(intent, 0).map { it.serviceInfo.packageName }
            .distinct()
    }
}