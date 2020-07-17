package com.cjgmj.webflux.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.cjgmj.webflux.models.dao.ProductoDao;
import com.cjgmj.webflux.models.documents.Producto;

import reactor.core.publisher.Flux;

@Controller
public class ProductoController {

	private static final Logger LOG = LoggerFactory.getLogger(ProductoController.class);

	@Autowired
	private ProductoDao productoDao;

	@GetMapping({ "/", "/listar" })
	public String listar(Model model) {
		final Flux<Producto> productos = this.productoDao.findAll().map(producto -> {
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});

		productos.subscribe(producto -> LOG.info(producto.getNombre()));

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		return "listar";
	}
}
