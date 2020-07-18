package com.cjgmj.webflux.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.cjgmj.webflux.models.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

}
