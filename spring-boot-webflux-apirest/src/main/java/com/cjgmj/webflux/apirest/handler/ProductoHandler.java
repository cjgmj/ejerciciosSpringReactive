package com.cjgmj.webflux.apirest.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.cjgmj.webflux.apirest.models.documents.Categoria;
import com.cjgmj.webflux.apirest.models.documents.Producto;
import com.cjgmj.webflux.apirest.models.services.ProductoService;

import reactor.core.publisher.Mono;

@Component
public class ProductoHandler {

	@Value("${config.uploads.path}")
	private String path;

	@Autowired
	private ProductoService productoService;

	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(this.productoService.findAll(),
				Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		final String id = request.pathVariable("id");
		// Cuando no es un tipo reactivo hay que añadirlo a través de BodyInserters
		return this.productoService.findById(id)
				.flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		final Mono<Producto> producto = request.bodyToMono(Producto.class);

		return producto.flatMap(p -> {
			if (p.getCreateAt() == null) {
				p.setCreateAt(new Date());
			}

			return this.productoService.save(p);
		}).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(fromValue(p)));
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		final Mono<Producto> producto = request.bodyToMono(Producto.class);
		final String id = request.pathVariable("id");

		final Mono<Producto> productoDb = this.productoService.findById(id);

		return productoDb.zipWith(producto, (db, req) -> {
			db.setNombre(req.getNombre());
			db.setPrecio(req.getPrecio());
			db.setCategoria(req.getCategoria());

			return db;
		}).flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(this.productoService.save(p),
				Producto.class)).switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> eliminar(ServerRequest request) {
		final String id = request.pathVariable("id");

		final Mono<Producto> productoDb = this.productoService.findById(id);

		return productoDb.flatMap(p -> this.productoService.delete(p).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

	public Mono<ServerResponse> upload(ServerRequest request) {
		final String id = request.pathVariable("id");

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> this.productoService.findById(id).flatMap(p -> {
					p.setFoto(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));

					return file.transferTo(new File(this.path + p.getFoto())).then(this.productoService.save(p));
				})).flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(p)))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crearConFoto(ServerRequest request) {
		final Mono<Producto> producto = request.multipartData().map(multipart -> {
			final FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			final FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			final FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			final FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");

			final Categoria categoria = new Categoria(categoriaNombre.value());
			categoria.setId(categoriaId.value());

			return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);
		});

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file")).cast(FilePart.class)
				.flatMap(file -> producto.flatMap(p -> {
					p.setFoto(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));

					p.setCreateAt(new Date());

					return file.transferTo(new File(this.path + p.getFoto())).then(this.productoService.save(p));
				})).flatMap(p -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(p)));
	}
}
