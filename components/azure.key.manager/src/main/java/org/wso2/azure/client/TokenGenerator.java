package org.wso2.azure.client;

import java.io.IOException;

import org.wso2.azure.client.model.AccessTokenResponse;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class TokenGenerator {
    protected AccessTokenResponse getAccessToken(String clientId, String clientSecret, String tokenEndpoint) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(APIConstants.JSON_CLIENT_ID, clientId);
        formBuilder.add(APIConstants.JSON_CLIENT_SECRET, clientSecret);
        formBuilder.add(AzureADConstants.GRANT_TYPE, AzureADConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        formBuilder.add(AzureADConstants.SCOPE, AzureADConstants.MICROSOFT_DEFAULT_SCOPE);

        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .post(formBody)
                .build();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            return new Gson().fromJson(response.body().string(), AccessTokenResponse.class);
        } catch (IOException e) {
            // e.printStackTrace();
            log.error(e.getMessage());
        }

        return null;
    }

    protected AccessTokenInfo getAccessTokenInfo(String clientId, String clientSecret, String tokenEndpoint) {
        AccessTokenResponse tokenResp = this.getAccessToken(clientId, clientSecret, tokenEndpoint);
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        if (tokenResp != null) {
            tokenInfo.setConsumerKey(clientId);
            tokenInfo.setConsumerSecret(clientSecret);
            tokenInfo.setAccessToken(tokenResp.getAccessToken());
            tokenInfo.setScope(new String[] { AzureADConstants.MICROSOFT_DEFAULT_SCOPE });
            tokenInfo.setValidityPeriod(tokenResp.getExpiry());
            return tokenInfo;
        }

        tokenInfo.setTokenValid(false);
        tokenInfo.setErrorcode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
        return tokenInfo;
    }

}
