package org.wso2.azure.client;

import org.wso2.azure.client.model.ClientInformation;
import org.wso2.azure.client.model.ClientInformationList;
import org.wso2.azure.client.model.PasswordInfo;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface ApplicationClient {

	@RequestLine("POST /v1.0/applications")
	@Headers("Content-Type: application/json")
	public ClientInformation createApplication(ClientInformation applicationInfo)
			throws KeyManagerClientException;

	@RequestLine("GET /v1.0/applications/{id}")
	public ClientInformation getApplication(@Param("id") String id) throws KeyManagerClientException;

	/***
	 * id - ObjectId (NOT clientId)
	 */
	@RequestLine("DELETE /v1.0/applications/{id}")
	public void deleteApplication(@Param("id") String id) throws KeyManagerClientException;

	/***
	 * 
	 * @param id this is the clientId of the application
	 * @return
	 * @throws KeyManagerClientException
	 */
	@RequestLine("GET /v1.0/applications?$search=\"appId:{appId}\"&ConsistencyLevel=eventual")
	@Headers("ConsistencyLevel: eventual")
	public ClientInformationList searchByAppId(@Param("appId") String id) throws KeyManagerClientException;;

	/***
	 * 
	 * @param id this is the clientId of the application
	 * @return
	 * @throws KeyManagerClientException
	 */
	@RequestLine("GET /v1.0/applications?$search=\"displayName:TEST\"&ConsistencyLevel=eventual&$select=id")
	@Headers("ConsistencyLevel: eventual")
	public ClientInformationList getAllTestApplications() throws KeyManagerClientException;;

	@RequestLine("PATCH /v1.0/applications/{id}")
	@Headers("Content-Type: application/json")
	public void updateApplication(@Param("id") String id, ClientInformation applicationInfo)
			throws KeyManagerClientException;

	@RequestLine("POST /v1.0/applications/{id}/addPassword")
	@Headers("Content-Type: application/json")
	public PasswordInfo addPassword(@Param("id") String id, PasswordInfo passwordInfo) throws KeyManagerClientException;
}
