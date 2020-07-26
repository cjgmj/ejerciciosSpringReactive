package com.cjgmj.webflux.apirest.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.cjgmj.webflux.apirest.models.documents.Producto;
import com.cjgmj.webflux.apirest.models.services.ProductoService;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Autowired
	private ProductoService productoService;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(this.productoService.findAll(),
				Producto.class);
	}
}
