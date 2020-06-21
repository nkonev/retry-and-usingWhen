package com.example;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class UsingWhenThenRetry {

  private static final Logger LOGGER = LoggerFactory.getLogger(UsingWhenThenRetry.class);

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newCachedThreadPool();

    Supplier<Mono<CloseableFlakyStringo>> stringoSupplier = () -> Mono.just(new CloseableFlakyStringo());

    Flux<String> stringFlux = Flux.usingWhen(Mono.defer(stringoSupplier),
        connection -> Mono.just(connection.getString().toUpperCase()),
        closeableStringo -> Mono.defer(() -> {
          closeableStringo.close();
          return Mono.empty();
        }))
        .retryWhen(reactor.util.retry.Retry.fixedDelay(10, Duration.of(2, ChronoUnit.SECONDS))
            .doBeforeRetry(retrySignal -> {
              LOGGER.warn("Retrying to stringo due " + retrySignal.failure().getClass() + " " + retrySignal.failure().getMessage());
            }))
        .subscribeOn(Schedulers.fromExecutor(executorService));

    stringFlux.doOnTerminate(() -> {
      executorService.shutdown();
    }).subscribe(s -> {
      LOGGER.info("In subscription - value successful obtained: " + s);
    });
  }
}
