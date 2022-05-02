package com.gcalsolaro.spring.cloud.gateway.config;

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
@Configuration
public class AppConfig {
	
	// TODO - your config props here

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	@ConditionalOnMissingBean
	public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
		return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
	}
	
	@Bean
	@SuppressWarnings("deprecation")
	public ObjectMapper mapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
		return mapper;
	}

}