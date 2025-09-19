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

import feign.Feign;
import feign.Feign.Builder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.azure.client.model.ApiConfiguration;
import org.wso2.azure.client.model.ClientInformation;
import org.wso2.azure.client.model.ClientInformationList;
import org.wso2.azure.client.model.PasswordCredential;
import org.wso2.azure.client.model.PasswordInfo;
import org.wso2.azure.client.model.ServicePrincipalRequest;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.AbstractKeyManager;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import feign.Logger;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AzureADClient extends AbstractKeyManager {

    private static final Log log = LogFactory.getLog(AzureADClient.class);

    /***
     * Application related API calls
     */
    private ApplicationClient appClient;

    private String tokenEndpoint;
    private String requestedAccessTokenVersion;

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
        this.configuration = configuration;

        String appClientId = (String) this.configuration.getParameter(AzureADConstants.AD_APP_CLIENT_ID);
        String appClientSecret = (String) this.configuration.getParameter(AzureADConstants.AD_APP_CLIENT_SECRET);
        String revokeEndpoint = (String) this.configuration.getParameter(APIConstants.KeyManager.REVOKE_ENDPOINT);
        String graphApiEndpoint = (String) this.configuration.getParameter(AzureADConstants.GRAPH_API_ENDPOINT);
        requestedAccessTokenVersion =
                (String) this.configuration.getParameter(AzureADConstants.AZURE_AD_REQUESTED_ACCESS_TOKEN_VERSION);
        String graphApiDefaultScope = graphApiEndpoint + AzureADConstants.GRAPH_API_DEFAULT_SCOPE_SUFFIX;

        tokenEndpoint = (String) this.configuration.getParameter(APIConstants.KeyManager.TOKEN_ENDPOINT);

        AccessTokenGenerator accessTokenGenerator = new AccessTokenGenerator(tokenEndpoint, revokeEndpoint, appClientId,
                appClientSecret);

        AzureADRequestInterceptor appInterceptor = new AzureADRequestInterceptor(accessTokenGenerator, graphApiDefaultScope);

        appClient = this.buildFeignClient(new OkHttpClient(), appInterceptor)
                .target(ApplicationClient.class, graphApiEndpoint);
    }

    private Builder buildFeignClient(
            OkHttpClient client, AzureADRequestInterceptor interceptor) {
        return Feign.builder()
                .client(client)
                .requestInterceptor(interceptor)
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .errorDecoder(new KMClientErrorDecoder())
                .logger(new Slf4jLogger(ApplicationClient.class)).logLevel(Logger.Level.FULL);
    }

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthApplicationInfo oauthAppInfo = oauthAppRequest.getOAuthApplicationInfo();

        if (oauthAppInfo != null) {
            ClientInformation appInfo = this.getClientInformation(oauthAppInfo);
            ClientInformation app;

            if (log.isDebugEnabled()) {
                log.debug(String.format("Creating application : %s:", appInfo.toString()));
            }

            try {
                app = appClient.createApplication(appInfo);
                if (app != null) {
                    this.addNewPassword(app);

                    // update Application ID URI, Otherwise the token will be in v1.
                    this.updateApplicationIDURI(app.getId(), app.getClientId());

                    // Create servicePrincipal
                    this.createServicePrincipalForApplication(app.getClientId());

                    return getOAuthApplicationInfo(app);
                } else {
                    throw new APIManagementException("Client Application creation failed");
                }
            } catch (KeyManagerClientException e) {
                handleException("Error occurred while creating Azure AD Application", e);
            }
            return oauthAppInfo;
        }

        return null;
    }

    private void addNewPassword(ClientInformation app) throws KeyManagerClientException {
        PasswordInfo pInfo = this.setPassword(app.getId());
        app.setClientSecret(pInfo.getSecret());
    }

    private PasswordInfo setPassword(String id) throws KeyManagerClientException {
        PasswordCredential passwordCredential = new PasswordCredential();
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        passwordCredential.setDisplayName("app_secret_" + timeStamp);

        PasswordInfo passwordInfo = new PasswordInfo();
        passwordInfo.setPasswordCredential(passwordCredential);

        return appClient.addPassword(id, passwordInfo);
    }

    private void updateApplicationIDURI(String id, String appId) throws KeyManagerClientException {
        ClientInformation info = new ClientInformation();
        // Need to create Application ID URI. Used in default scope and
        String applicationIdUri = String.format(AzureADConstants.API_ID_URI_TEMPLATE, appId);
        info.setIdentifierUris(new String[] { applicationIdUri });
        appClient.updateApplication(id, info);
    }

    private void createServicePrincipalForApplication(String appId) throws KeyManagerClientException {
        ServicePrincipalRequest servicePrincipalRequest = new ServicePrincipalRequest();
        servicePrincipalRequest.setAppId(appId);
        appClient.createServicePrincipal(servicePrincipalRequest);
        if (log.isDebugEnabled()) {
            log.debug("Service Principal created for the application id : " + appId);
        }
    }

    private OAuthApplicationInfo getOAuthApplicationInfo(ClientInformation appInfo) {
        OAuthApplicationInfo oauthAppInfo = new OAuthApplicationInfo();
        oauthAppInfo.setClientName(appInfo.getAppName());
        oauthAppInfo.setClientId(appInfo.getClientId());

        if (appInfo.getClientSecret() != null) {
            oauthAppInfo.setClientSecret(appInfo.getClientSecret());
        }

        if (StringUtils.isNotEmpty(appInfo.getAppName())) {
            oauthAppInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_NAME, appInfo.getAppName());
        }

        oauthAppInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT,
                AzureADConstants.CLIENT_CREDENTIALS_GRANT_TYPE);

        // Add object UUID
        oauthAppInfo.addParameter(AzureADConstants.OBJECT_ID, appInfo.getId());

        return oauthAppInfo;
    }

    private ClientInformation getClientInformation(OAuthApplicationInfo oauthAppInfo) throws APIManagementException {
        ClientInformation clientInformation = new ClientInformation();
        clientInformation.setAppName(oauthAppInfo.getClientName());
        Object id = oauthAppInfo.getParameter(AzureADConstants.OBJECT_ID);
        if (id != null) {
            String idString = (String) id;
            clientInformation.setId(idString);
        }

        if (oauthAppInfo.getClientId() != null) {
            clientInformation.setClientId(oauthAppInfo.getClientId());
        }

        if (oauthAppInfo.getClientSecret() != null) {
            clientInformation.setClientSecret(oauthAppInfo.getClientSecret());
        }

        ApiConfiguration apiConfiguration = new ApiConfiguration();
        int requestedAccessTokenVersion = validateAndGetRequestedAccessTokenVersion(this.requestedAccessTokenVersion);
        apiConfiguration.setRequestedAccessTokenVersion(requestedAccessTokenVersion);
        clientInformation.setApi(apiConfiguration);

        return clientInformation;
    }

    private int validateAndGetRequestedAccessTokenVersion(String requestedAccessTokenVersion)
            throws APIManagementException {
        if (requestedAccessTokenVersion == null) {
            return AzureADConstants.AZURE_AD_DEFAULT_REQUESTED_ACCESS_TOKEN_VERSION;
        }
        Integer version = AzureADConstants.AZURE_AD_ALLOWED_ACCESS_TOKEN_VERSIONS.get(requestedAccessTokenVersion);
        if (version != null) {
            return version;
        }
        throw new APIManagementException("Invalid property azure_ad_requested_access_token_version: " +
                requestedAccessTokenVersion);
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthApplicationInfo oauthAppInfo = oauthAppRequest.getOAuthApplicationInfo();

        if (oauthAppInfo != null) {
            ClientInformation appInfo = this.getClientInformationByClientId(oauthAppInfo.getClientId());
            String id = appInfo.getId();

            if (log.isDebugEnabled()) {
                log.debug(String.format("Updating application : %s:", appInfo.toString()));
            }

            // Update the password every time application updates
            try {
                this.addNewPassword(appInfo);

                return this.getOAuthApplicationInfo(appInfo);
            } catch (KeyManagerClientException e) {
                handleException("Error occurred while updating Azure AD Application", e);
                return null;
            }
        }
        return null;
    }

    @Override
    public void deleteApplication(String clientId) throws APIManagementException {
        ClientInformation client = getClientInformationByClientId(clientId);
        if (client != null) {
            try {
                appClient.deleteApplication(client.getId());
            } catch (KeyManagerClientException e) {
                handleException("Error occurred while deleting Azure AD Application", e);
            }
        }
    }

    private ClientInformation getClientInformationByClientId(String clientId) throws APIManagementException {
        ClientInformation client = null;
        try {
            ClientInformationList list = appClient.searchByAppId(clientId);
            if (list != null && list.getValue().size() > 0) {
                client = list.getValue().get(0);
            }
        } catch (KeyManagerClientException e1) {
            handleException("Error occurred while searching Azure AD Application", e1);
        }
        return client;
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String clientId) throws APIManagementException {
        ClientInformation client = getClientInformationByClientId(clientId);
        if (client != null) {
            return this.getOAuthApplicationInfo(client);
        }
        return null;
    }

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws APIManagementException {
        TokenGenerator tokenGenerator = new TokenGenerator();
        String clientId = tokenRequest.getClientId();
        String clientSecret = tokenRequest.getClientSecret();

        return tokenGenerator.getAccessTokenInfo(clientId, clientSecret, tokenEndpoint);
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {
        return this.configuration;
    }

    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {
        String consumerKey = oAuthAppRequest.getOAuthApplicationInfo().getClientId();
        OAuthApplicationInfo clientInfo = null;
        if (StringUtils.isNotBlank(consumerKey)) {
            try {
                ClientInformation clientInformation = appClient.getApplicationByAppId(consumerKey);
                if (clientInformation != null) {
                    clientInfo = this.getOAuthApplicationInfo(clientInformation);
                } else {
                    throw new APIManagementException( "Something went wrong while getting OAuth application for given consumer key " + consumerKey + " " );
                }
            } catch (KeyManagerClientException e1 ) {
                handleException("Azure AD Application not found for the given consumer key " + consumerKey + " ", e1);
            }
            if (clientInfo == null) {
                String msg = "Something went wrong while getting OAuth application for given consumer key "
                        + consumerKey;
                throw new APIManagementException(msg);
            }

            return oAuthAppRequest.getOAuthApplicationInfo();
        }

        throw new APIManagementException("Consumer credentials are blank");
    }

    @Override
    public String getNewApplicationConsumerSecret(AccessTokenRequest tokenRequest) throws APIManagementException {
        return null;
    }

    /***
     * https://login.microsoftonline.com/common/.well-known/openid-configuration
     * as per above, no Introspection Endpoint supported.
     */
    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {
        return null;
    }

    @Override
    public boolean registerNewResource(API api, Map map) throws APIManagementException {
        return true;
    }

    @Override
    public Map getResourceByApiId(String apiId) throws APIManagementException {
        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws APIManagementException {
        return true;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException {
    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws APIManagementException {
    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String consumerKey) throws APIManagementException {
        return Collections.<String>emptySet();
    }

    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws APIManagementException {
        return null;
    }

    @Override
    public Map<String, Set<Scope>> getScopesForAPIS(String apiIdsString) throws APIManagementException {
        return null;
    }

    @Override
    public void registerScope(Scope scope) throws APIManagementException {

    }

    @Override
    public Scope getScopeByName(String name) throws APIManagementException {
        return null;
    }

    @Override
    public Map<String, Scope> getAllScopes() throws APIManagementException {
        return null;
    }

    @Override
    public void deleteScope(String scopeName) throws APIManagementException {

    }

    @Override
    public void updateScope(Scope scope) throws APIManagementException {

    }

    @Override
    public boolean isScopeExists(String scopeName) throws APIManagementException {
        return false;
    }

    @Override
    public String getType() {
        return AzureADConstants.AZURE_AD;
    }
}
