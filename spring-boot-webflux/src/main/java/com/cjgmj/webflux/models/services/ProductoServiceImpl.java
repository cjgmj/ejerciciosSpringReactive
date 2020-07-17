package com.cjgmj.webflux.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cjgmj.webflux.models.dao.ProductoDao;
import com.cjgmj.webflux.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductoServiceImpl implements ProductoService {

	@Autowired
	private ProductoDao productoDao;

	@Override
	public Flux<Producto> findAll() {
		return this.productoDao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return this.productoDao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return this.productoDao.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		return this.productoDao.delete(producto);
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCase() {
		return this.productoDao.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});
	}

	@Override
	public Flux<Producto> findAllConNombreUpperCaseRepeat() {
		return this.findAllConNombreUpperCaseRepeat().repeat(5000);
	}

}