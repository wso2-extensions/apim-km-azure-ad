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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                .add(new ConfigurationDto(
                        AzureADConstants.AZURE_AD_REQUESTED_ACCESS_TOKEN_VERSION,
                        "Requested Access Token Version",
                        "options",
                        "Select the requested access token version",
                        "v1.0",
                        false,
                        false,
                        AzureADConstants.AZURE_AD_ALLOWED_ACCESS_TOKEN_VERSIONS.keySet().stream()
                                .sorted()
                                .collect(Collectors.toList()),
                        false,
                        true
                ));
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
        return AzureADConstants.AZP;
    }

    @Override
    public String getDisplayName() {
        return AzureADConstants.AZURE_DISPLAY_NAME;
    }
}
