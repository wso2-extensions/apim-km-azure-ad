package org.wso2.azure.client.model;

import com.google.gson.annotations.SerializedName;

public class PasswordCredential {

    @SerializedName("displayName")
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
