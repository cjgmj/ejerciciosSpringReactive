package com.cjgmj.webflux.apirest.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.cjgmj.webflux.apirest.models.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

}
