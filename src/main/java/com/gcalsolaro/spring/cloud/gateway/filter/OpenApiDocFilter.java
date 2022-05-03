package com.gcalsolaro.spring.cloud.gateway.filter;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gcalsolaro.spring.cloud.gateway.Constants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Primary
@Component
public class OpenApiDocFilter extends ModifyResponseBodyGatewayFilterFactory {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private RouteDefinitionLocator locator;

	private final ErrorWebExceptionHandler errorWebExceptionHandler;

	/**
	 * 
	 * @param messageReaders
	 * @param messageBodyDecoders
	 * @param messageBodyEncoders
	 */
	public OpenApiDocFilter(List<HttpMessageReader<?>> messageReaders, Set<MessageBodyDecoder> messageBodyDecoders, Set<MessageBodyEncoder> messageBodyEncoders, ErrorWebExceptionHandler errorWebExceptionHandler) {
		super(messageReaders, messageBodyDecoders, messageBodyEncoders);
		this.errorWebExceptionHandler = errorWebExceptionHandler;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return new OpenApiModifyResponseGatewayFilter(config);
	}

	public class OpenApiModifyResponseGatewayFilter extends ModifyResponseGatewayFilter {
		OpenApiModifyResponseGatewayFilter(Config config) {
			super(config);
		}

		@Override
		public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
			ServerHttpResponse serverHttpResponse = this.getServerHttpResponseFromSuper(exchange);
			DataBufferFactory bufferFactory = serverHttpResponse.bufferFactory();

			final ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
				@Override
				public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
					if (this.shouldServeErrorPage(exchange)) {
						exchange.getResponse().getHeaders().setContentLength(-1);
						return errorWebExceptionHandler.handle(exchange, new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));
					}

					if (body instanceof Flux) {
						Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
						return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
							DefaultDataBuffer joinedBuffers = new DefaultDataBufferFactory().join(dataBuffers);

							byte[] bytes = null;
							byte[] content = new byte[joinedBuffers.readableByteCount()];
							joinedBuffers.read(content);

							try {
								bytes = mapper.writeValueAsBytes(this.manipulateOpenApiResource(content));
							} catch (Exception e) {
								log.error("Exception during OpenApiResource serialization...rollback! {}", e.getMessage());
								super.writeWith(body);
							}

							serverHttpResponse.getHeaders().setContentLength(bytes.length);
							return bufferFactory.wrap(bytes);
						}));
					}
					return super.writeWith(body);
				}

				/**
				 * 
				 * @param exchange
				 * @return
				 */
				private boolean shouldServeErrorPage(ServerWebExchange exchange) {
					HttpStatus statusCode = this.getHttpStatus(exchange);
					return statusCode.is5xxServerError() || statusCode.is4xxClientError();
				}

				/**
				 * 
				 * @param exchange
				 * @return
				 */
				private HttpStatus getHttpStatus(ServerWebExchange exchange) {
					return Optional.ofNullable(exchange.getResponse().getStatusCode()).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
				}

				/**
				 * 
				 * @param content
				 * @return
				 * @throws IOException
				 */
				private ObjectNode manipulateOpenApiResource(byte[] content) throws IOException {
					ObjectNode json = (ObjectNode) mapper.readTree(content);
					
					locator.getRouteDefinitions().collectList().subscribe(definitions -> {
						URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
						String routeUriId = this.extractRouteURI(routeUri);
						Optional<RouteDefinition> rd = definitions.stream().filter(route -> StringUtils.split(route.getId(), "-")[0].equals(routeUriId)).findFirst();
						if (rd.isPresent()) {
							// TODO - your manipulation logic here. Manipulate as json
						}
					});
					
					// FIXME - Example - Infer Gateway Server
					this.inferServer(json);

					return json;
				}
				
				/**
				 * 
				 * @param json
				 */
				private void inferServer(ObjectNode json) {
					URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
					JsonNode pathsJsonNode = json.findValue(Constants.OAS3_SERVERS);
					ObjectNode server = (ObjectNode) ((ArrayNode) pathsJsonNode).get(0);
					server.put(Constants.OAS3_URL, StringUtils.remove(routeUri.toString(), Constants.OAS3_API_DOCS_PATH));
					server.put(Constants.OAS3_DESCRIPTION, Constants.OAS3_SERVERS_URL_DESCRIPTION);
				}
				
				/**
				 * 
				 * @param routeUri
				 * @return
				 */
				private String extractRouteURI(URI routeUri) {
					String[] uri = StringUtils.split(StringUtils.remove(routeUri.toString(), Constants.OAS3_API_DOCS_PATH), "/");
					return uri[uri.length - 1];
				}

			};
			return chain.filter(exchange.mutate().response(responseDecorator).build());
		}

		/**
		 * 
		 * @param exchange
		 * @return
		 */
		private ServerHttpResponse getServerHttpResponseFromSuper(ServerWebExchange exchange) {
			ServerHttpResponse[] serverHttpResponse = new ServerHttpResponse[1];
			super.filter(exchange, chain -> {
				serverHttpResponse[0] = chain.getResponse(); // capture the response when the super sets it
				return null;
			});
			return serverHttpResponse[0];
		}
	}
}
