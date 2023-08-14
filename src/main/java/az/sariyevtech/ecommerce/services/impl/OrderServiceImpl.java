package az.sariyevtech.ecommerce.services.impl;

import az.sariyevtech.ecommerce.dto.OrderDto;
import az.sariyevtech.ecommerce.dto.converter.OrderConverter;
import az.sariyevtech.ecommerce.dto.productDto.ProductDto;
import az.sariyevtech.ecommerce.dto.request.OrderCreateRequest;
import az.sariyevtech.ecommerce.model.order.OrderModel;
import az.sariyevtech.ecommerce.repository.OrderRepository;
import az.sariyevtech.ecommerce.response.TokenResponse;
import az.sariyevtech.ecommerce.services.OrderService;
import az.sariyevtech.ecommerce.services.ProductService;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderConverter converter;
    private final TokenResponse tokenResponse;
    private final ProductService productService;

    public OrderServiceImpl(OrderRepository repository,
                            OrderConverter converter,
                            TokenResponse tokenResponse, ProductService productService) {
        this.repository = repository;
        this.converter = converter;
        this.tokenResponse = tokenResponse;
        this.productService = productService;
    }

    @Override
    public List<OrderDto> getAllOrders() {
        return repository.findAll().stream().map(converter::toDto).collect(Collectors.toList());
    }

    @Override
    public OrderDto findById(Long id) {
        final OrderModel order = repository.findById(id).orElseThrow();
        return converter.toDto(order);
    }

    //TODO create findByUserId method in repo
    @Override
    public List<OrderDto> findUserOrders() {
        var userId = tokenResponse.getUserId();
        return repository.findByCustomerId(userId).stream().map(converter::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto createOrder(OrderCreateRequest request) {
        final ProductDto product = productService.viewProduct(request.getProductId());
        final var customerId = tokenResponse.getUserId();
        final OrderModel order = OrderModel.builder()
                .customerId(customerId)
                .productId(request.getProductId())
                .count(request.getCount())
                .description(request.getDescription())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryTime(request.getDeliveryTime())
                .paymentType(request.getPaymentType())
                .deliveryLocType(request.getDeliveryLocType())
                .build();
        order.setTotalPrice(request.getCount() * product.getPrice());
        final OrderModel orderFromDb = repository.save(order);
        return converter.toDto(orderFromDb);
    }

    @Transactional
    @Override
    public OrderDto updateOrder(Long id, OrderDto request) {
        return null;
    }

    @Override
    public void deleteOrder(Long id) {

    }
}
