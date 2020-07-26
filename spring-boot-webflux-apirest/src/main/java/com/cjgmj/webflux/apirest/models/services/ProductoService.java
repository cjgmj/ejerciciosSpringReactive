package com.cjgmj.webflux.apirest.models.services;

import com.cjgmj.webflux.apirest.models.documents.Categoria;
import com.cjgmj.webflux.apirest.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoService {

	Flux<Producto> findAll();

	Flux<Producto> findAllConNombreUpperCase();

	Flux<Producto> findAllConNombreUpperCaseRepeat();

	Mono<Producto> findById(String id);

	Mono<Producto> save(Producto producto);

	Mono<Void> delete(Producto producto);

	Flux<Categoria> findAllCategoria();

	Mono<Categoria> findCategoriaById(String id);

	Mono<Categoria> saveCategoria(Categoria categoria);
}
