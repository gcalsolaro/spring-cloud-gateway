package com.gcalsolaro.spring.cloud.gateway.security.jwt;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.authentication.BadCredentialsException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gcalsolaro.spring.cloud.gateway.exception.JwtApiException;
import com.gcalsolaro.spring.cloud.gateway.exception.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class JwtVerifyHandler {

	/**
	 * 
	 * @param accessToken
	 * @return
	 */
	public Mono<DecodedJWT> check(String accessToken) {
		DecodedJWT signedJWT = null;

		try {
			// TODO - your logic here
			signedJWT = this.verifyJwtToken(accessToken);
		} catch (Exception ex) {
			log.error("Bearer JWT Error - StackTrace -> " + ExceptionUtils.getStackTrace(ex));
			return Mono.error(new UnauthorizedException("Invalid Bearer JWT Token"));
		}

		return Mono.just(signedJWT).log().onErrorResume(e -> Mono.error(new UnauthorizedException("Invalid Bearer JWT Token")));
	}

	/**
	 * 
	 * @param accessToken
	 * @return
	 * @throws BadCredentialsException
	 */
	private DecodedJWT verifyJwtToken(String accessToken) throws BadCredentialsException {
		try {
			// Step 1 - Decode JWT AccessToken
			DecodedJWT decodedJWT = JWT.decode(accessToken);
			log.debug("Decode Bearer JWT AccessToken: Success!");

			// Step 2 - Check for expired Token
			this.checkExpiresAtClaim(decodedJWT.getExpiresAt());
			log.debug("Check for expired Bearer JWT Token: Success!");

			// Step 3 - Check aud
			this.checkAudClaim(decodedJWT.getAudience());
			log.debug("Check Bearer JWT aud: Success!");

			// Step 4 - Return decodedJWT
			log.debug("Verify Bearer JWT Token: Success! Exit...");
			return decodedJWT;
		} catch (SignatureVerificationException e) {
			throw new BadCredentialsException("Error trusting Bearer JWT Token with well-know RSAPublicKey", e);
		} catch (JwtApiException e) {
			throw new BadCredentialsException("Exception checking Bearer JWT claims", e);
		} catch (Exception e) {
			log.error("Auth verification error: " + e.toString());
			throw new BadCredentialsException("Auth verification error", e);
		}
	}

	/**
	 * 
	 * @param expiresAt
	 * @throws JwtApiException
	 */
	private void checkExpiresAtClaim(Date expiresAt) throws JwtApiException {
		// TODO
	}

	/**
	 * 
	 * @param audiences
	 * @throws JwtApiException
	 */
	private void checkAudClaim(List<String> audiences) throws JwtApiException {
		// TODO
	}
}
