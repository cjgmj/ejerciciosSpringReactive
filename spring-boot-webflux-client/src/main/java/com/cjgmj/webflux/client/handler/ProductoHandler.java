package com.cjgmj.webflux.client.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.cjgmj.webflux.client.models.Producto;
import com.cjgmj.webflux.client.models.services.ProductoService;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Autowired
	private ProductoService productoService;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(APPLICATION_JSON).body(this.productoService.findAll(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		final String id = request.pathVariable("id");

		return this.productoService.findById(id)
				.flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(p))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		final Mono<Producto> producto = request.bodyToMono(Producto.class);

		return producto.flatMap(p -> {
			if (p.getCreateAt() == null) {
				p.setCreateAt(new Date());
			}

			return this.productoService.save(p);
		}).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
				.contentType(APPLICATION_JSON).bodyValue(p)).onErrorResume(error -> {
					final WebClientResponseException errorResponse = (WebClientResponseException) error;

					if (errorResponse.getStatusCode() == HttpStatus.BAD_REQUEST) {
						return ServerResponse.badRequest().contentType(APPLICATION_JSON)
								.bodyValue(errorResponse.getResponseBodyAsString());
					}

					return Mono.error(errorResponse);
				});
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		final String id = request.pathVariable("id");
		final Mono<Producto> producto = request.bodyToMono(Producto.class);

		return producto.flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON)
				.body(this.productoService.update(p, id), Producto.class));
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		final String id = request.pathVariable("id");

		return this.productoService.delete(id).then(ServerResponse.noContent().build());
	}

}
