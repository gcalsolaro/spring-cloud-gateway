package com.gcalsolaro.spring.cloud.gateway.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.gcalsolaro.spring.cloud.gateway.exception.UnauthorizedException;
import com.gcalsolaro.spring.cloud.gateway.security.jwt.CurrentUserAuthenticationToken;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuditFilter implements WebFilter {

	private String requestBody, responseBody = null;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		/**
		 * Request Decorator
		 */
		ServerHttpRequestDecorator serverHttpRequestDecorator = new ServerHttpRequestDecorator(exchange.getRequest()) {

			@Override
			public Flux<DataBuffer> getBody() {
				requestBody = null;
				return super.getBody().doOnNext(dataBuffer -> {
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
						requestBody = IOUtils.toString(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8.name());
						log.debug("Request Body {}", requestBody);
					} catch (IOException e) {
						log.error(requestBody, e);
					}
				});
			}
		};

		/**
		 * Response Decorator
		 */
		ServerHttpResponseDecorator serverHttpResponseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {

			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				responseBody = null;
				Mono<DataBuffer> buffer = Mono.from(body);
				return super.writeWith(buffer.doOnNext(dataBuffer -> {
					try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
						Channels.newChannel(byteArrayOutputStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
						responseBody = IOUtils.toString(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8.name());
						log.debug("Response Body {}", responseBody);
					} catch (Exception e) {
						log.error(responseBody, e);
					}
				}));
			}
		};

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Main Filter Operation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
		final String originalUri = exchange.getRequest().getURI().toString();

		if (this.isSkippableUri(originalUri)) {
			return chain.filter(exchange);
		}

		return chain.filter(exchange.mutate().request(serverHttpRequestDecorator).response(serverHttpResponseDecorator).build()).then(ReactiveSecurityContextHolder.getContext().map(securityContext -> {
			Authentication auth = securityContext.getAuthentication();

			if (auth != null) {
				CurrentUserAuthenticationToken loggedUser = (CurrentUserAuthenticationToken) auth;
				log.info("Audit GatewayFilter: request with Auth parameters incoming! Principal(sub): " + loggedUser.getPrincipal() + " - aud: " + loggedUser.getAud() + " - jti: " + loggedUser.getJti());
				log.info("Audit GatewayFilter: response HttpStatus " + exchange.getResponse().getStatusCode());

				// TODO - your auth logic here
			} else {
				log.error("Fatal - Expected Authorization not found! Something goes wrong...");
				throw new UnauthorizedException("Fatal - Expected Authorization not found!");
			}
			return auth;
		}).then(Mono.fromRunnable(() -> {
			log.info("Audit GatewayFilter: audit success!");
		})));
	}

	/**
	 * Check if current URI is skippable from authentication
	 * 
	 * @param originalUri
	 * @return
	 */
	private boolean isSkippableUri(String originalUri) {
		// TODO - your logic here
		return false;
	}
}
