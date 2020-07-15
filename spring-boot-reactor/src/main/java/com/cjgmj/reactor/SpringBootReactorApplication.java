package com.cjgmj.reactor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cjgmj.reactor.models.Usuario;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		final List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Joe Simons");
		usuariosList.add("John Doe");
		usuariosList.add("Linus Torvalds");
		usuariosList.add("Jane Doe");
		usuariosList.add("Rasmus Lerdorf");
		usuariosList.add("Erich Gamma");

//		final Flux<String> nombres = Flux.just("Joe Simons", "John Doe", "Linus Torvalds", "Jane Doe", "Rasmus Lerdorf",
//				"Erich Gamma");
		final Flux<String> nombres = Flux.fromIterable(usuariosList);

		// Al ser inmutables los observables los operadores siguientes no afectarán al
		// observable original, ya que esto crea un nuevo observable, si queremos ver la
		// ejecución de esta sección deberemos asignarla a un nuevo observable y
		// suscribirnos al mismo
		final Flux<Usuario> usuarios = nombres
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> "Doe".equalsIgnoreCase(usuario.getApellido())).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("El nombre no puede estar vacío");
					}

					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				}).map(usuario -> {
					final String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(usuario -> LOG.info(usuario.toString()), error -> LOG.error(error.getMessage()),
				() -> LOG.info("Ha finalizado la ejecución del observable con éxito"));
	}

}
