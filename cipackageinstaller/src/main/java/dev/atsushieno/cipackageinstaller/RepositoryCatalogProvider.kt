package dev.atsushieno.cipackageinstaller

import android.content.Context

abstract class RepositoryCatalogProvider {
    companion object {
        val empty = EmptyRepositoryCatalogProvider()
    }

    class EmptyRepositoryCatalogProvider : RepositoryCatalogProvider() {
        override val repositories: List<RepositoryInformation> = listOf()
        override fun initialize(context: Context) {}
    }

    class MergedRepositoryCatalogProvicer : RepositoryCatalogProvider() {
        // Note that after call to initialize() on this class itself, any added store must be initialized before being added to this list.
        val providers = mutableListOf<RepositoryCatalogProvider>()

        override val repositories: List<RepositoryInformation>
            get() = providers.flatMap { repositories }
        override fun initialize(context: Context) {
            // in principle this method should not initialize anything, as each item in `providers` should have already been initialized.
        }
    }

    abstract val repositories: List<RepositoryInformation>
    abstract fun initialize(context: Context)
}