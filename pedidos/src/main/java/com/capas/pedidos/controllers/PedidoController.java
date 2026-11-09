package com.capas.pedidos.controllers;

import com.capas.pedidos.models.Pedido;
import com.capas.pedidos.services.PedidoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/{id}")
    public Optional<Pedido> getPedidoPorId(@PathVariable Integer id) {
        return pedidoService.getPedidoPorId(id);
    }

    @GetMapping
    public List<Pedido> getTodosLosPedidos() {
        return pedidoService.getTodosLosPedidos();
    }

    @PostMapping
    public Pedido registrarPedido(@RequestBody Pedido pedido) {
        return pedidoService.registrarPedido(pedido);
    }

    @PatchMapping("/{id}/cancelar")
    public Pedido cancelarPedido(@PathVariable Integer id) {
        return pedidoService.cancelarPedido(id);
    }

    @PatchMapping("/{id}/completar")
    public Pedido completarPedido(@PathVariable Integer id) {
        return pedidoService.completarPedido(id);
    }
}
