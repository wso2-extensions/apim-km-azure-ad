package org.wso2.azure.client;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.wso2.azure.client.model.ClientInformation;
import org.wso2.azure.client.model.ClientInformationList;
import org.wso2.azure.client.model.PasswordCredential;
import org.wso2.azure.client.model.PasswordInfo;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.AbstractKeyManager;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;

import feign.Feign;
import feign.Feign.Builder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureADClient extends AbstractKeyManager {
    /***
     * Application related API calls
     */
    private ApplicationClient appClient;

    private String tokenEndpoint;

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
        this.configuration = configuration;

        String appClientId = (String) this.configuration.getParameter(AzureADConstants.AD_APP_CLIENT_ID);
        String appClientSecret = (String) this.configuration.getParameter(AzureADConstants.AD_APP_CLIENT_SECRET);
        String revokeEndpoint = (String) this.configuration.getParameter(APIConstants.KeyManager.REVOKE_ENDPOINT);
        String graphApiEndpoint = (String) this.configuration.getParameter(AzureADConstants.GRAPH_API_ENDPOINT);

        tokenEndpoint = (String) this.configuration.getParameter(APIConstants.KeyManager.TOKEN_ENDPOINT);

        AccessTokenGenerator accessTokenGenerator = new AccessTokenGenerator(tokenEndpoint, revokeEndpoint, appClientId,
                appClientSecret);

        AzureADRequestInterceptor appInterceptor = new AzureADRequestInterceptor(accessTokenGenerator);

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
                .logger(new Slf4jLogger());
    }

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthApplicationInfo oauthAppInfo = oauthAppRequest.getOAuthApplicationInfo();

        if (oauthAppInfo != null) {
            ClientInformation appInfo = this.getClientInformation(oauthAppInfo);
            ClientInformation app;

            try {
                app = appClient.createApplication(appInfo);
                if (app != null) {
                    PasswordInfo pInfo = this.setPassword(app.getId());
                    app.setClientSecret(pInfo.getSecret());
                    return getOAuthApplicationInfo(app);
                }
            } catch (KeyManagerClientException e) {
                handleException("Error occurred while creating Azure AD Application", e);
            }
            return oauthAppInfo;
        }

        return null;
    }

    private PasswordInfo setPassword(String id) throws KeyManagerClientException {
        PasswordCredential passwordCredential = new PasswordCredential();
        passwordCredential.setDisplayName("app_secret");

        PasswordInfo passwordInfo = new PasswordInfo();
        passwordInfo.setPasswordCredential(passwordCredential);

        return appClient.addPassword(id, passwordInfo);
    }

    private OAuthApplicationInfo getOAuthApplicationInfo(ClientInformation appInfo) {
        OAuthApplicationInfo oauthAppInfo = new OAuthApplicationInfo();
        oauthAppInfo.setClientName(appInfo.getAppName());
        oauthAppInfo.setClientId(appInfo.getClientId());
        oauthAppInfo.setClientSecret(appInfo.getClientSecret());

        oauthAppInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT,
                AzureADConstants.CLIENT_CREDENTIALS_GRANT_TYPE);

        // Add object UUID
        oauthAppInfo.addParameter(AzureADConstants.OBJECT_ID, appInfo.getId());

        return oauthAppInfo;
    }

    private ClientInformation getClientInformation(OAuthApplicationInfo oauthAppInfo) {
        ClientInformation clientInformation = new ClientInformation();
        clientInformation.setAppName(oauthAppInfo.getClientName());
        String id = (String) oauthAppInfo.getParameter(AzureADConstants.OBJECT_ID);
        if (id != null)
            clientInformation.setId(id);

        if (oauthAppInfo.getClientId() != null)
            clientInformation.setClientId(oauthAppInfo.getClientId());

        if (oauthAppInfo.getClientSecret() != null)
            clientInformation.setClientSecret(oauthAppInfo.getClientSecret());

        return clientInformation;
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {
        OAuthApplicationInfo oauthAppInfo = oauthAppRequest.getOAuthApplicationInfo();
        if (oauthAppInfo != null) {
            ClientInformation appInfo = this.getClientInformation(oauthAppInfo);
            String id = appInfo.getId();

            try {
                // Update, Client does not send a body. 204 Status only
                appClient.updateApplication(id, appInfo);

                // // Request the updated application,
                ClientInformation clientInformation = appClient.getApplication(id);
                return this.getOAuthApplicationInfo(clientInformation);
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
                e.printStackTrace();
                handleException("Error occurred while deleting Azure AD Application", e);
            }
        }
    }

    private ClientInformation getClientInformationByClientId(String clientId) throws APIManagementException {
        ClientInformation client = null;
        try {
            ClientInformationList list = appClient.searchByAppId(clientId);
            if (list != null && list.getValue().size() > 0)
                client = list.getValue().get(0);
        } catch (KeyManagerClientException e1) {
            e1.printStackTrace();
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

    protected void clearTest() throws KeyManagerClientException {
        ClientInformationList allTestApplications = this.appClient.getAllTestApplications();
        allTestApplications.getValue().forEach(v -> {
            try {
                this.appClient.deleteApplication(v.getId());
            } catch (KeyManagerClientException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {
        return this.configuration;
    }

    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {
        String consumerKey = oAuthAppRequest.getOAuthApplicationInfo().getClientId();
        String consumerSecret = oAuthAppRequest.getOAuthApplicationInfo().getClientSecret();

        if (StringUtils.isNotBlank(consumerKey)) {
            OAuthApplicationInfo clientInfo = retrieveApplication(consumerKey);
            if (clientInfo == null) {
                String msg = "Something went wrong while getting OAuth application for given consumer key "
                        + consumerKey;
                log.error(msg);
                throw new APIManagementException(msg);
            }

            if (StringUtils.isNotBlank(consumerSecret) && !consumerSecret.equals(clientInfo.getClientSecret())) {
                throw new APIManagementException("The secret key is wrong for the given consumer key " + consumerKey);
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
