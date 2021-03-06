package com.cjgmj.webflux.apirest.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.cjgmj.webflux.apirest.models.documents.Producto;
import com.cjgmj.webflux.apirest.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	@Autowired
	private ProductoService productoService;

	@Value("${config.uploads.path}")
	private String path;

	@GetMapping("/lista/flux")
	public Flux<Producto> listaFlux() {
		return this.productoService.findAll();
	}

	@GetMapping
	public Mono<ResponseEntity<Flux<Producto>>> lista() {
		// Se podría hacer también con ResponseEntity.ok(this.productoService.findAll())
		return Mono
				.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(this.productoService.findAll()));
	}

	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> ver(@PathVariable String id) {
		return this.productoService.findById(id).map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	// Obtenemos un tipo reactivo para poder capturar los errores en el proceso de
	// validación y poder devolver los errores con el onErrorResume en el
	// ResponseEntity.badRequest()
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
		final Map<String, Object> respuesta = new HashMap<>();

		return monoProducto.flatMap(producto -> {
			if (producto.getCreateAt() == null) {
				producto.setCreateAt(new Date());
			}

			return this.productoService.save(producto).map(p -> {
				respuesta.put("producto", p);
				respuesta.put("mensaje", "Producto creado con éxito");
				respuesta.put("timestamp", new Date());

				return ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(respuesta);
			});
		}).onErrorResume(t -> Mono.just(t).cast(WebExchangeBindException.class)
				.flatMap(e -> Mono.just(e.getFieldErrors())).flatMapMany(Flux::fromIterable)
				.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
				.collectList().flatMap(list -> {
					respuesta.put("errors", list);
					respuesta.put("timestamp", new Date());
					respuesta.put("status", HttpStatus.BAD_REQUEST.value());

					return Mono.just(ResponseEntity.badRequest().body(respuesta));
				}));
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Producto>> editar(@PathVariable String id, @RequestBody Producto producto) {
		return this.productoService.findById(id).flatMap(p -> {
			p.setNombre(producto.getNombre());
			p.setPrecio(producto.getPrecio());
			p.setCategoria(producto.getCategoria());

			return this.productoService.save(p);
		}).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {
		return this.productoService.findById(id).flatMap(
				// Para usar ResponseEntity.notFound().build() o
				// ResponseEntity.notContent().build() cambiamos Void a Object
				p -> this.productoService.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
		return this.productoService.findById(id).flatMap(p -> {
			p.setFoto(UUID.randomUUID().toString() + "-"
					+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));

			return file.transferTo(new File(this.path + p.getFoto())).then(this.productoService.save(p));
		}).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/v2")
	public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file) {
		if (producto.getCreateAt() == null) {
			producto.setCreateAt(new Date());
		}

		producto.setFoto(UUID.randomUUID().toString() + "-"
				+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));

		return file.transferTo(new File(this.path + producto.getFoto())).then(this.productoService.save(producto))
				.map(p -> ResponseEntity.created(URI.create("/api/productos/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(p));
	}

}
