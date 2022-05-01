package com.gcalsolaro.spring.cloud.gateway.security.bearer;

import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;

import com.gcalsolaro.spring.cloud.gateway.Constants;
import com.gcalsolaro.spring.cloud.gateway.security.jwt.AuthorizationHeaderPayload;
import com.gcalsolaro.spring.cloud.gateway.security.jwt.CurrentUserAuthenticationBearer;
import com.gcalsolaro.spring.cloud.gateway.security.jwt.JwtVerifyHandler;

import reactor.core.publisher.Mono;

public class ServerHttpBearerAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {

	private static final Predicate<String> matchBearerLength = 
			authValue -> authValue.length() > Constants.AUTH_BEARER_AUTHORIZATION_TOKEN.length();
			
	private static final Function<String, Mono<String>> isolateBearerValue = 
			authValue -> Mono.justOrEmpty(authValue.substring(Constants.AUTH_BEARER_AUTHORIZATION_TOKEN.length()));

	@Override
	public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {

		JwtVerifyHandler jwtVerifier = new JwtVerifyHandler();
		
		return Mono.justOrEmpty(serverWebExchange)
				.flatMap(AuthorizationHeaderPayload::extract)
				.filter(matchBearerLength)
				.flatMap(isolateBearerValue)
				.flatMap(jwtVerifier::check)
				.flatMap(CurrentUserAuthenticationBearer::create)
				.log();
	}

}
