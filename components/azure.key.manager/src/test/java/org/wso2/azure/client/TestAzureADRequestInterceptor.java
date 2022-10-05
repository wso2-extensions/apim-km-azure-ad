package org.wso2.azure.client;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wso2.carbon.apimgt.impl.APIConstants;

import feign.RequestTemplate;

@ExtendWith(MockitoExtension.class)
public class TestAzureADRequestInterceptor {

    AzureADRequestInterceptor adRequestInterceptor;

    // @BeforeEach
    public void before() {

        // this.adRequestInterceptor = new
        // AzureADRequestInterceptor(AzureAdValues.tokenEndpoint,
        // AzureAdValues.clientId,
        // AzureAdValues.clientSecret);
    }

    // @Test
    public void itShouldAppendTokenToHeader() {
        RequestTemplate requestTemplate = mock(RequestTemplate.class);

        adRequestInterceptor.apply(requestTemplate);

        String key = APIConstants.AUTHORIZATION_HEADER_DEFAULT;
        verify(requestTemplate).header(eq(key),
                startsWith(APIConstants.AUTHORIZATION_BEARER));
    }

}
