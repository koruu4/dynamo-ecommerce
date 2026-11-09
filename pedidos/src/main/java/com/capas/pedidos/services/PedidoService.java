package com.capas.pedidos.services;

import com.capas.pedidos.models.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoService {
    Optional<Pedido> getPedidoPorId(Integer id);
    List<Pedido> getTodosLosPedidos();
    Pedido registrarPedido(Pedido pedido);
    Pedido cancelarPedido(Integer id);
    Pedido completarPedido(Integer id);
}
