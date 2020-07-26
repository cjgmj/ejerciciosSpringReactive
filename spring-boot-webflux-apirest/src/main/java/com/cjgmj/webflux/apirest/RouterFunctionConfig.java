package com.cjgmj.webflux.apirest;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.cjgmj.webflux.apirest.handler.ProductoHandler;

@Configuration
public class RouterFunctionConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler) {
//		return route(GET("/api/v2/productos").or(GET("/api/v2/productos/lista")), request -> ServerResponse.ok()
//				.contentType(MediaType.APPLICATION_JSON).body(this.productoService.findAll(), Producto.class));
		return route(GET("/api/v2/productos").or(GET("/api/v2/productos/lista")), productoHandler::listar);
	}

}
