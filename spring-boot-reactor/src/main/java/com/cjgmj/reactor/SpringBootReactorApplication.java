package com.cjgmj.reactor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cjgmj.reactor.models.Comentario;
import com.cjgmj.reactor.models.Usuario;
import com.cjgmj.reactor.models.UsuarioComentario;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		this.ejemploContrapresion();
	}

	public void ejemploContrapresion() {
		Flux.range(1, 10).log().limitRate(2).subscribe(
//			new Subscriber<Integer>() {
//	
//				private Subscription s;
//	
//				private Integer limite = 2;
//	
//				private Integer consumido = 0;
//	
//				@Override
//				public void onSubscribe(Subscription s) {
//					this.s = s;
//					s.request(this.limite);
//				}
//	
//				@Override
//				public void onNext(Integer t) {
//					LOG.info(t.toString());
//					this.consumido++;
//	
//					if (this.consumido == this.limite) {
//						this.consumido = 0;
//						this.s.request(this.limite);
//					}
//				}
//	
//				@Override
//				public void onError(Throwable t) {
//	
//				}
//	
//				@Override
//				public void onComplete() {
//	
//				}
//			}
		);
	}

	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++this.contador);

					if (this.contador == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (this.contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Se ha detenido el Flux"));
					}
				}
			}, 1000, 1000);
		}).doOnNext(next -> LOG.info(next.toString())).doOnError(error -> LOG.error(error.getMessage()))
				.doOnComplete(() -> LOG.info("Terminado")).subscribe();
	}

	public void ejemploIntervalInfinito() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown).flatMap(i -> {
			if (i >= 5) {
				return Flux.error(new InterruptedException("Llegó a 5"));
			}
			return Flux.just(i);
		}).map(i -> "Hola " + i)
				// Si lanza error intenta ejecutar de nuevo el número de veces indicado, en caso
				// de que las siguientes den error
				.retry(2).subscribe(s -> LOG.info(s), e -> LOG.error(e.getMessage()));

		latch.await();
	}

	public void ejemploDelayElements() throws InterruptedException {
		final Flux<Integer> rango = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> LOG.info(i.toString()));

//		rango.blockLast();
		rango.subscribe();

		Thread.sleep(13000);

	}

	public void ejemploInterval() {
		final Flux<Integer> rango = Flux.range(1, 12);
		final Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(delay, (ra, de) -> ra).doOnNext(i -> LOG.info(i.toString()))
				// Se suscribe al flujo bloqueando la ejecución hasta que se completa el
				// observable
				.blockLast();

	}

	public void ejemploZipWithRangos() {
		Flux.just(1, 2, 3, 4).map(i -> i * 2)
				.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(LOG::info);
	}

	public void ejemploUsuarioComentariosZipWithForma2() {
		final Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		final Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(() -> {
			final Comentario comentario = new Comentario();

			comentario.addComentario("Hello world");
			comentario.addComentario("Hola mundo");
			comentario.addComentario("Lorem ipsum");

			return comentario;
		});

		final Mono<UsuarioComentario> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono).map(tuple -> {
			final Usuario u = tuple.getT1();
			final Comentario c = tuple.getT2();

			return new UsuarioComentario(u, c);
		});

		usuarioConComentarios.subscribe(uc -> LOG.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith() {
		final Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		final Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(() -> {
			final Comentario comentario = new Comentario();

			comentario.addComentario("Hello world");
			comentario.addComentario("Hola mundo");
			comentario.addComentario("Lorem ipsum");

			return comentario;
		});

		final Mono<UsuarioComentario> usuarioConComentarios = usuarioMono.zipWith(comentariosUsuarioMono,
				UsuarioComentario::new);

		usuarioConComentarios.subscribe(uc -> LOG.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap() {
		final Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		final Mono<Comentario> comentariosUsuarioMono = Mono.fromCallable(() -> {
			final Comentario comentario = new Comentario();

			comentario.addComentario("Hello world");
			comentario.addComentario("Hola mundo");
			comentario.addComentario("Lorem ipsum");

			return comentario;
		});

		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentario(u, c)))
				.subscribe(uc -> LOG.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {
		final List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Joe", "Simons"));
		usuariosList.add(new Usuario("John", "Doe"));
		usuariosList.add(new Usuario("Linus", "Torvalds"));
		usuariosList.add(new Usuario("Jane", "Doe"));
		usuariosList.add(new Usuario("Rasmus", "Lerdorf"));
		usuariosList.add(new Usuario("Erich", "Gamma"));

		Flux.fromIterable(usuariosList).collectList().subscribe(lista -> {
			lista.forEach(item -> LOG.info(item.toString()));
		});
	}

	public void ejemploToString() throws Exception {
		final List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Joe", "Simons"));
		usuariosList.add(new Usuario("John", "Doe"));
		usuariosList.add(new Usuario("Linus", "Torvalds"));
		usuariosList.add(new Usuario("Jane", "Doe"));
		usuariosList.add(new Usuario("Rasmus", "Lerdorf"));
		usuariosList.add(new Usuario("Erich", "Gamma"));

		Flux.fromIterable(usuariosList).map(
				usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("Doe".toUpperCase())) {
						return Mono.just(nombre);
					}
					return Mono.empty();
				}).map(String::toLowerCase).subscribe(LOG::info);
	}

	public void ejemploFlatMap() throws Exception {
		final List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Joe Simons");
		usuariosList.add("John Doe");
		usuariosList.add("Linus Torvalds");
		usuariosList.add("Jane Doe");
		usuariosList.add("Rasmus Lerdorf");
		usuariosList.add("Erich Gamma");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if ("Doe".equalsIgnoreCase(usuario.getApellido())) {
						return Mono.just(usuario);
					}
					return Mono.empty();
				}).map(usuario -> {
					final String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).subscribe(u -> LOG.info(u.toString()));
	}

	public void ejemploIterable() throws Exception {
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
