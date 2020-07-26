package com.cjgmj.webflux.apirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.cjgmj.webflux.apirest.models.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String> {

}
