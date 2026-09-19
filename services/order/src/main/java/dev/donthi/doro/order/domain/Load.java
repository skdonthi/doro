package dev.donthi.doro.order.domain;

public record Load(Double loadingMetres, Integer pallets, Double weightKg) {
    boolean isEmpty() {
        return loadingMetres == null && pallets == null && weightKg == null;
    }
}
