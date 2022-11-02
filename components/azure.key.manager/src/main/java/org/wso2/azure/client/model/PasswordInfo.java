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

public class PasswordInfo {

    @SerializedName("displayName")
    private String name;

    @SerializedName("secretText")
    private String secret;

    @SerializedName("passwordCredential")
    private PasswordCredential passwordCredential;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public PasswordCredential getPasswordCredential() {
        return passwordCredential;
    }

    public void setPasswordCredential(PasswordCredential passwordCredential) {
        this.passwordCredential = passwordCredential;
    }

}
