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

import org.wso2.azure.client.model.ClientInformation;
import org.wso2.azure.client.model.ClientInformationList;
import org.wso2.azure.client.model.PasswordInfo;
import org.wso2.azure.client.model.ServicePrincipalRequest;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/***
 * Client for communicating with Microsoft Graph API v1.0
 */
public interface ApplicationClient {

	@RequestLine("POST /v1.0/applications")
	@Headers("Content-Type: application/json")
	public ClientInformation createApplication(ClientInformation applicationInfo)
			throws KeyManagerClientException;

	@RequestLine("GET /v1.0/applications/{id}")
	public ClientInformation getApplication(@Param("id") String id) throws KeyManagerClientException;

	/***
	 * @param id Application ID
	 * @throws KeyManagerClientException If application deletion failed
	 */
	@RequestLine("DELETE /v1.0/applications/{id}")
	public void deleteApplication(@Param("id") String id) throws KeyManagerClientException;

	/***
	 * 
	 * @param id this is the clientId of the application
	 * @return Client information
	 * @throws KeyManagerClientException If search by app id failed
	 */
	@RequestLine("GET /v1.0/applications?$search=\"appId:{appId}\"&ConsistencyLevel=eventual")
	@Headers("ConsistencyLevel: eventual")
	public ClientInformationList searchByAppId(@Param("appId") String id) throws KeyManagerClientException;;

	/***
	 * @param id Application ID
	 * @throws KeyManagerClientException If application retrieval failed
	 */
	@RequestLine("GET /v1.0/applications(appId='{appid}')")
	public ClientInformation getApplicationByAppId(@Param("appid") String id) throws KeyManagerClientException;

	/***
	 * 
	 * @return Client Information
	 * @throws KeyManagerClientException If get test applications failed
	 */
	@RequestLine("GET /v1.0/applications?$search=\"displayName:TEST\"&ConsistencyLevel=eventual&$select=id")
	@Headers("ConsistencyLevel: eventual")
	public ClientInformationList getAllTestApplications() throws KeyManagerClientException;;

	@RequestLine("PATCH /v1.0/applications/{id}")
	@Headers("Content-Type: application/json")
	public void updateApplication(@Param("id") String id, ClientInformation applicationInfo)
			throws KeyManagerClientException;

	@RequestLine("POST /v1.0/servicePrincipals")
	@Headers("Content-Type: application/json")
	public void createServicePrincipal(ServicePrincipalRequest servicePrincipalRequest)
			throws KeyManagerClientException;

	@RequestLine("POST /v1.0/applications/{id}/addPassword")
	@Headers("Content-Type: application/json")
	public PasswordInfo addPassword(@Param("id") String id, PasswordInfo passwordInfo) throws KeyManagerClientException;
}
