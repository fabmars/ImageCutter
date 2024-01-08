package org.mars.cutter.domain;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public record HVHoughs(
    List<Hough> horizontals,
    List<Hough> verticals
) {

  public Stream<Hough> stream() {
    return Stream.concat(horizontals.stream(), verticals.stream());
  }

  public void forEach(Consumer<? super Hough> action) {
    stream().forEach(action);
  }
}
