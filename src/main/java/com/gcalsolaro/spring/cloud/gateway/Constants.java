package com.gcalsolaro.spring.cloud.gateway;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

	// AUTHENTICATION
	public final String AUTH_BEARER_AUTHORIZATION_TOKEN = "Bearer ";

	// OAS3
	public final String OAS3_API_DOCS_PATH = "/v3/api-docs";
	public final String OAS3_API_DOCS_CONFIG_PATH = OAS3_API_DOCS_PATH + "/swagger-config";
	public final String OAS3_AUTH_SECURITY_SCHEMES = "securitySchemes";
	public final String OAS3_AUTH_SCHEMA_BEARER = "bearerAuth";
	public final String OAS3_AUTH_SECURITY = "security";
	public final String OAS3_COMPONENTS = "components";
	public final String OAS3_SERVERS = "servers";
	public final String OAS3_PATHS = "paths";
	public final String OAS3_URLS = "urls";
	public final String OAS3_URL = "url";

	// YAML - SECURE
	public final String YAML_API_VERSION = "api_version";

}
