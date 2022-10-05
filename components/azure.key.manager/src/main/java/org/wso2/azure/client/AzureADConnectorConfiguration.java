package org.wso2.azure.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;

import edu.emory.mathcs.backport.java.util.Arrays;

@Component(name = "azuread.configuration.component", immediate = true, service = KeyManagerConnectorConfiguration.class)
public class AzureADConnectorConfiguration implements KeyManagerConnectorConfiguration {

    @Override
    public String getImplementation() {
        return AzureADClient.class.getName();
    }

    @Override
    public String getJWTValidator() {
        return null;
    }

    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {
        String[] versions = new String[] { "v1.0" };
        List<ConfigurationDto> configurationDtoList = new ArrayList<>();
        configurationDtoList
                .add(new ConfigurationDto(AzureADConstants.GRAPH_API_ENDPOINT, "Microsoft Graph API Endpoint", "input",
                        "Microsoft's Graph API Endpoint", "https://graph.microsoft.com", true,
                        false, Collections.emptyList(), false));

        configurationDtoList
                .add(new ConfigurationDto(AzureADConstants.GRAPH_API_ENDPOINT_VERSION,
                        "Microsoft Graph API Endpoint Version", "select",
                        "Microsoft's Graph API Endpoint Version", "v1.0", true,
                        false, Arrays.asList(versions), false));
        configurationDtoList
                .add(new ConfigurationDto(AzureADConstants.AD_APP_CLIENT_ID, "Client ID", "input",
                        "Azure AD App Client ID", "", true,
                        false, Collections.emptyList(), false));
        configurationDtoList
                .add(new ConfigurationDto(AzureADConstants.AD_APP_CLIENT_SECRET, "Client Secret", "input",
                        "Azure AD App Client Secret", "", true,
                        true, Collections.emptyList(), false));

        return configurationDtoList;
    }

    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {
        return new ArrayList<>();
    }

    @Override
    public String getType() {
        return AzureADConstants.AZURE_AD;
    }

    @Override
    public String getDefaultConsumerKeyClaim() {
        return AzureADConstants.APP_ID;
    }

    @Override
    public String getDisplayName() {
        return AzureADConstants.AZURE_DISPLAY_NAME;
    }
}
