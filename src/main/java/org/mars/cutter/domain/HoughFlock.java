package org.mars.cutter.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class HoughFlock implements Iterable<Hough> {

  private final List<Hough> houghs = new ArrayList<>();

  public int size() {
    return houghs.size();
  }

  public boolean isEmpty() {
    return houghs.isEmpty();
  }

  public Iterator<Hough> iterator() {
    return houghs.iterator();
  }

  public Stream<Hough> stream() {
    return houghs.stream();
  }

  @Override
  public void forEach(Consumer<? super Hough> action) {
    houghs.forEach(action);
  }

  public boolean add(Hough hough) {
    return houghs.add(hough);
  }

  public Hough get(int index) {
    return houghs.get(index);
  }

  public Hough getFirst() {
    return houghs.getFirst();
  }

  public Hough getLast() {
    return houghs.getLast();
  }
}
