package com.cjgmj.webflux.apirest;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.cjgmj.webflux.apirest.handler.ProductoHandler;

@Configuration
public class RouterFunctionConfig {

	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler productoHandler) {
//		return route(GET("/api/v2/productos").or(GET("/api/v2/productos/lista")), request -> ServerResponse.ok()
//				.contentType(MediaType.APPLICATION_JSON).body(this.productoService.findAll(), Producto.class));
		return route(GET("/api/v2/productos").or(GET("/api/v2/productos/lista")), productoHandler::listar)
				.andRoute(GET("/api/v2/productos/{id}"), productoHandler::ver)
				// Se puede especificar el contentType añadiendo and a la petición
				.andRoute(POST("/api/v2/productos").and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),
						productoHandler::crear)
				.andRoute(PUT("/api/v2/productos/{id}").and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),
						productoHandler::editar)
				.andRoute(DELETE("/api/v2/productos/{id}"), productoHandler::eliminar);
	}

}
