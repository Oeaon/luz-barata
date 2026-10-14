package com.luzbarata.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luzbarata.app.ads.AdsConsent
import com.luzbarata.app.notifications.TomorrowPricesNotifier
import com.luzbarata.app.ui.DaySelection
import com.luzbarata.app.ui.LuzBarataRoot
import com.luzbarata.app.ui.MainViewModel
import com.luzbarata.app.ui.theme.LuzBarataTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory((application as LuzBarataApp).container)
    }
    private var adsReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        AdsConsent.gather(this) { adsReady = true }

        setContent {
            LuzBarataTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                LuzBarataRoot(
                    state = state,
                    viewModel = viewModel,
                    adsReady = adsReady,
                    isPrivacyOptionsRequired = { AdsConsent.isPrivacyOptionsRequired(this) },
                    onPrivacyOptions = { AdsConsent.showPrivacyOptions(this) },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(TomorrowPricesNotifier.EXTRA_OPEN_TOMORROW, false) == true) {
            viewModel.select(DaySelection.TOMORROW)
        }
    }
}
