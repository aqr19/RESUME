package com.liskovsoft.googleapi.oauth2.manager;

import com.liskovsoft.googlecommon.common.helpers.RetrofitOkHttpHelper;
import com.liskovsoft.googlecommon.common.models.auth.AccessToken;
import com.liskovsoft.googlecommon.service.oauth.YouTubeAccount;
import com.liskovsoft.mediaserviceinterfaces.oauth.Account;
import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.sharedutils.mylogger.Log;

import java.util.Map;

public abstract class OAuth2AccountManagerBase {
    private static final String TAG = OAuth2AccountManagerBase.class.getSimpleName();
    private static final long TOKEN_REFRESH_PERIOD_MS = 60 * 60 * 1_000; // NOTE: auth token max lifetime is 60 min
    private String mCachedAuthorizationHeader;
    private long mCacheUpdateTime;

    protected abstract Account getSelectedAccount();
    protected abstract AccessToken obtainAccessToken(String refreshToken);

    public void checkAuth() {
        updateAuthHeadersIfNeeded();
    }

    protected void updateAuthHeadersIfNeeded() {
        Account account = getSelectedAccount();
        String refreshToken = account != null ? ((YouTubeAccount) account).getRefreshToken() : null;

        if (refreshToken == null) {
            if (mCachedAuthorizationHeader == null && RetrofitOkHttpHelper.getAuthHeaders().isEmpty()
                    && System.currentTimeMillis() - mCacheUpdateTime < TOKEN_REFRESH_PERIOD_MS) {
                return;
            }
            mCachedAuthorizationHeader = null;
            syncWithRetrofit();
            mCacheUpdateTime = System.currentTimeMillis();
            return;
        }

        if (mCachedAuthorizationHeader != null && Helpers.equals(mCachedAuthorizationHeader, RetrofitOkHttpHelper.getAuthHeaders().get("Authorization"))
                && System.currentTimeMillis() - mCacheUpdateTime < TOKEN_REFRESH_PERIOD_MS) {
            return;
        }

        updateAuthHeaders();

        mCacheUpdateTime = System.currentTimeMillis();
    }

    private void updateAuthHeaders() {
        Account account = getSelectedAccount();
        String refreshToken = account != null ? ((YouTubeAccount) account).getRefreshToken() : null;
        // get or create authorization on fly
        if (refreshToken != null) {
            mCachedAuthorizationHeader = createAuthorizationHeader(refreshToken);
        } else {
            mCachedAuthorizationHeader = null;
        }
        syncWithRetrofit();
    }

    /**
     * For testing purposes
     */
    public void setAuthorizationHeader(String authorizationHeader) {
        mCachedAuthorizationHeader = authorizationHeader;
        mCacheUpdateTime = System.currentTimeMillis();

        syncWithRetrofit();
    }

    public void invalidateCache() {
        mCachedAuthorizationHeader = null;
    }

    /**
     * Authorization should be updated periodically (see expire_in field in response)
     */
    private synchronized String createAuthorizationHeader(String refreshToken) {
        if (refreshToken == null) {
            return null;
        }

        Log.d(TAG, "Updating authorization header...");

        String authorizationHeader = null;

        AccessToken token = obtainAccessToken(refreshToken);

        if (token != null) {
            authorizationHeader = String.format("%s %s", token.getTokenType(), token.getAccessToken());
        } else {
            Log.d(TAG, "Access token is null or not yet authorized.");
        }

        return authorizationHeader;
    }

    private void syncWithRetrofit() {
        if (Helpers.isJUnitTest()) {
            return;
        }

        Map<String, String> headers = RetrofitOkHttpHelper.getAuthHeaders();
        headers.clear();

        if (mCachedAuthorizationHeader != null && getSelectedAccount() != null) {
            headers.put("Authorization", mCachedAuthorizationHeader);
            String pageIdToken = ((YouTubeAccount) getSelectedAccount()).getPageIdToken();
            if (pageIdToken != null) {
                // Apply branded account rights (restricted videos). Branded refresh token with current account page id.
                headers.put("X-Goog-Pageid", pageIdToken);
            }
        }
    }
}
