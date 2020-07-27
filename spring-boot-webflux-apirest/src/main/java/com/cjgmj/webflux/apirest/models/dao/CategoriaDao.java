package com.cjgmj.webflux.apirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.cjgmj.webflux.apirest.models.documents.Categoria;

import reactor.core.publisher.Mono;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

	Mono<Categoria> findByNombre(String nombre);
}
