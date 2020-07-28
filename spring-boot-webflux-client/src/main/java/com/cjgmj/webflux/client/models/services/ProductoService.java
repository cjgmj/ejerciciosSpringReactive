package com.cjgmj.webflux.client.models.services;

import org.springframework.http.codec.multipart.FilePart;

import com.cjgmj.webflux.client.models.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

	Flux<Producto> findAll();

	Mono<Producto> findById(String id);

	Mono<Producto> save(Producto producto);

	Mono<Producto> update(Producto producto, String id);

	Mono<Void> delete(String id);

	Mono<Producto> upload(FilePart file, String id);

}
