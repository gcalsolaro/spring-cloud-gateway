package com.gcalsolaro.spring.cloud.gateway.config;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

import com.gcalsolaro.spring.cloud.gateway.Constants;
import com.gcalsolaro.spring.cloud.gateway.filter.cache.HttpRequestBodyCachingFilter;
import com.gcalsolaro.spring.cloud.gateway.security.bearer.BearerReactiveAuthenticationManager;
import com.gcalsolaro.spring.cloud.gateway.security.bearer.ServerHttpBearerAuthenticationConverter;

import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

		// Disable things you don't need in spring security.
		http
			.authorizeExchange().pathMatchers(HttpMethod.HEAD).permitAll()
			.and().authorizeExchange().pathMatchers(HttpMethod.GET).permitAll()
			.and().authorizeExchange().pathMatchers(HttpMethod.POST).permitAll()
			.and().authorizeExchange().pathMatchers(HttpMethod.PUT).permitAll()
			.and().authorizeExchange().pathMatchers(HttpMethod.DELETE).permitAll()
			.and().authorizeExchange().pathMatchers(HttpMethod.PATCH).permitAll()
			.and().authorizeExchange().pathMatchers(HttpMethod.OPTIONS).permitAll()
			.and()
			.httpBasic().disable()
			.formLogin().disable()
			.csrf().disable()
			.logout().disable()
			.cors();
		
		// Those that do not require jwt token authentication should be pass.
		// TODO verificare la home page
		http.authorizeExchange().pathMatchers(Constants.OAS3_API_DOCS_PATH).permitAll();

		// Apply a JWT custom filter to all /** apis.
		http.authorizeExchange().pathMatchers("/**").authenticated();
		http.addFilterBefore(new HttpRequestBodyCachingFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
		http.addFilterAt(this.bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
		
		return http.build();
	}

	/**
	 * 
	 * @return
	 */
	private AuthenticationWebFilter bearerAuthenticationFilter() {
		AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(new BearerReactiveAuthenticationManager());
		Function<ServerWebExchange, Mono<Authentication>> bearerConverter = new ServerHttpBearerAuthenticationConverter();
		bearerAuthenticationFilter.setServerAuthenticationConverter(bearerConverter::apply);
		bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
		return bearerAuthenticationFilter;
	}

}
