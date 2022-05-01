package com.gcalsolaro.spring.cloud.gateway.security.jwt;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.CollectionUtils;

import com.auth0.jwt.interfaces.DecodedJWT;

public class CurrentUserAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private DecodedJWT decodedJWT;

	@Override
	public Object getCredentials() {
		return this.decodedJWT;
	}

	@Override
	public Object getPrincipal() {
		return this.decodedJWT.getSubject();
	}

	public String getJti() {
		return this.decodedJWT.getId();
	}

	public String getAud() {
		List<String> audience = this.decodedJWT.getAudience();
		return !CollectionUtils.isEmpty(audience) ? audience.get(0) : null;
	}

	public CurrentUserAuthenticationToken(DecodedJWT decodedJWT) {
		super(null);
		this.decodedJWT = decodedJWT;
		super.setAuthenticated(true);
	}

}