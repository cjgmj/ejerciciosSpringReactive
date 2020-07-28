package com.cjgmj.webflux.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

	@Value("${config.base.endpoint}")
	private String url;

	// Configuración sin eureka
//	@Bean
//	public WebClient registrarWebClient() {
//		return WebClient.create(this.url);
//	}

	// Configuración con eureka
	@Bean
	@LoadBalanced
	public WebClient.Builder registrarWebClient() {
		return WebClient.builder().baseUrl(this.url);
	}

}
