package com.gcalsolaro.spring.cloud.gateway;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

	// AUTHENTICATION
	public final String AUTH_BEARER_AUTHORIZATION_TOKEN = "Bearer ";

	// OAS3
	public final String OAS3_API_DOCS_PATH = "/v3/api-docs";
	public final String OAS3_SERVERS = "servers";
	public final String OAS3_URL = "url";
	public final String OAS3_DESCRIPTION = "description";
	public final String OAS3_SERVERS_URL_DESCRIPTION = "Interoperability API Gateway";

	// YAML - SECURE
	public final String YAML_API_VERSION = "api_version";

}
