package com.duoc.servicioPedidos.service;

import com.duoc.servicioPedidos.messaging.PedidoEventPublisher;
import com.duoc.servicioPedidos.model.Pedido;
import com.duoc.servicioPedidos.repository.PedidoRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoEventPublisher pedidoEventPublisher;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PedidoEventPublisher pedidoEventPublisher) {

        this.pedidoRepository = pedidoRepository;
        this.pedidoEventPublisher = pedidoEventPublisher;
    }

    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> buscarPorUsuario(String usuario) {
        return pedidoRepository.findByUsuario(usuario);
    }

    public Pedido crear(Pedido pedido) {

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        pedidoEventPublisher.publicarPedidoCreado(pedidoGuardado);

        return pedidoGuardado;
    }

    public Pedido cambiarEstado(Long id, String estado) {

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(
                        () -> new RuntimeException("Pedido no encontrado")
                );

        pedido.setEstado(estado);

        return pedidoRepository.save(pedido);
    }

    public void eliminar(Long id) {

        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado");
        }

        pedidoRepository.deleteById(id);
    }
}