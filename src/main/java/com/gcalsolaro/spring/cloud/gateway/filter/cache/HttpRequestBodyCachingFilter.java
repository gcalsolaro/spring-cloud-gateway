package com.gcalsolaro.spring.cloud.gateway.filter.cache;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class HttpRequestBodyCachingFilter implements WebFilter {

	private static final byte[] EMPTY_BODY = new byte[0];

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		HttpMethod method = exchange.getRequest().getMethod();

		// GET and DELETE don't have a body
		if (method == null || method.matches(HttpMethod.GET.name()) || method.matches(HttpMethod.DELETE.name())) {
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
}
