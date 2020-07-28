package com.cjgmj.webflux.client.handler;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
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

		return this.errorHandler(this.productoService.findById(id)
				.flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(p))
				.switchIfEmpty(ServerResponse.notFound().build()));
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

		return this.errorHandler(producto.flatMap(p -> this.productoService.update(p, id))
				.flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(p)));
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		final String id = request.pathVariable("id");

		return this.errorHandler(this.productoService.delete(id).then(ServerResponse.noContent().build()));
	}

	public Mono<ServerResponse> upload(ServerRequest request) {
		final String id = request.pathVariable("id");

		return this.errorHandler(request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class).flatMap(file -> this.productoService.upload(file, id))
				.flatMap(p -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(p)));
	}

	private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {
		return response.onErrorResume(error -> {
			final WebClientResponseException errorResponse = (WebClientResponseException) error;

			if (errorResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
				final Map<String, Object> body = new HashMap<>();
				body.put("error", "No existe el producto: ".concat(errorResponse.getMessage()));
				body.put("timestamp", new Date());
				body.put("status", errorResponse.getStatusCode().value());

				return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(body);
			}

			return Mono.error(errorResponse);
		});
	}

}
