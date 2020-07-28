package com.cjgmj.webflux.apirest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.cjgmj.webflux.apirest.models.documents.Categoria;
import com.cjgmj.webflux.apirest.models.documents.Producto;
import com.cjgmj.webflux.apirest.models.services.ProductoService;

import reactor.core.publisher.Flux;

@EnableEurekaClient
@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	@Autowired
	private ProductoService productoService;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		this.mongoTemplate.dropCollection("productos").subscribe();
		this.mongoTemplate.dropCollection("categorias").subscribe();

		final Categoria electronico = new Categoria("Electrónico");
		final Categoria deporte = new Categoria("Deporte");
		final Categoria computacion = new Categoria("Computación");
		final Categoria muebles = new Categoria("Muebles");

		Flux.just(electronico, deporte, computacion, muebles).flatMap(this.productoService::saveCategoria)
				.doOnNext(c -> LOG.info("Categoria " + c.getNombre() + " creada con id " + c.getId()))
				.thenMany(Flux
						.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
								new Producto("Sony Cámara HD Digital", 177.89, electronico),
								new Producto("Apple iPod shuffle", 46.89, electronico),
								new Producto("Sony Notebook Z110", 846.89, computacion),
								new Producto("Hewlett Packard Multifuncional F2280", 200.89, computacion),
								new Producto("Bianchi Bicicleta Aro 26", 70.89, deporte),
								new Producto("HP Notebook Omen 17", 2500.89, computacion),
								new Producto("Mica Cómoda 5 Cajones", 150.89, muebles),
								new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89, electronico))
						.flatMap(producto -> {
							producto.setCreateAt(new Date());
							return this.productoService.save(producto);
						}))
				.subscribe(producto -> LOG.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}

}
