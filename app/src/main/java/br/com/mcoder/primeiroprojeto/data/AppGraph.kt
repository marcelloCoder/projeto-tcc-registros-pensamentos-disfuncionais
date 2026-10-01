package br.com.mcoder.primeiroprojeto.data

import android.content.Context
import br.com.mcoder.primeiroprojeto.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

object AppGraph {
    private var initialized = false

    lateinit var storage: AppStorage
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var thoughtRepository: ThoughtRepository
        private set

    fun initialize(context: Context) {
        if (initialized) {
            return
        }

        FirebaseApp.initializeApp(context.applicationContext)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            if (BuildConfig.DEBUG) DebugAppCheckProviderFactory.getInstance()
            else PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        storage = AppStorage(context.applicationContext)
        authRepository = AuthRepository(storage)
        thoughtRepository = ThoughtRepository(storage, authRepository)
        initialized = true
    }
}
