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
