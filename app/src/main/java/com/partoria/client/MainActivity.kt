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

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var partsViewModel: PartsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenDataStore = TokenDataStore(this)
        val apiService = ApiServiceImpl(HttpClientProvider.createClient())

        val authRepository = AuthRepositoryImpl(apiService, tokenDataStore)
        val partRepository = PartRepositoryImpl(apiService) { authRepository.getToken() }

        val loginUseCase = LoginUseCase(authRepository)
        val registerUseCase = RegisterUseCase(authRepository)
        val saveAuthDataUseCase = SaveAuthDataUseCase(authRepository)
        val clearAuthDataUseCase = ClearAuthDataUseCase(authRepository)
        val isLoggedInUseCase = IsLoggedInUseCase(authRepository)
        val getUserRoleUseCase = GetUserRoleUseCase(authRepository)
        val getUsernameUseCase = GetUsernameUseCase(authRepository)

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
        val getPartByIdUseCase = GetPartByIdUseCase(partRepository)
        val getPartWithDetailsUseCase = GetPartWithDetailsUseCase(partRepository)
        val getFilteredPartsUseCase = GetFilteredPartsUseCase(partRepository)
        val getFiltersMetaUseCase = GetFiltersMetaUseCase(partRepository)
        val addToFavoritesUseCase = AddToFavoritesUseCase(partRepository)
        val removeFromFavoritesUseCase = RemoveFromFavoritesUseCase(partRepository)
        val getFavoritesUseCase = GetFavoritesUseCase(partRepository)
        val searchPartsUseCase = SearchPartsUseCase(partRepository)

        partsViewModel = PartsViewModel(
            getAllPartsUseCase,
            getPartByIdUseCase,
            getPartWithDetailsUseCase,
            getFilteredPartsUseCase,
            getFiltersMetaUseCase,
            addToFavoritesUseCase,
            removeFromFavoritesUseCase,
            getFavoritesUseCase,
            searchPartsUseCase
        )

        setContent {
            PartoriaClientTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        partsViewModel = partsViewModel
                    )
                }
            }
        }
    }
}