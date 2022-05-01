package com.gcalsolaro.spring.cloud.gateway.filter.cache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HttpRequestBodyCachingFilter implements WebFilter {

	private static final byte[] EMPTY_BODY = new byte[0];

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		final String originalUri = exchange.getRequest().getURI().toString();
		HttpMethod method = exchange.getRequest().getMethod();

		// GET and DELETE don't have a body
		if (this.isSkippableUri(originalUri) || method == null || method.matches(HttpMethod.GET.name()) || method.matches(HttpMethod.DELETE.name())) {
			return chain.filter(exchange);
		}

		return DataBufferUtils.join(exchange.getRequest().getBody()).map(dataBuffer -> {
			byte[] bytes = new byte[dataBuffer.readableByteCount()];
			dataBuffer.read(bytes);
			DataBufferUtils.release(dataBuffer);
			return bytes;
		}).defaultIfEmpty(EMPTY_BODY).flatMap(bytes -> {
			ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
				@Override
				public Flux<DataBuffer> getBody() {
					if (bytes.length > 0) {
						DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
						return Flux.just(dataBufferFactory.wrap(bytes));
					}
					return Flux.empty();
				}
			};
			return chain.filter(exchange.mutate().request(decorator).build());
		});
	}
	
	/**
	 * Check if current URI is skippable from Caching
	 * 
	 * @param originalUri
	 * @return
	 */
	private boolean isSkippableUri(String originalUri) {
		String[] skippable = new String[] { ".html", ".css", ".js", ".png", ".ico", ".map", "/v3/api-docs/swagger-config", "/v3/api-docs/example", "primaryName=example", "/v3/api-docs" };
		if (StringUtils.endsWithAny(originalUri, skippable)) {
			log.debug("Caching GatewayFilter logging: incoming request " + originalUri + " - Skip from Cache...");
			return true;
		}
		log.debug("Caching GatewayFilter logging: incoming request " + originalUri + " - Not Skippable! Caching if necessary...");
		return false;
	}
}
