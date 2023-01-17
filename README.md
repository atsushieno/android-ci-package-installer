# AAP CI package installer

It is an Android application that lets you install the latest versions of those APKs built and uploaded as CI artifacts.

Currently this application only targets AAP (Audio Plugins For Android) projects, but the project is 50% intended to be used more widely than just for AAP.

## Limitations

GitHub requires sign-in to download artifacts, so a GitHub account and a Personal Access Token is required to download those APKs.

Currently only GitHub Actions is supported, but it could be extended to any CI builds.

Currently only such a build artifact that contains an APK is supported.

Currently the target apps are listed in the hard-coded catalogs, but they can be extended to anything e.g. download catalogs, query at GitHub, everything for the logged user.

Since we cannot get a lot of information about the target APK without downloading it, it is unlikely to happen that the catalog could be fully automatically filled.

## Licenses

AAP-CI-Package-Installer is licensed under the MIT license.

AAP-CI-Package-Installer heavily depends on [GitHub API for Java](https://github-api.kohsuke.org/) by Kohsuke Kawaguchi and other contributors. It is distributed under the MIT license.
