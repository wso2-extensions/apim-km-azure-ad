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

public class AzureADConstants {

    public static final String AZURE = "Azure AD";
    public static final String AZURE_AD = "AzureAD";
    public static final String AZURE_DISPLAY_NAME = "Azure AD";

    public static final String GRAPH_API_ENDPOINT = "microsoft_graph_api_endpoint";
    public static final String GRAPH_API_ENDPOINT_VALUE = "https://graph.microsoft.com";
    public static final String GRAPH_API_ENDPOINT_VERSION = "v1.0";
    // public static final String APP_ID = "appid";
    public static final String AZP = "azp";
    public static final String OBJECT_ID = "id";
    public static final String AD_APP_CLIENT_ID = "azure_ad_client_id";
    public static final String AD_APP_CLIENT_SECRET = "azure_ad_client_secret";
    public static final String AD_APP_TENANT = "azure_ad_tenant";
    public static final String MICROSOFT_DEFAULT_SCOPE = "https://graph.microsoft.com/.default";
    public static final String API_ID_URI_TEMPLATE = "api://%s";
    public static final String API_SCOPE_TEMPLATE = "api://%s/.default";

    public static final String GRANT_TYPE = "grant_type";
    public static final String SCOPE = "scope";
    public static final String CLIENT_CREDENTIALS_GRANT_TYPE = "client_credentials";
    public static final String BASIC = "Basic ";
    public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";

}
