package sit.int221.sc3_server.service.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.sc3_server.DTO.order.OrderItems;
import sit.int221.sc3_server.DTO.order.OrderRequest;
import sit.int221.sc3_server.entity.*;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.repository.order.OrderDetailRepository;
import sit.int221.sc3_server.repository.order.OrderRepository;
import sit.int221.sc3_server.repository.saleItem.SaleitemRepository;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderServices {
    @Autowired
    private SaleitemRepository saleitemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private SellerRepository sellerRepository;

    public Order createOrder(OrderRequest orderRequest){
        Buyer buyer = buyerRepository.findById(orderRequest.getBuyerId()).orElseThrow(()->new ItemNotFoundException("buyer not found"));
        Seller seller = sellerRepository.findById(orderRequest.getSellerId()).orElseThrow(()->new ItemNotFoundException("seller not found"));
        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderStatus(orderRequest.getOrderStatus());
        order.setPaymentStatus(orderRequest.getOrderStatus());
        order.setOrderNote(orderRequest.getOrderNote());

        Set<OrderDetail> orderDetails = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItems itemRequest:
             orderRequest.getOrderItems()) {
            SaleItem saleItem = saleitemRepository.findById(itemRequest.getSaleItemId()).orElseThrow(()->new ItemNotFoundException("sale item not found"));
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setSaleItem(saleItem);
            orderDetail.setPriceEachAtPurchase(saleItem.getPrice());

            BigDecimal subTotal = BigDecimal.valueOf(saleItem.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(subTotal);
            orderDetails.add(orderDetail);
        }
        order.setOrderDetails(orderDetails);
        order.setTotalPrice(total);
        return orderRepository.save(order);



    }



}
