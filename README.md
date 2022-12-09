# Integrate Azure AD as a third-party Key Manager with WSO2 API Manager

Third-party Key Manager implementation which allows to integrate Azure AD with WSO2 API Manager. This repo contains a sample implementation that consumes the Microsoft's Graph API to create and manage Azure AD Applications.

> Kindly note that this implementation & feature is supported from WSO2 API Manager v3.2.0 onwards

[:construction:] Development in progress

## Build

Execute the following command from the project's root directory to build

Linux/ MACOS

```sh
mvn clean install
```

Windows

```sh
mvn clean install
```

## Quick Start

### Configure Azure AD

Prerequisites

- An Azure account that has an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- The Azure account must have permission to manage applications in Azure Active Directory (Azure AD). Any of the following Azure AD roles include the required permissions:

  - Application administrator
  - Application developer
  - Cloud application administrator

- Completion of the [Set up a tenant](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-create-new-tenant) quickstart.

Create Application

- Visit [Azure home](https://portal.azure.com/#home)
- Navigate to [Manage Azure Active Directory](https://portal.azure.com/#view/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/~/Overview)

- Navigate to [App registration](https://portal.azure.com/#view/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/~/RegisteredApps)

- Navigate to New registration, Give a name (Ex : KeyManger Client) and Click Register

Create secrets

- Navigate to `Certificates & secrets` > `Client secrets `>` New client secret`

- Fill the form with relevant information and give custom as the value for Expire.
- Make sure to copy the secret,it only shows once.

Configured permissions

- Navigate to `API permissions`
- Click `Add a permissions`
- Select `APIs my organization uses`
- Select `Microsoft Graph` from the list
- Click on `Application permissions`
- Expand `Application` and check
  - Application.Read.All
  - Application.ReadWrite.All
  - Application.ReadWrite.OwnedBy
- Click `Add permissions`

Copy application details

- Navigate to [Manage Azure Active Directory](https://portal.azure.com/#view/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/~/Overview)
- Click on 'Endpoints' and Note down
  - OAuth 2.0 token endpoint (v2) (NOT OAuth 2.0 token endpoint (v1))
  - OpenID Connect metadata document
  - Microsoft Graph API endpoint
- Under 'Owned applications' click on the newly created application
- Copy the Application (client) ID, (Object ID)

### Configure API manager

Start the API Manager server and log-in to the Admin portal to configure Azure AD as a Key Manager.

### Configure Key Manager

- Click on `Key Managers` from the side panel and then click `Add Key Manager`
- Fill the following
  - Name: `AzureAD`
  - Display Name: `Azure AD Key Manager`
  - Description _(optional)_
  - Key Manager Type: `Azure AD`
- Grant Types: `client_credentials`
- Connector Configurations
  - Microsoft Graph API Endpoint: paste the `Microsoft Graph API endpoint`
  - Client ID: paste the `Application (client) ID`
  - Client Secret: paste the client secret value that is generated
  - Well-known URL: paste the `OpenID Connect metadata document` URL collected from the endpoints and click on `Import`
  - Client Registration Endpoint: paste the `Microsoft Graph API endpoint`
  - Introspection Endpoint: paste the `OAuth 2.0 token endpoint (v2)` URL _(token introspection is not supported in Azure AD)_
  - Revoke Endpoint: paste the `OAuth 2.0 token endpoint (v2)` URL _(token revocation is not supported in Azure AD)_
- Connector Configurations
  - Microsoft Graph API Endpoint: paste the `Microsoft Graph API endpoint`
  - Client ID: paste the `Application (client) ID`
  - Client Secret: paste the client secret value that is generated
- Click on `Add`

### Create an Application & Generate Keys

Next, log-in to the Devportal and navigate to `Applications` section

- Click on `Add New Application`
- Fill the required information and click on `Add`
- Once created, navigate to the `Production Keys` section of that Application
- Select the `Azure AD Key Manager` and click on `Generate Keys`

### Post checks

Under [App registration](https://portal.azure.com/#view/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/~/RegisteredApps) there should be newly created application.

If we are using existing app registered. Make sure to check the `Expose an API` section and we have setup `Application ID URI`. The value should be

```code
api://<Application (client) ID>
```

We can also get this value by clicking the `set` link right next to the label. Default value is what we will see above.

Without this in the app,the token will be generate in a version 1 format and will not work with APIM KM due to failed signature.

## License

[Apache 2.0](LICENSE)

## Thanks

Big thanks for [https://github.com/athiththan11/apim-km-azure](athiththan11) for initial base project.
