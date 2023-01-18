# AAP CI package installer

It is an Android application that lets you install the latest versions of those APKs built and uploaded as CI artifacts.

![AAP-CI-Package-Installer sshot](./docs/images/aap-ci-package-installer.png)

Currently this application only targets AAP (Audio Plugins For Android) projects, but the project is 50% intended to be used more widely than just for AAP.

## Rationale

We need something to facilitate testing existing audio plugins to provide easier access to experience the AAP ecosystem. Or, we sometimes need to demonstrate or "show off" what the current state of union is like, often on a dedicated "demo devices". Since we already have 20~ish audio plugin APKs and it's going to grow, it makes more sense to provide easier way to install those plugins, rather than going to each project to download an APK and install manually for 20+ repos.

Not [Firebase Testing](https://firebase.google.com/docs/app-distribution/android/distribute-gradle)? It is possible to distribute testing APKs and probably tooling works, but looks like we have to create one app per apk / repo, which is quite annoying to manage. We may use it for individual development, but not for this particular need.

[DeployGate](https://deploygate.com/?locale=en) offers simple and straightforward test app distribution that I love as a user (also it is run by friends), but the free tier offers 2 apps at most where we have 20+ (not sure if aggregated management is doable there either). So not for the individual plugins apps. But we may use it for testing *this* installer app.

## Limitations

I wanted to "batch install" multiple APKs, but as a general Android app, it can only "request" user to install i.e. `android.permission.REQUEST_INSTALL_PACKAGES`, not `android.permission.INSTALL_PACKAGES` like Google Play Store app or any other vendor-specific application store app can perform. That would be achieved by batch `adb install` command runs at host elsewhere.

Since it targets "artifacts" it is more like a development aid, not for mere "users" yet. When we publish APKs to "releases" (we don't currently, as it is not automated). To make it happen, automatic APK uploading to the release would be required first.

GitHub requires sign-in to download artifacts, so a GitHub account and a Personal Access Token is required to download those APKs.

Currently only GitHub Actions is supported, but it could be extended to any CI builds.

Currently only such a build artifact that contains an APK is supported.

Currently the target apps are listed in the hard-coded catalogs, but they can be extended to anything e.g. download catalogs, query at GitHub, everything for the logged user. The catalog information does not seem to be strictly examined at install time, but "Uninstall" operation on the UI depends on the precise package name.

Due to the AAP plugin ecosystem compatibility, the packages may vary depending on the AAP protocol version. It may be a good idea for us (or people like us) to provide multiple sets of catalogs. It may be just easier to provide multiple apps based on this codebase though.

Since we cannot get a lot of information about the target APK without downloading it, it is unlikely to happen that the catalog could be fully automatically filled. It is more likely that we expect some metadata text file in the source tree so that we can retrieve before downloading the artifact.

API wise, I don't use PackageInstaller and stick to deprecated `Intent.ACTION_INSTALL_PACKAGE` approach. It is simply because I could not find any single working example of the API and thus [I could not get it working](https://github.com/atsushieno/aap-ci-package-installer/blob/a41ea213728bd8434da08b04497988cfa4757145/app/src/main/java/dev/atsushieno/cipackageinstaller/AppModel.kt#L73). Google's own [ApiDemos](https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/) does not work either (you can find my extracted port to Android Studio project [here](https://drive.google.com/file/d/1IRGJSTbR2fJzveJjbP6Y9sNynh3tWXIj/view?usp=share_link).

## Licenses

AAP-CI-Package-Installer is licensed under the MIT license.

AAP-CI-Package-Installer heavily depends on [GitHub API for Java](https://github-api.kohsuke.org/) by Kohsuke Kawaguchi and other contributors. It is distributed under the MIT license.
