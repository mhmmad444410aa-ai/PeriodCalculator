package com.mhmmad.periodcalculator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhmmad.periodcalculator.data.PeriodViewModel
import com.mhmmad.periodcalculator.ui.PeriodTheme
import com.mhmmad.periodcalculator.ui.PeriodRoot
import com.mhmmad.periodcalculator.ads.AdConsentManager

class MainActivity : AppCompatActivity() {
    private var locked by mutableStateOf(true)
    private var authenticated = false
    private lateinit var vm: PeriodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Huawei SDK checks country/region consent requirements at each app launch.
        // No general advertising on/off dialog is shown.
        AdConsentManager(this).refresh()
        // Prevent sensitive journal snapshots in recents and screen captures.
        if (!BuildConfig.DEBUG) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        vm = androidx.lifecycle.ViewModelProvider(this)[PeriodViewModel::class.java]
        setContent {
            val prefs by vm.prefs.collectAsStateWithLifecycle()
            val ready by vm.ready.collectAsStateWithLifecycle()
            LaunchedEffect(ready, prefs.lock) { if (ready) locked = prefs.lock && !authenticated }
            PeriodTheme(prefs.theme) {
                if (!ready) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                else if (locked && prefs.lock) LockScreen { tryUnlock() }
                else PeriodRoot(vm, onUnlock = { tryUnlock() }, canLock = { supportsDeviceAuth() })
            }
        }
    }
    override fun onStop() {
        if (::vm.isInitialized && vm.prefs.value.lock) {
            authenticated = false
            locked = true
        }
        super.onStop()
    }
    private fun supportedAuthenticators(): Int = if (android.os.Build.VERSION.SDK_INT >= 30)
        BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    else BiometricManager.Authenticators.BIOMETRIC_WEAK
    fun supportsDeviceAuth(): Boolean = BiometricManager.from(this)
        .canAuthenticate(supportedAuthenticators()) == BiometricManager.BIOMETRIC_SUCCESS
    private fun tryUnlock() {
        if (!supportsDeviceAuth()) return
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                authenticated = true
                locked = false
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.unlock))
            .setSubtitle(getString(R.string.lock_reason))
            .setAllowedAuthenticators(supportedAuthenticators())
            .apply { if (android.os.Build.VERSION.SDK_INT < 30) setNegativeButtonText(getString(android.R.string.cancel)) }
            .build()
        prompt.authenticate(info)
    }
}

@Composable
private fun LockScreen(unlock: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Lock, null, Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.lock_reason), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Button(onClick = unlock) { Text(stringResource(R.string.unlock)) }
        }
    }
}
