package com.cjgmj.webflux.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.cjgmj.webflux.models.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String> {

}
