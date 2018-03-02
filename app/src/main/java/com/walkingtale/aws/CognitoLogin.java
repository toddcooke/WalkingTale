package com.walkingtale.aws;

import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;

public class CognitoLogin {
    private final String TAG = this.getClass().getSimpleName();
    private String accessToken;

    public String getToken(Context context) {

        CognitoUserPool userPool = new CognitoUserPool(context,
                ConstantsKt.getCognitoUserPoolId(),
                ConstantsKt.getCognitoClientId(),
                ConstantsKt.getCognitoClientSecret());
        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                accessToken = userSession.getIdToken().getJWTToken();
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, "Passw0rd!", null);
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
            }

            @Override
            public void onFailure(Exception exception) {
            }
        };

        userPool.getUser("todd").getSession(authenticationHandler);
        Log.i(TAG, accessToken);
        return accessToken;
    }
}