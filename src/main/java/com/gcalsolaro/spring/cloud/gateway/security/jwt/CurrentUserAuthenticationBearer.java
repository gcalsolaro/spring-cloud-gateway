package com.gcalsolaro.spring.cloud.gateway.security.jwt;

import org.springframework.security.core.Authentication;

import com.auth0.jwt.interfaces.DecodedJWT;

import reactor.core.publisher.Mono;

public class CurrentUserAuthenticationBearer {

	public static Mono<Authentication> create(DecodedJWT decodedJWT) {
		return Mono.justOrEmpty(new CurrentUserAuthenticationToken(decodedJWT));
	}

}
