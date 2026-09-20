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
        name = "servicioProductos",
        url = "${services.productos.url}"
)
public interface ProductosClient {

    @GetMapping("/api/productos")
    ResponseEntity<Object> listarProductos(
            @RequestHeader("Authorization") String authorization);

    @GetMapping("/api/productos/{id}")
    ResponseEntity<Object> buscarProductoPorId(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id);

    @PostMapping("/api/productos")
    ResponseEntity<Object> crearProducto(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> producto);

    @PutMapping("/api/productos/{id}")
    ResponseEntity<Object> actualizarProducto(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> producto);

    @DeleteMapping("/api/productos/{id}")
    ResponseEntity<Void> eliminarProducto(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long id);
}
