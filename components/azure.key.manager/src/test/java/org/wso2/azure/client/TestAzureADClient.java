package org.wso2.azure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;

@ExtendWith(MockitoExtension.class)
public class TestAzureADClient {

    AzureADClient azureADClient;

    @BeforeEach
    public void setUp() throws APIManagementException {
        this.azureADClient = new AzureADClient();

        // Create configurations
        KeyManagerConfiguration configuration = new KeyManagerConfiguration();
        configuration.addParameter(AzureADConstants.AD_APP_CLIENT_ID,
                AzureAdValues.clientId);
        configuration.addParameter(AzureADConstants.AD_APP_CLIENT_SECRET,
                AzureAdValues.clientSecret);

        configuration.addParameter(AzureADConstants.GRAPH_API_ENDPOINT,
                AzureADConstants.GRAPH_API_ENDPOINT_VALUE);
        configuration.addParameter(APIConstants.KeyManager.TOKEN_ENDPOINT,
                AzureAdValues.tokenEndpoint);

        azureADClient.loadConfiguration(configuration);
    }

    @AfterEach
    public void tearDown() throws KeyManagerClientException {
        this.azureADClient.clearTest();
    }

    // @Test
    public void itShouldReturnNull_ForNullOAuthApplicationInfo() throws APIManagementException {
        OAuthAppRequest oauthAppRequest = new OAuthAppRequest();
        OAuthApplicationInfo createApplication = azureADClient.createApplication(oauthAppRequest);
        assertNull(createApplication);
    }

    // @Test
    public void itShouldReturnCreatedApplicationInfo() throws APIManagementException {
        String randomName = this.getRandomName();
        OAuthApplicationInfo createApplication = this.createApplication(randomName);
        assertEquals(randomName, createApplication.getClientName());
    }

    // @Test
    public void itShouldDeleteApplicationApplicationInfo() throws APIManagementException {
        String clientId = this.createAndGetClientId();
        this.azureADClient.deleteApplication(clientId);
    }

    // @Test
    public void itShouldGetClientInformation() throws APIManagementException,
            InterruptedException {
        String clientId = this.createAndGetClientId();

        // this is needed since update take a bit of time to reflect.
        Thread.sleep(30000);

        OAuthApplicationInfo retrieveApplication = this.azureADClient
                .retrieveApplication(clientId);
        assertNotNull(retrieveApplication);
    }

    // @Test
    public void itShouldUpdateCreatedApplication() throws APIManagementException,
            InterruptedException {
        String id = this.createAndGetId();

        // this is needed since update take a bit of time to reflect.
        // Thread.sleep(30000);

        OAuthApplicationInfo updatingApp = new OAuthApplicationInfo();
        updatingApp.addParameter(AzureADConstants.OBJECT_ID, id);

        String updatingName = this.getRandomName();
        updatingApp.setClientName(updatingName);

        OAuthAppRequest oAuthAppRequest = mock(OAuthAppRequest.class);
        when(oAuthAppRequest.getOAuthApplicationInfo()).thenReturn(updatingApp);

        OAuthApplicationInfo updateApplication = azureADClient.updateApplication(oAuthAppRequest);
        assertEquals(updateApplication.getClientName(), updatingName);
    }

    private OAuthApplicationInfo createApplication(String randomName) throws APIManagementException {
        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientName(randomName);

        OAuthAppRequest oAuthAppRequest = mock(OAuthAppRequest.class);
        when(oAuthAppRequest.getOAuthApplicationInfo()).thenReturn(oAuthApplicationInfo);

        return azureADClient.createApplication(oAuthAppRequest);
    }

    private String getRandomName() {
        String random = RandomStringUtils.randomAscii(7);
        return "TEST_" + random;
    }

    private String createAndGetId() throws APIManagementException {
        String randomName = this.getRandomName();
        OAuthApplicationInfo createApplication = this.createApplication(randomName);
        return (String) createApplication.getParameter(AzureADConstants.OBJECT_ID);
    }

    private String createAndGetClientId() throws APIManagementException {
        String randomName = this.getRandomName();
        OAuthApplicationInfo createApplication = this.createApplication(randomName);
        return (String) createApplication.getClientId();
    }

}
