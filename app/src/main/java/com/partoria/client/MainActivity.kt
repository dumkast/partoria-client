package com.partoria.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.partoria.client.data.api.ApiServiceImpl
import com.partoria.client.data.api.HttpClientProvider
import com.partoria.client.data.datastore.TokenDataStore
import com.partoria.client.data.repository.AuthRepositoryImpl
import com.partoria.client.data.repository.PartRepositoryImpl
import com.partoria.client.domain.usecase.auth.*
import com.partoria.client.domain.usecase.part.*
import com.partoria.client.presentation.navigation.NavGraph
import com.partoria.client.presentation.viewmodels.AuthViewModel
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.ui.theme.PartoriaClientTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.partoria.client.data.datastore.SettingsDataStore
import com.partoria.client.presentation.screens.AppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var partsViewModel: PartsViewModel
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenDataStore = TokenDataStore(this)
        settingsDataStore = SettingsDataStore(this)
        val apiService = ApiServiceImpl(HttpClientProvider.createClient())

        val authRepository = AuthRepositoryImpl(apiService, tokenDataStore)

        val loginUseCase = LoginUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository)
        val getTokenUseCase = GetTokenUseCase(authRepository)
        val saveAuthDataUseCase = SaveAuthDataUseCase(authRepository)
        val clearAuthDataUseCase = ClearAuthDataUseCase(authRepository)
        val isLoggedInUseCase = IsLoggedInUseCase(authRepository)
        val getUserRoleUseCase = GetUserRoleUseCase(authRepository)
        val getUsernameUseCase = GetUsernameUseCase(authRepository)

        val partRepository = PartRepositoryImpl(apiService) { getTokenUseCase() }

        authViewModel = AuthViewModel(
            loginUseCase,
            registerUseCase,
            saveAuthDataUseCase,
            clearAuthDataUseCase,
            isLoggedInUseCase,
            getUserRoleUseCase,
            getUsernameUseCase
        )

        val getAllPartsUseCase = GetAllPartsUseCase(partRepository)
        val getPartWithDetailsUseCase = GetPartWithDetailsUseCase(partRepository)
        val getFilteredPartsUseCase = GetFilteredPartsUseCase(partRepository)
        val getFiltersMetaUseCase = GetFiltersMetaUseCase(partRepository)
        val addToFavoritesUseCase = AddToFavoritesUseCase(partRepository)
        val removeFromFavoritesUseCase = RemoveFromFavoritesUseCase(partRepository)
        val getFavoritesUseCase = GetFavoritesUseCase(partRepository)
        val deletePartUseCase = DeletePartUseCase(partRepository)
        val createPartUseCase = CreatePartUseCase(partRepository)
        val updatePartUseCase = UpdatePartUseCase(partRepository)

        partsViewModel = PartsViewModel(
            getAllPartsUseCase,
            getPartWithDetailsUseCase,
            getFilteredPartsUseCase,
            getFiltersMetaUseCase,
            addToFavoritesUseCase,
            removeFromFavoritesUseCase,
            getFavoritesUseCase,
            deletePartUseCase,
            createPartUseCase,
            updatePartUseCase
        )

        setContent {
            val currentTheme by settingsDataStore.appThemeFlow.collectAsState(initial = AppTheme.SYSTEM)
            val savedColorIndex by settingsDataStore.avatarColorIndexFlow.collectAsState(initial = -1)
            val useDarkTheme = when (currentTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            PartoriaClientTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        partsViewModel = partsViewModel,
                        currentTheme = currentTheme,
                        onThemeChange = { selectedTheme ->
                            lifecycleScope.launch {
                                settingsDataStore.saveAppTheme(selectedTheme)
                            }
                        },
                        savedColorIndex = savedColorIndex,
                        onColorIndexChange = { newIndex ->
                            lifecycleScope.launch {
                                settingsDataStore.saveAvatarColorIndex(newIndex)
                            }
                        }
                    )
                }
            }
        }
    }
}