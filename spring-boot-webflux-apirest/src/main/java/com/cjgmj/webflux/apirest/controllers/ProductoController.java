package com.cjgmj.webflux.apirest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cjgmj.webflux.apirest.models.documents.Producto;
import com.cjgmj.webflux.apirest.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	@Autowired
	private ProductoService productoService;

	@GetMapping
	public Flux<Producto> lista() {
		return this.productoService.findAll();
	}

	@GetMapping("/lista/mono")
	public Mono<ResponseEntity<Flux<Producto>>> listaMono() {
		// Se podría hacer también con ResponseEntity.ok(this.productoService.findAll())
		return Mono
				.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(this.productoService.findAll()));
	}

}
