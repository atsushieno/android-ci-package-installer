package org.androidaudioplugin.aapapkinstaller

import dev.atsushieno.cipackageinstaller.AppModel
import dev.atsushieno.cipackageinstaller.GitHubRepositoryInformation

object ApplicationCatalog {

    @Suppress("SpellCheckingInspection")
    fun setupApkList() {
        val store = AppModel.githubApplicationStore
        store.githubRepositories.clear()
        store.githubRepositories.addAll(listOf(
            GitHubRepositoryInformation("atsushieno", "android-ci-package-installer",
                "org.androidaudioplugin.aapapkinstaller", "AAP APK Installer"),
            GitHubRepositoryInformation("atsushieno", "resident-midi-keyboard",
                "org.androidaudioplugin.resident_midi_keyboard", "MIDI2 Keyboard"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-mda",
                "org.androidaudioplugin.ports.lv2.mda_lv2", "AAP MDA-LV2"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-sfizz",
                "org.androidaudioplugin.ports.lv2.sfizz", "AAP Sfizz"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-fluidsynth",
                "org.androidaudioplugin.ports.lv2.aap_fluidsynth", "AAP Fluidsynth"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-guitarix",
                "org.androidaudioplugin.ports.lv2.guitarix", "AAP Guitarix"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-dragonfly-reverb",
                "org.androidaudioplugin.ports.lv2.dragonfly_reverb", "AAP Dragonfly Reverb"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-string-machine",
                "org.androidaudioplugin.ports.lv2.string_machine", "AAP String Machine"),
            GitHubRepositoryInformation("atsushieno", "aap-lv2-aida-x",
                "org.androidaudioplugin.ports.lv2.aida_x", "AAP AIDA-X"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-plugin-host-cmake",
                "org.androidaudioplugin.ports.juce.audiopluginhost", "AAP AudioPluginHost"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-simple-host",
                "org.androidaudioplugin.samples.simple_plugin_host", "AAP SimpleHost"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-helio",
                "org.androidaudioplugin.ports.juce.helio", "AAP Helio"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-ob-x-ae",
                "org.androidaudioplugin.ports.juce.ob_x_ae", "AAP OB-X-AE"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-dexed",
                "org.androidaudioplugin.ports.juce.dexed", "AAP Dexed"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-adlplug-ae",
                "org.androidaudioplugin.ports.juce.adlplug_ae", "AAP ADLplug-AE"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-hera",
                "org.androidaudioplugin.ports.juce.hera", "AAP Hera"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-odin2",
                "org.androidaudioplugin.ports.juce.odin2", "AAP Odin2"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-vital",
                "org.androidaudioplugin.ports.juce.vitaloid", "AAP Vitaloid"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-os251",
                "org.androidaudioplugin.ports.juce.os251", "AAP OS-251"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-witte-eq",
                "org.androidaudioplugin.ports.juce.witte_ew", "AAP witte/Eq"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-simple-reverb",
                "org.androidaudioplugin.ports.juce.simple_reverb", "AAP SimpleReverb"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-chow-phaser",
                "org.androidaudioplugin.ports.juce.chow_phaser", "AAP ChowPhaser"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-frequalizer",
                "org.androidaudioplugin.ports.juce.frequalizer", "AAP Frequalizer"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-byod",
                "org.androidaudioplugin.ports.juce.byod", "AAP BYOD"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-xenos",
                "org.androidaudioplugin.ports.juce.xenos", "AAP Xenos"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-ddsp",
                "org.androidaudioplugin.ports.juce.ddsp_vst", "AAP DDSP-VST"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-surge",
                "org.androidaudioplugin.ports.juce.surge_xt", "AAP Surge-XT"),
            GitHubRepositoryInformation("atsushieno", "aap-juce-audible-planets",
                "org.androidaudioplugin.ports.juce.audibleplanets", "AAP Audible Planets"),
        ))
    }
}
