package dev.atsushieno.cipackageinstaller

import android.content.Context
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.connector.GitHubConnector
import java.util.Properties

class GitHubRepositoryStore(
    referrer: String
) : ApplicationStore(referrer) {

    override fun initialize(context: Context) {
        val creds = AppModel.getGitHubCredentials(context)
        if (creds.username.isNotEmpty() && creds.pat.isNotEmpty())
            updateCredentials(creds.username, creds.pat)
    }

    fun updateCredentials(username: String, pat: String) {
        github = GitHub.connect(username, pat)
    }

    var github: GitHub = GitHub.connectAnonymously()

    @Suppress("SpellCheckingInspection")
    override val repositories = listOf<RepositoryInformation>(
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-mda",
            "org.androidaudioplugin.ports.lv2.mda_lv2", "AAP MDA-LV2"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-sfizz",
            "org.androidaudioplugin.ports.lv2.sfizz", "AAP Sfizz"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-fluidsynth",
            "org.androidaudioplugin.ports.lv2.aap_fluidsynth", "AAP Fluidsynth"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-guitarix",
            "org.androidaudioplugin.ports.lv2.guitarix", "AAP Guitarix"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-dragonfly-reverb",
            "org.androidaudioplugin.ports.lv2.dragonfly_reverb", "AAP Dragonfly Reverb"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-string-machine",
            "org.androidaudioplugin.ports.lv2.string_machine", "AAP String Machine"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-obxd",
            "org.androidaudioplugin.ports.juce.obxd", "AAP OB-Xd"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-dexed",
            "org.androidaudioplugin.ports.juce.dexed", "AAP Dexed"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-adlplug",
                    "org.androidaudioplugin.ports.juce.adlplug", "AAP ADLplug"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-hera",
            "org.androidaudioplugin.ports.juce.hera", "AAP Hera"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-odin2",
            "org.androidaudioplugin.ports.juce.odin2", "AAP Odin2"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-vital",
            "org.androidaudioplugin.ports.juce.vital", "AAP Vitaloid"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-os251",
            "org.androidaudioplugin.ports.juce.os251", "AAP OS-251"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-simple-reverb",
            "org.androidaudioplugin.ports.juce.simple_reverb", "AAP SimpleReverb"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-chow-phaser",
                    "org.androidaudioplugin.ports.juce.chow_phaser", "AAP ChowPhaser"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-frequalizer",
            "org.androidaudioplugin.ports.juce.frequalizer", "AAP Frequalizer"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-byod",
            "org.androidaudioplugin.ports.juce.byod", "AAP BYOD"),
    )
}
