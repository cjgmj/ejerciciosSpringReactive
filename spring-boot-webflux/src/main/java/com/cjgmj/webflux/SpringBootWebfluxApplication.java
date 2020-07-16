package com.cjgmj.webflux;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.cjgmj.webflux.models.dao.ProductoDao;
import com.cjgmj.webflux.models.documents.Producto;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);

	@Autowired
	private ProductoDao productoDao;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		this.mongoTemplate.dropCollection("productos").subscribe();

		Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89), new Producto("Sony Cámara HD Digital", 177.89),
				new Producto("Apple iPod shuffle", 46.89), new Producto("Sony Notebook Z110", 846.89),
				new Producto("Hewlett Packard Multifuncional F2280", 200.89),
				new Producto("Bianchi Bicicleta Aro 26", 70.89), new Producto("HP Notebook Omen 17", 2500.89),
				new Producto("Mica Cómoda 5 Cajones", 150.89), new Producto("TV Sony Bravia OLED 4K Ultra HD", 2255.89))
				.flatMap(producto -> {
					producto.setCreateAt(new Date());
					return this.productoDao.save(producto);
				}).subscribe(producto -> LOG.info("Insert: " + producto.getId() + " " + producto.getNombre()));
	}

}
