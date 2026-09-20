package com.duoc.bff.controller;

import com.duoc.bff.client.PedidosClient;
import com.duoc.bff.client.ProductosClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BffController {

    private final PedidosClient pedidosClient;
    private final ProductosClient productosClient;

    public BffController(PedidosClient pedidosClient, ProductosClient productosClient) {
        this.pedidosClient = pedidosClient;
        this.productosClient = productosClient;
    }

    @GetMapping("/pedidos")
    public ResponseEntity<Object> listarPedidos(
            @RequestHeader("Authorization") String authorization) {
        return pedidosClient.listarPedidos(authorization);
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<Object> buscarPedidoPorId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        return pedidosClient.buscarPedidoPorId(authorization, id);
    }

    @GetMapping("/pedidos/usuario/{usuario}")
    public ResponseEntity<Object> buscarPedidosPorUsuario(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String usuario) {
        return pedidosClient.buscarPedidosPorUsuario(authorization, usuario);
    }

    @PostMapping("/pedidos")
    public ResponseEntity<Object> crearPedido(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> pedido) {
        return pedidosClient.crearPedido(authorization, pedido);
    }

    @PutMapping("/pedidos/{id}/estado")
    public ResponseEntity<Object> cambiarEstado(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, String> datos) {
        return pedidosClient.cambiarEstado(authorization, id, datos);
    }

    @DeleteMapping("/pedidos/{id}")
    public ResponseEntity<Void> eliminarPedido(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        return pedidosClient.eliminarPedido(authorization, id);
    }

    @GetMapping("/productos")
    public ResponseEntity<Object> listarProductos(
            @RequestHeader("Authorization") String authorization) {
        return productosClient.listarProductos(authorization);
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Object> buscarProductoPorId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        return productosClient.buscarProductoPorId(authorization, id);
    }

    @PostMapping("/productos")
    public ResponseEntity<Object> crearProducto(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> producto) {
        return productosClient.crearProducto(authorization, producto);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Object> actualizarProducto(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @RequestBody Map<String, Object> producto) {
        return productosClient.actualizarProducto(authorization, id, producto);
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        return productosClient.eliminarProducto(authorization, id);
    }
}
