package com.cjgmj.webflux.client.models.services;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cjgmj.webflux.client.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

	@Autowired
	private WebClient client;

	@Override
	public Flux<Producto> findAll() {
		return this.client.get().accept(APPLICATION_JSON).exchange()
				.flatMapMany(response -> response.bodyToFlux(Producto.class));
	}

	@Override
	public Mono<Producto> findById(String id) {
		final Map<String, Object> params = new HashMap<>();
		params.put("id", id);

		return this.client.get().uri("/{id}", params).accept(APPLICATION_JSON)
//				.exchange()
//				.flatMap(response -> response.bodyToMono(Producto.class));
				// Otra forma de obtener el body, también aplicable cuando es un Flux con
				// bodyToFlux
				.retrieve().bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return this.client.post().accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
//				.body(fromValue(producto))
				// Otra forma de pasar el body
				.bodyValue(producto).retrieve().bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> update(Producto producto, String id) {
		return this.client.put().uri("/{id}", Collections.singletonMap("id", id)).accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON).bodyValue(producto).retrieve().bodyToMono(Producto.class);
	}

	@Override
	public Mono<Void> delete(String id) {
		return this.client.delete().uri("/{id}", Collections.singletonMap("id", id)).exchange().then();
	}

}
