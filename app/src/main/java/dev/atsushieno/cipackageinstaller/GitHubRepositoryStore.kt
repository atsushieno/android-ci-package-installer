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
            "org.androidaudioplugin.samples.aap_mda_lv2", "AAP MDA-LV2"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-sfizz",
            "org.androidaudioplugin.samples.aap_mda_sfizz", "AAP Sfizz"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-fluidsynth",
            "org.androidaudioplugin.samples.aap_mda_fluidsynth", "AAP Fluidsynth"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-guitarix",
            "org.androidaudioplugin.samples.aap_mda_guitarix", "AAP Guitarix"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-dragonfly-reverb",
            "org.androidaudioplugin.samples.aap_dragonfly_reverb", "AAP Dragonfly Reverb"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-lv2-string-machine",
            "org.androidaudioplugin.samples.aap_string_machine", "AAP String Machine"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-obxd",
            "org.androidaudioplugin.samples.aap_juce_obxd", "AAP OB-Xd"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-dexed",
            "org.androidaudioplugin.samples.aap_juce_dexed", "AAP Dexed"),
        //GitHubRepositoryInformation(this, "atsushieno", "aap-juce-adlplug",
        //            "org.androidaudioplugin.samples.aap_juce_adlplug", "AAP ADLplug"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-hera",
            "org.androidaudioplugin.samples.aap_juce_hera", "AAP Hera"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-odin2",
            "org.androidaudioplugin.samples.aap_juce_odin2", "AAP Odin2"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-vital",
            "org.androidaudioplugin.samples.aap_juce_vital", "AAP Vitaloid"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-os251",
            "org.androidaudioplugin.samples.aap_juce_os251", "AAP OS-251"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-simple-reverb",
            "org.androidaudioplugin.samples.aap_juce_simple_reverb", "AAP SimpleReverb"),
        //GitHubRepositoryInformation(this, "atsushieno", "aap-juce-chow-phaser",
        //            "org.androidaudioplugin.samples.aap_juce_chow_phaser", "AAP ChowPhaser"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-frequalizer",
            "org.androidaudioplugin.samples.aap_juce_frequalizer", "AAP Frequalizer"),
        GitHubRepositoryInformation(this, "atsushieno", "aap-juce-byod",
            "org.androidaudioplugin.samples.aap_juce_byod", "AAP BYOD"),
    )
}
