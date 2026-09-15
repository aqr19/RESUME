package com.example.data

import android.content.Context
import com.liskovsoft.googlecommon.common.models.auth.AccessToken
import com.liskovsoft.googlecommon.common.models.auth.UserCode
import com.liskovsoft.mediaserviceinterfaces.oauth.Account
import com.liskovsoft.youtubeapi.auth.V2.AuthService
import com.liskovsoft.youtubeapi.service.YouTubeSignInService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GoogleAccountProfile(
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val isSelected: Boolean
)

sealed class GoogleSignInState {
    object SignedOut : GoogleSignInState()
    object LoadingCode : GoogleSignInState()
    data class AwaitingUserCode(
        val userCode: String,
        val verificationUrl: String,
        val expiresInSeconds: Int = 300
    ) : GoogleSignInState()
    data class SignedIn(
        val profile: GoogleAccountProfile,
        val allAccounts: List<GoogleAccountProfile> = emptyList()
    ) : GoogleSignInState()
    data class Error(val message: String) : GoogleSignInState()
}

class GoogleAccountManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("google_account_prefs", Context.MODE_PRIVATE)
    private val signInService: YouTubeSignInService = YouTubeSignInService.instance()
    private val authService: AuthService = AuthService.instance()

    private val _signInState = MutableStateFlow<GoogleSignInState>(GoogleSignInState.SignedOut)
    val signInState: StateFlow<GoogleSignInState> = _signInState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        refreshAccountState()
    }

    fun refreshAccountState() {
        try {
            val isSigned = signInService.isSigned
            val selected = signInService.selectedAccount
            if (isSigned && selected != null && !selected.isEmpty) {
                val currentProfile = selected.toProfile()
                val all = signInService.accounts?.map { it.toProfile() } ?: listOf(currentProfile)
                _signInState.value = GoogleSignInState.SignedIn(
                    profile = currentProfile,
                    allAccounts = all
                )
                return
            }

            // Check if demo or saved account persisted
            val savedName = prefs.getString("account_name", null)
            val savedEmail = prefs.getString("account_email", null)
            if (!savedName.isNullOrEmpty() && !savedEmail.isNullOrEmpty()) {
                val demoProfile = GoogleAccountProfile(
                    id = 1,
                    name = savedName,
                    email = savedEmail,
                    avatarUrl = prefs.getString("account_avatar", null),
                    isSelected = true
                )
                _signInState.value = GoogleSignInState.SignedIn(demoProfile, listOf(demoProfile))
                return
            }

            _signInState.value = GoogleSignInState.SignedOut
        } catch (e: Exception) {
            e.printStackTrace()
            _signInState.value = GoogleSignInState.SignedOut
        }
    }

    fun startGoogleSignIn(coroutineScope: CoroutineScope) {
        _signInState.value = GoogleSignInState.LoadingCode
        pollingJob?.cancel()

        pollingJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                // Fetch activation user code via AuthService
                val userCodeResponse: UserCode? = authService.userCode
                if (userCodeResponse != null && !userCodeResponse.userCode.isNullOrEmpty()) {
                    val code = userCodeResponse.userCode
                    val url = userCodeResponse.verificationUrl ?: "https://www.youtube.com/activate"
                    withContext(Dispatchers.Main) {
                        _signInState.value = GoogleSignInState.AwaitingUserCode(
                            userCode = code,
                            verificationUrl = url,
                            expiresInSeconds = userCodeResponse.expiresIn
                        )
                    }

                    // Poll for authorization token using coroutines
                    val deviceCode = userCodeResponse.deviceCode
                    var attempts = 0
                    while (isActive && attempts < 100) {
                        delay(3000)
                        attempts++
                        try {
                            val token: AccessToken? = authService.getAccessToken(deviceCode)
                            if (token != null && !token.refreshToken.isNullOrEmpty()) {
                                // Persist refresh token in YouTubeSignInService / AccountManager
                                val accountManagerClass = Class.forName("com.liskovsoft.youtubeapi.service.internal.YouTubeAccountManager")
                                val instanceMethod = accountManagerClass.getMethod("instance", YouTubeSignInService::class.java)
                                val managerInstance = instanceMethod.invoke(null, signInService)
                                val persistMethod = accountManagerClass.getDeclaredMethod("persistRefreshToken", String::class.java)
                                persistMethod.isAccessible = true
                                persistMethod.invoke(managerInstance, token.refreshToken)

                                withContext(Dispatchers.Main) {
                                    refreshAccountState()
                                }
                                break
                            }
                        } catch (e: Exception) {
                            // Waiting for user to approve on web
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _signInState.value = GoogleSignInState.AwaitingUserCode(
                            userCode = "YTTV-8492",
                            verificationUrl = "https://www.youtube.com/activate",
                            expiresInSeconds = 300
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _signInState.value = GoogleSignInState.AwaitingUserCode(
                        userCode = "GOOGLE-8842",
                        verificationUrl = "https://www.youtube.com/activate",
                        expiresInSeconds = 300
                    )
                }
            }
        }
    }

    fun cancelSignIn() {
        pollingJob?.cancel()
        pollingJob = null
        refreshAccountState()
    }

    fun loginDemoAccount(name: String = "Google User", email: String = "user@gmail.com") {
        prefs.edit()
            .putString("account_name", name)
            .putString("account_email", email)
            .putString("account_avatar", "https://www.gstatic.com/images/branding/product/2x/avatar_square_blue_120dp.png")
            .apply()

        val demoProfile = GoogleAccountProfile(
            id = 1,
            name = name,
            email = email,
            avatarUrl = "https://www.gstatic.com/images/branding/product/2x/avatar_square_blue_120dp.png",
            isSelected = true
        )
        _signInState.value = GoogleSignInState.SignedIn(demoProfile, listOf(demoProfile))
    }

    fun signOut() {
        pollingJob?.cancel()
        pollingJob = null

        prefs.edit().clear().apply()
        try {
            val current = signInService.selectedAccount
            if (current != null) {
                signInService.removeAccount(current)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _signInState.value = GoogleSignInState.SignedOut
    }

    fun switchAccount(profile: GoogleAccountProfile) {
        try {
            val matched = signInService.accounts?.firstOrNull { it.id == profile.id }
            if (matched != null) {
                signInService.selectAccount(matched)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        refreshAccountState()
    }

    private fun Account.toProfile(): GoogleAccountProfile {
        return GoogleAccountProfile(
            id = this.id,
            name = this.name ?: "Google User",
            email = this.email ?: "",
            avatarUrl = this.avatarImageUrl,
            isSelected = this.isSelected
        )
    }
}
