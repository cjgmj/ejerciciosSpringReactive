package com.cjgmj.webflux.client.models.services;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.cjgmj.webflux.client.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

	// Con eureka se añade .Builder y en cada client se añade .build()
	@Autowired
	private WebClient.Builder client;

	@Override
	public Flux<Producto> findAll() {
		return this.client.build().get().accept(APPLICATION_JSON).exchange()
				.flatMapMany(response -> response.bodyToFlux(Producto.class));
	}

	@Override
	public Mono<Producto> findById(String id) {
		final Map<String, Object> params = new HashMap<>();
		params.put("id", id);

		return this.client.build().get().uri("/{id}", params).accept(APPLICATION_JSON)
//				.exchange()
//				.flatMap(response -> response.bodyToMono(Producto.class));
				// Otra forma de obtener el body, también aplicable cuando es un Flux con
				// bodyToFlux
				.retrieve().bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return this.client.build().post().accept(APPLICATION_JSON).contentType(APPLICATION_JSON)
//				.body(fromValue(producto))
				// Otra forma de pasar el body
				.bodyValue(producto).retrieve().bodyToMono(Producto.class);
	}

	@Override
	public Mono<Producto> update(Producto producto, String id) {
		return this.client.build().put().uri("/{id}", Collections.singletonMap("id", id)).accept(APPLICATION_JSON)
				.contentType(APPLICATION_JSON).bodyValue(producto).retrieve().bodyToMono(Producto.class);
	}

	@Override
	public Mono<Void> delete(String id) {
		return this.client.build().delete().uri("/{id}", Collections.singletonMap("id", id)).retrieve()
				.bodyToMono(Void.class);
	}

	@Override
	public Mono<Producto> upload(FilePart file, String id) {
		final MultipartBodyBuilder parts = new MultipartBodyBuilder();
		parts.asyncPart("file", file.content(), DataBuffer.class).headers(h -> {
			h.setContentDispositionFormData("file", file.filename());
		});

		return this.client.build().post().uri("/upload/{id}", Collections.singletonMap("id", id))
				.contentType(MULTIPART_FORM_DATA).bodyValue(parts.build()).retrieve().bodyToMono(Producto.class);
	}

}
