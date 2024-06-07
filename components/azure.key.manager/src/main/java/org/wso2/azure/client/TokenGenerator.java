/*
 * Copyright © 2022 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.azure.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.azure.client.model.AccessTokenResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TokenGenerator {
    private static final Log log = LogFactory.getLog(TokenGenerator.class);

    protected AccessTokenResponse getAccessToken(String clientId, String clientSecret, String tokenEndpoint)
            throws APIManagementException {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add(APIConstants.JSON_CLIENT_ID, clientId);
        formBuilder.add(APIConstants.JSON_CLIENT_SECRET, clientSecret);
        formBuilder.add(AzureADConstants.GRANT_TYPE, AzureADConstants.CLIENT_CREDENTIALS_GRANT_TYPE);
        formBuilder.add(AzureADConstants.SCOPE, String.format(AzureADConstants.API_SCOPE_TEMPLATE, clientId));

        RequestBody formBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(tokenEndpoint)
                .post(formBody)
                .build();
        Call call = client.newCall(request);

        try (Response response = call.execute()) {
            if (response.code() == 401) {
                log.warn("Invalid client secret provided, Please try again");
            }
            return new Gson().fromJson(response.body().string(), AccessTokenResponse.class);
        } catch (IOException e) {
            throw new APIManagementException("Error Getting access token for clientId " + clientId);
        }
    }

    protected AccessTokenInfo getAccessTokenInfo(String clientId, String clientSecret, String tokenEndpoint)
            throws APIManagementException {
        AccessTokenResponse tokenResp = this.getAccessToken(clientId, clientSecret, tokenEndpoint);
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        if (tokenResp != null) {
            tokenInfo.setConsumerKey(clientId);
            tokenInfo.setConsumerSecret(clientSecret);
            tokenInfo.setAccessToken(tokenResp.getAccessToken());
            tokenInfo.setScope(tokenInfo.getScopes());
            tokenInfo.setValidityPeriod(tokenResp.getExpiry());
            return tokenInfo;
        }

        tokenInfo.setTokenValid(false);
        tokenInfo.setErrorcode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
        return tokenInfo;
    }

}
