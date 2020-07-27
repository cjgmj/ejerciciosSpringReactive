package com.cjgmj.webflux.apirest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.cjgmj.webflux.apirest.models.documents.Producto;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void listarTest() {
		this.client
				// Verbo de la petición
				.get()
				// URI de la petición
				.uri("/api/v2/productos")
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

}
