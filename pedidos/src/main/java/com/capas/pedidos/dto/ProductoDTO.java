package com.capas.pedidos.dto;

import java.math.BigDecimal;

public class ProductoDTO {
    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }
}
