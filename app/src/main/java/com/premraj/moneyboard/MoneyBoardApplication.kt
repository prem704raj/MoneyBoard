package com.premraj.moneyboard

import android.app.Application
import com.premraj.moneyboard.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.YearMonth

class MoneyBoardApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    private val applicationScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)

        applicationScope.launch {
            appContainer.databaseInitializer.initialize()

            if (BuildConfig.DEBUG) {
                appContainer.referenceDemoPlanSeeder.seedIfEmpty(
                    YearMonth.now()
                )
            }
        }
    }
}
