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
package org.wso2.azure.client.model;

import com.google.gson.annotations.SerializedName;

public class ClientInformation {

    @SerializedName("id")
    private String id;

    @SerializedName("appId")
    private String clientId;

    @SerializedName("displayName")
    private String appName;

    @SerializedName("identifierUris")
    private String[] identifierUris;

    @SerializedName("api")
    private ApiConfiguration api;

    private String clientSecret;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String[] getIdentifierUris() {
        return identifierUris;
    }

    public void setIdentifierUris(String[] identifierUris) {
        this.identifierUris = identifierUris;
    }

    public ApiConfiguration getApi() {
        return api;
    }

    public void setApi(ApiConfiguration api) {
        this.api = api;
    }

    @Override
    public String toString() {
        return "ClientInformation [id=" + id + ", clientId=" + clientId + ", appName=" + appName +
                ", api=" + api + "]";
    }

}
