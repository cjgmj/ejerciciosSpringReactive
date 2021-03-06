package com.cjgmj.webflux.apirest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.cjgmj.webflux.apirest.models.documents.Categoria;
import com.cjgmj.webflux.apirest.models.documents.Producto;
import com.cjgmj.webflux.apirest.models.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Ejecutar en modo MOCK
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebfluxApirestApplicationTests {

	@Value("${config.base.endpoint}")
	private String url;

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService productoService;

	@Disabled("La ejecución del test de crear sobrepasa el tamaño")
	@Test
	public void listarTest() {
		this.client
				// Verbo de la petición
				.get()
				// URI de la petición
				.uri(this.url)
				// MediaType que se consumirá
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isOk()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBodyList(Producto.class)
				// Comprueba que el body tenga la cantidad de elementos indicada
				.hasSize(9);
	}

	@Test
	public void listarTestConsumeWith() {
		this.client
				// Verbo de la petición
				.get()
				// URI de la petición
				.uri(this.url)
				// MediaType que se consumirá
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isOk()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBodyList(Producto.class)
				// Podemos hacer pruebas con la respuesta con Assertions
				.consumeWith(response -> {
					final List<Producto> productos = response.getResponseBody();

					productos.forEach(p -> {
						System.out.println(p.getNombre());
					});

					Assertions.assertThat(productos.size() > 0).isTrue();
				});
	}

	@Test
	public void verTest() {
		final Mono<Producto> producto = this.productoService.findByNombre("TV Panasonic Pantalla LCD");

		this.client
				// Verbo de la petición
				.get()
				// URI de la petición, pasamos la variable con Collections, para pruebas
				// unitarias tiene que ser
				// síncrono por ello usamos block
				.uri(this.url + "/{id}", Collections.singletonMap("id", producto.block().getId()))
				// MediaType que se consumirá
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isOk()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBody()
				// Comprueba los elementos del json de la respuesta
				.jsonPath("$.id").isNotEmpty().jsonPath("$.nombre").isEqualTo("TV Panasonic Pantalla LCD");
	}

	@Test
	public void ver2Test() {
		final Mono<Producto> producto = this.productoService.findByNombre("TV Panasonic Pantalla LCD");

		this.client
				// Verbo de la petición
				.get()
				// URI de la petición, pasamos la variable con Collections, para pruebas
				// unitarias tiene que ser
				// síncrono por ello usamos block
				.uri(this.url + "/{id}", Collections.singletonMap("id", producto.block().getId()))
				// MediaType que se consumirá
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isOk()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBody(Producto.class)
				// Compara con otro objeto producto con isEqualTo
				.isEqualTo(producto.block());
	}

	@Test
	public void ver3Test() {
		final Mono<Producto> producto = this.productoService.findByNombre("TV Panasonic Pantalla LCD");

		this.client
				// Verbo de la petición
				.get()
				// URI de la petición, pasamos la variable con Collections, para pruebas
				// unitarias tiene que ser
				// síncrono por ello usamos block
				.uri(this.url + "/{id}", Collections.singletonMap("id", producto.block().getId()))
				// MediaType que se consumirá
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isOk()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBody(Producto.class)
//				// Compara con Assertions
				.consumeWith(response -> {
					final Producto p = response.getResponseBody();

					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getNombre()).isEqualTo("TV Panasonic Pantalla LCD");
				});
	}

	@Test
	public void crearTest() {
		final Categoria categoria = this.productoService.findCategoriaByNombre("Muebles").block();

		final Producto producto = new Producto("Mesa comedor", 100.00, categoria);

		this.client
				// Verbo de la petición
				.post()
				// URI de la petición
				.uri(this.url)
				// MediaType del request
				.contentType(MediaType.APPLICATION_JSON)
				// MediaType del response
				.accept(MediaType.APPLICATION_JSON)
				// Body que se envia como JSON, se puede crear como Mono.just() indicando el
				// tipo del objeto o con BodyInserters.fromValue()
				.body(Mono.just(producto), Producto.class)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isCreated()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBody()
				// Comprobación con jsonPath
				// Validar cuando no apunte al controlador
//				.jsonPath("$.id").isNotEmpty().jsonPath("$.nombre").isEqualTo("Mesa comedor")
//				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
				// Validar cuando apunte al controlador
				.jsonPath("$.producto.id").isNotEmpty().jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
				.jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}

	@Test
	public void crear2Test() {
		final Categoria categoria = this.productoService.findCategoriaByNombre("Muebles").block();

		final Producto producto = new Producto("Mesa comedor", 100.00, categoria);

		this.client
				// Verbo de la petición
				.post()
				// URI de la petición
				.uri(this.url)
				// MediaType del request
				.contentType(MediaType.APPLICATION_JSON)
				// MediaType del response
				.accept(MediaType.APPLICATION_JSON)
				// Body que se envia como JSON, se puede crear como Mono.just() indicando el
				// tipo del objeto o con BodyInserters.fromValue()
				.body(Mono.just(producto), Producto.class)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isCreated()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				// Validar cuando no apunte al controlador
//				.expectBody(Producto.class)
				// Comprobación con Assertions
//				.consumeWith(response -> {
//					final Producto p = response.getResponseBody();
//
//					Assertions.assertThat(p.getId()).isNotEmpty();
//					Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
//					Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
//				});
				// Validar cuando apunte al controlador
				.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {
				})
				// Comprobación con Assertions
				.consumeWith(response -> {
					final Object o = response.getResponseBody().get("producto");

					final Producto p = new ObjectMapper().convertValue(o, Producto.class);

					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
					Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
				});
	}

	@Test
	public void editarTest() {
		final Producto producto = this.productoService.findByNombre("Sony Notebook Z110").block();
		final Categoria categoria = this.productoService.findCategoriaByNombre("Electrónico").block();

		final Producto productoEditado = new Producto("Asus Notebook", 700.00, categoria);

		this.client
				// Verbo de la petición
				.put()
				// URI de la petición, pasamos la variable con Collections
				.uri(this.url + "/{id}", Collections.singletonMap("id", producto.getId()))
				// MediaType del request
				.contentType(MediaType.APPLICATION_JSON)
				// MediaType del response
				.accept(MediaType.APPLICATION_JSON)
				// Body que se envia como JSON, se puede crear como Mono.just() indicando el
				// tipo del objeto o con BodyInserters.fromValue()
				.body(BodyInserters.fromValue(productoEditado))
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isOk()
				// Cabecera de la respuesta que se espera
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				// Contenido de la respuesta que se espera
				.expectBody()
				// Comprobación con jsonPath
				.jsonPath("$.id").isNotEmpty().jsonPath("$.nombre").isEqualTo("Asus Notebook")
				.jsonPath("$.categoria.nombre").isEqualTo("Electrónico");
	}

	@Test
	public void eliminarTest() {
		final Producto producto = this.productoService.findByNombre("Mica Cómoda 5 Cajones").block();

		this.client
				// Verbo de la petición
				.delete()
				// URI de la petición, pasamos la variable con Collections
				.uri(this.url + "/{id}", Collections.singletonMap("id", producto.getId()))
				// MediaType del response
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isNoContent()
				// Contenido de la respuesta que se espera
				.expectBody().isEmpty();

		this.client
				// Verbo de la petición
				.get()
				// URI de la petición, pasamos la variable con Collections
				.uri(this.url + "/{id}", Collections.singletonMap("id", producto.getId()))
				// MediaType del response
				.accept(MediaType.APPLICATION_JSON)
				// Envia el request al endpoint
				.exchange()
				// Estado de la respuesta que se espera
				.expectStatus().isNotFound()
				// Contenido de la respuesta que se espera
				.expectBody().isEmpty();
	}

}
