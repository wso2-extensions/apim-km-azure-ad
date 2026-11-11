/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.azure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.wso2.azure.client.model.ClientInformation;
import org.wso2.azure.client.model.PasswordInfo;
import org.wso2.azure.client.model.ServicePrincipalRequest;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for application creation in {@link AzureADClient}.
 */
public class AzureADClientTest {

    private static final String OBJECT_ID = "object-id";
    private static final String CLIENT_ID = "client-id";

    private ApplicationClient appClient;
    private AzureADClient azureADClient;

    @BeforeEach
    public void setUp() throws Exception {
        appClient = mock(ApplicationClient.class);
        azureADClient = new AzureADClient();
        setField("appClient", appClient);
        // Keep polling short so that failing operations give up quickly in tests.
        setField("pollingInitialDelayMs", 1L);
        setField("pollingMaxWaitTimeMs", 5L);

        ClientInformation createdApp = new ClientInformation();
        createdApp.setId(OBJECT_ID);
        createdApp.setClientId(CLIENT_ID);
        when(appClient.createApplication(any(ClientInformation.class))).thenReturn(createdApp);
    }

    @Test
    public void testCreateApplicationSucceedsWithoutDeletingApplication() throws Exception {
        PasswordInfo passwordInfo = new PasswordInfo();
        passwordInfo.setSecret("secret");
        when(appClient.addPassword(eq(OBJECT_ID), any(PasswordInfo.class))).thenReturn(passwordInfo);

        OAuthApplicationInfo result = azureADClient.createApplication(createAppRequest());

        assertEquals(CLIENT_ID, result.getClientId());
        assertEquals("secret", result.getClientSecret());
        verify(appClient, never()).deleteApplication(anyString());
    }

    @Test
    public void testCreateApplicationDeletesApplicationWhenAddPasswordFails() throws Exception {
        when(appClient.addPassword(eq(OBJECT_ID), any(PasswordInfo.class)))
                .thenThrow(new KeyManagerClientException(404, "Not Found"));

        assertThrows(APIManagementException.class, () -> azureADClient.createApplication(createAppRequest()));

        verify(appClient).deleteApplication(OBJECT_ID);
    }

    @Test
    public void testCreateApplicationDeletesApplicationWhenServicePrincipalCreationFails() throws Exception {
        PasswordInfo passwordInfo = new PasswordInfo();
        passwordInfo.setSecret("secret");
        when(appClient.addPassword(eq(OBJECT_ID), any(PasswordInfo.class))).thenReturn(passwordInfo);
        doThrow(new KeyManagerClientException(403, "Forbidden"))
                .when(appClient).createServicePrincipal(any(ServicePrincipalRequest.class));

        assertThrows(APIManagementException.class, () -> azureADClient.createApplication(createAppRequest()));

        verify(appClient).deleteApplication(OBJECT_ID);
    }

    @Test
    public void testCreateApplicationReportsOriginalErrorWhenCleanupFails() throws Exception {
        when(appClient.addPassword(eq(OBJECT_ID), any(PasswordInfo.class)))
                .thenThrow(new KeyManagerClientException(404, "Not Found"));
        doThrow(new KeyManagerClientException(500, "Internal Server Error"))
                .when(appClient).deleteApplication(OBJECT_ID);

        APIManagementException exception = assertThrows(APIManagementException.class,
                () -> azureADClient.createApplication(createAppRequest()));

        assertTrue(exception.getMessage().startsWith("Add password failed"),
                "The setup error, not the cleanup error, should be reported");
        verify(appClient, atLeastOnce()).deleteApplication(OBJECT_ID);
    }

    private OAuthAppRequest createAppRequest() {
        OAuthApplicationInfo oauthAppInfo = new OAuthApplicationInfo();
        oauthAppInfo.setClientName("test-app");
        OAuthAppRequest request = new OAuthAppRequest();
        request.setOAuthApplicationInfo(oauthAppInfo);
        return request;
    }

    private void setField(String name, Object value) throws Exception {
        Field field = AzureADClient.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(azureADClient, value);
    }
}
