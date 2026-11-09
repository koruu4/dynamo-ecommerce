package com.capas.pedidos.services;

import com.capas.pedidos.dto.ProductoDTO;
import com.capas.pedidos.models.DetallePedido;
import com.capas.pedidos.models.Pedido;
import com.capas.pedidos.repositories.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PedidoServiceImpl implements PedidoService {
    private final PedidoRepository pedidoRepository;
    private final RestTemplate restTemplate;

    @Value("${productos.service.url}")
    private String productosServiceUrl;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, RestTemplate restTemplate){
        this.pedidoRepository = pedidoRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<Pedido> getPedidoPorId(Integer id){
        if(id == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El id del pedido no puede ser nulo");
        }
        return pedidoRepository.findById(id);
    }

    @Override
    public List<Pedido> getTodosLosPedidos(){
        return pedidoRepository.findAll();
    }

    @Override
    @Transactional
    public Pedido registrarPedido(Pedido pedido){
        pedido.setCreacion(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");
        BigDecimal totalCalculado = BigDecimal.ZERO;

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido debe contener al menos un detalle de producto");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getProductoId() == null || detalle.getCantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de producto o cantidad inválidos en el detalle");
            }

            String urlProducto = productosServiceUrl + "/" + detalle.getProductoId();
            ProductoDTO producto;

            try{
                var response = restTemplate.getForObject(urlProducto, java.util.Map.class);
                if(response == null || !response.containsKey("producto")){
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto con id " + detalle.getProductoId() + " no encontrado en el sistema");
                }

                java.util.Map<String, Object> prodMap = (java.util.Map<String, Object>) response.get("producto");

                BigDecimal precioProd = new BigDecimal(prodMap.get("precio").toString());
                Integer stockProd = Integer.parseInt(prodMap.get("stock").toString());

                if(stockProd < detalle.getCantidad()){
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto id: " + detalle.getProductoId() + ". Stock disponible: " + stockProd);
                }

                detalle.setPedido(pedido);
                detalle.setSubtotal(precioProd.multiply(new BigDecimal(detalle.getCantidad())));
                totalCalculado = totalCalculado.add(detalle.getSubtotal());

            } catch (ResponseStatusException rse) {
                throw rse; // Propagamos directamente nuestra excepción HTTP controlada
            } catch(Exception e){
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "E rror al comunicarse con el servicio de productos: " + e.getMessage());
            }

            // Llamar a endpoint patch para descontar stock
            String urlDescontarStock = productosServiceUrl + "/" + detalle.getProductoId() + "/stock";
            try{
                java.util.Map<String, Integer> bodyStock = new java.util.HashMap<>();
                bodyStock.put("cantidad", detalle.getCantidad());
                restTemplate.patchForObject(urlDescontarStock, bodyStock, Void.class);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo descontar el stock en el servicio de productos para el id: " + detalle.getProductoId());
            }
        }

        pedido.setTotal(totalCalculado);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cancelarPedido(Integer id){
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado con id: " + id));

        if("CANCELADO".equalsIgnoreCase(pedido.getEstado())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ya se encuentra cancelado");
        }

        if("CONFIRMADO".equalsIgnoreCase(pedido.getEstado())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pedido ha sido confirmado previamente, ya no puede ser cancelado");
        }

        for(var detalle: pedido.getDetalles()){
            String urlSumarStock = productosServiceUrl + "/" + detalle.getProductoId() + "/stock/suma";
            try {
                java.util.Map<String, Integer> bodyStock = new java.util.HashMap<>();
                bodyStock.put("cantidad", detalle.getCantidad());

                restTemplate.patchForObject(urlSumarStock, bodyStock, Void.class);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error al devolver el stock al servicio de productos: " + e.getMessage());
            }
        }

        pedido.setEstado("CANCELADO");
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido completarPedido(Integer id){
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado con id: " + id));

        if("CANCELADO".equalsIgnoreCase(pedido.getEstado())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido previamente cancelado, no puede validarse");
        }

        if("CONFIRMADO".equalsIgnoreCase(pedido.getEstado())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido ya se encuentra confirmado");
        }

        pedido.setEstado("CONFIRMADO");
        return pedidoRepository.save(pedido);
    }
}