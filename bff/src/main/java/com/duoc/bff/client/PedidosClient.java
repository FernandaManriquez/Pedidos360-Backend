package com.duoc.bff.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
        name = "servicioPedidos",
        url = "${services.pedidos.url}"
)
public interface PedidosClient {

    @GetMapping("/api/pedidos")
    ResponseEntity<Object> listarPedidos(
            @RequestHeader("Authorization") String authorization);

    @GetMapping("/api/pedidos/{id}")
    ResponseEntity<Object> buscarPedidoPorId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id);

    @GetMapping("/api/pedidos/usuario/{usuario}")
    ResponseEntity<Object> buscarPedidosPorUsuario(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("usuario") String usuario);

    @PostMapping("/api/pedidos")
    ResponseEntity<Object> crearPedido(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> pedido);

    @PutMapping("/api/pedidos/{id}/estado")
    ResponseEntity<Object> cambiarEstado(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> datos);

    @DeleteMapping("/api/pedidos/{id}")
    ResponseEntity<Void> eliminarPedido(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id);
}
