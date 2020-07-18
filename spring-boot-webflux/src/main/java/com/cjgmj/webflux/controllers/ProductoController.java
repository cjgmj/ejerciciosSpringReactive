package com.cjgmj.webflux.controllers;

import java.io.File;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.cjgmj.webflux.models.documents.Categoria;
import com.cjgmj.webflux.models.documents.Producto;
import com.cjgmj.webflux.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {

	private static final Logger LOG = LoggerFactory.getLogger(ProductoController.class);

	@Value("${config.uploads.path}")
	private String path;

	@Autowired
	private ProductoService productoService;

	@ModelAttribute("categorias")
	public Flux<Categoria> categorias() {
		return this.productoService.findAllCategoria();
	}

	@GetMapping("/ver/{id}")
	public Mono<String> ver(Model model, @PathVariable String id) {
		return this.productoService.findById(id).doOnNext(p -> {
			model.addAttribute("producto", p);
			model.addAttribute("titulo", "Detalle producto");
		}).switchIfEmpty(Mono.just(new Producto())).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}

			return Mono.just(p);
		}).then(Mono.just("ver")).onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
	}

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
		model.addAttribute("boton", "Crear");

		return Mono.just("form");
	}

	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable String id, Model model) {
		final Mono<Producto> productoMono = this.productoService.findById(id)
				.doOnNext(p -> LOG.info("Producto: " + p.getNombre())).defaultIfEmpty(new Producto());

		model.addAttribute("titulo", "Editar producto");
		model.addAttribute("producto", productoMono);
		model.addAttribute("boton", "Editar");

		return Mono.just("form");
	}

	// Versión más reactiva para editar
	// De esta forma no funciona el SessionAttributes y se tendría que poner el
	// campo hidden en el formulario
	@GetMapping("/form-v2/{id}")
	public Mono<String> editarV2(@PathVariable String id, Model model) {
		return this.productoService.findById(id).doOnNext(p -> {
			LOG.info("Producto: " + p.getNombre());
			model.addAttribute("titulo", "Editar producto");
			model.addAttribute("producto", p);
			model.addAttribute("boton", "Editar");
		}).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}

			return Mono.just(p);
		}).thenReturn("form").onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
	}

	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart FilePart file,
			SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Errores en el formulario");
			model.addAttribute("boton", "Guardar");

			return Mono.just("form");
		} else {
			status.setComplete();

			final Mono<Categoria> categoria = this.productoService.findCategoriaById(producto.getCategoria().getId());

			return categoria.flatMap(c -> {
				if (producto.getCreateAt() == null) {
					producto.setCreateAt(new Date());
				}

				if (!file.filename().isEmpty()) {
					producto.setFoto(UUID.randomUUID().toString() + "-"
							+ file.filename().replace(" ", "").replace(":", "").replace("\\", ""));
				}

				producto.setCategoria(c);
				return this.productoService.save(producto);
			}).doOnNext(p -> {
				LOG.info("Categoria asignada " + p.getCategoria().getNombre() + " id: " + p.getCategoria().getId());
				LOG.info("Producto guardado " + p.getNombre() + " id: " + p.getId());
			}).flatMap(p -> {
				if (!file.filename().isEmpty()) {
					return file.transferTo(new File(this.path + p.getFoto()));
				}

				return Mono.empty();
			}).thenReturn("redirect:/listar?success=insertado+correctamente");
		}
	}

	@GetMapping("/eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id) {
		return this.productoService.findById(id).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto a eliminar!"));
			}

			return Mono.just(p);
		}).flatMap(p -> {
			LOG.info("Eliminando producto con id " + p.getId() + " y nombre " + p.getNombre());
			return this.productoService.delete(p);
		}).thenReturn("redirect:/listar?success=producto+eliminado+con+éxito")
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
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
