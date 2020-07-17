package com.cjgmj.webflux.controllers;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.cjgmj.webflux.models.documents.Producto;
import com.cjgmj.webflux.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductoController {

	private static final Logger LOG = LoggerFactory.getLogger(ProductoController.class);

	@Autowired
	private ProductoService productoService;

	@GetMapping({ "/", "/listar" })
	public Mono<String> listar(Model model) {
		final Flux<Producto> productos = this.productoService.findAllConNombreUpperCase();

		productos.subscribe(producto -> LOG.info(producto.getNombre()));

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		return Mono.just("listar");
	}

	@GetMapping("/form")
	public Mono<String> crear(Model model) {
		model.addAttribute("producto", new Producto());
		model.addAttribute("titulo", "Formulario de producto");

		return Mono.just("form");
	}

	@PostMapping("/form")
	public Mono<String> guardar(Producto producto) {
		return this.productoService.save(producto).doOnNext(p -> {
			LOG.info("Producto guardado" + p.getNombre() + " id: " + p.getId());
		}).thenReturn("redirect:/listar");
	}

	@GetMapping("/listar-datadriver")
	public String listarDataDriver(Model model) {
		final Flux<Producto> productos = this.productoService.findAllConNombreUpperCase()
				.delayElements(Duration.ofSeconds(1));

		productos.subscribe(producto -> LOG.info(producto.getNombre()));

		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
		model.addAttribute("titulo", "Listado de productos");

		return "listar";
	}

	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		final Flux<Producto> productos = this.productoService.findAllConNombreUpperCaseRepeat();

		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
		model.addAttribute("titulo", "Listado de productos");

		return "listar";
	}

	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {
		final Flux<Producto> productos = this.productoService.findAllConNombreUpperCaseRepeat();

		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
		model.addAttribute("titulo", "Listado de productos");

		return "listar-chunked";
	}
}
