package org.wso2.azure.client.model;

import com.google.gson.annotations.SerializedName;

public class ClientInformation {

    @SerializedName("id")
    private String id;

    @SerializedName("appId")
    private String clientId;

    @SerializedName("displayName")
    private String appName;

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

}
