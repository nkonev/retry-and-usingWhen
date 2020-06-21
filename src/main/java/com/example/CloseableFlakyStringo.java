package com.example;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseableFlakyStringo implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CloseableFlakyStringo.class);

  final String string;

  public CloseableFlakyStringo() {
    string = "I am stringo at " + Instant.now();
    LOGGER.info("Instantiated: " + string);
  }

  static final AtomicInteger counter = new AtomicInteger();

  @Override
  public void close() {
    LOGGER.info("I am closed: " + string);
  }

  public String getString() {
    int prev = counter.getAndIncrement();
    if (prev == 0) { // Replace to `if (prev < 1000)` to check exit when all attempts are exhausted
      throw new RuntimeException("I am not able to get value at first invocation!");
    }
    return string;
  }
}
