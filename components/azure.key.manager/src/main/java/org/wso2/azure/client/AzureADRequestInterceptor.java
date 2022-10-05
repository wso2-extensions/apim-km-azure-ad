package org.wso2.azure.client;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AzureADRequestInterceptor implements RequestInterceptor {

    private AccessTokenGenerator accessTokenGenerator;

    public AzureADRequestInterceptor(AccessTokenGenerator accessTokenGenerator) {
        this.accessTokenGenerator = accessTokenGenerator;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String[] scopes = { AzureADConstants.MICROSOFT_DEFAULT_SCOPE };
        String accessToken = accessTokenGenerator.getAccessToken(scopes);
        requestTemplate
                .header(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BEARER + accessToken);
    }

}
