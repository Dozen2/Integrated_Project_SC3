package sit.int221.sc3_server.service.order;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sit.int221.sc3_server.DTO.order.OrderItems;
import sit.int221.sc3_server.DTO.order.OrderRequest;
import sit.int221.sc3_server.DTO.order.OrderResponse;
import sit.int221.sc3_server.DTO.order.SellerResponseOrder;
import sit.int221.sc3_server.entity.*;
import sit.int221.sc3_server.exception.ForbiddenException;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.repository.order.OrderDetailRepository;
import sit.int221.sc3_server.repository.order.OrderRepository;
import sit.int221.sc3_server.repository.saleItem.SaleitemRepository;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.utils.Role;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
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

    @Transactional
    public Order createOrder(OrderRequest orderRequest){
        Buyer buyer = buyerRepository.findById(orderRequest.getBuyerId()).orElseThrow(()->new ItemNotFoundException("buyer not found"));
        Seller seller = sellerRepository.findById(orderRequest.getSellerId()).orElseThrow(()->new ItemNotFoundException("seller not found"));

        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setOrderStatus(orderRequest.getOrderStatus());
        order.setPaymentStatus("PENDING");
        order.setOrderNote(orderRequest.getOrderNote());
//        order.setOrderDate(orderRequest.getOrderDate());
        order.setOrderDate(Instant.now());

        order = orderRepository.save(order);
        Set<OrderDetail> orderDetails = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItems itemRequest:
             orderRequest.getOrderItems()) {
            SaleItem saleItem = saleitemRepository.findById(itemRequest.getSaleItemId()).orElseThrow(()->new ItemNotFoundException("sale item not found"));
            if(!saleItem.getSeller().getId().equals(seller.getId())){
                throw new ForbiddenException("Order cannot contain other seller's saleItem");
            }
            if(itemRequest.getQuantity() <= 0){
                throw new RuntimeException("Quantity must no equal or lower than 0");
            }
            if (saleItem.getQuantity() < itemRequest.getQuantity()){
                throw new RuntimeException("Not enough stock for " + saleItem.getModel());
            }
            saleItem.setQuantity(saleItem.getQuantity() - itemRequest.getQuantity());
            saleitemRepository.save(saleItem);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setSaleItem(saleItem);
            orderDetail.setPriceEachAtPurchase(saleItem.getPrice());
            orderDetail.setQuantity(itemRequest.getQuantity()); // จำนวนที่ user สั่ง\
            orderDetailRepository.save(orderDetail);

            BigDecimal subTotal = BigDecimal.valueOf(saleItem.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(subTotal);
            orderDetails.add(orderDetail);
        }
        order.setOrderDetails(orderDetails);
        order.setTotalPrice(total);
        return orderRepository.save(order);



    }

    public Order getOrderById(Integer id){
        return orderRepository.findById(id).orElseThrow(()->new ItemNotFoundException("order not found"));
    }

    public Page<Order> findAllBuyersOrder(Integer userId,Integer page,Integer size){
        return orderRepository.findAllOrderByBuyerId(userId, PageRequest.of(page,size, Sort.by("orderDate").descending()));
    }

    public OrderResponse mapOrderToResponseDTO(Order order){
        OrderResponse dto = new OrderResponse();
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setBuyerId(order.getBuyer().getId());
        dto.setId(order.getId());
        dto.setOrderNote(order.getOrderNote());

        SellerResponseOrder sellerDto = new SellerResponseOrder();
        sellerDto.setId(order.getSeller().getId());
        sellerDto.setEmail(order.getSeller().getBuyer().getEmail());
        sellerDto.setFullName(order.getSeller().getBuyer().getFullName());
        sellerDto.setNickName(order.getSeller().getBuyer().getNickName());
        sellerDto.setUserType(Role.SELLER.name());
        dto.setSeller(sellerDto);

      List<OrderItems> items = new ArrayList<>();
      int no = 1;
        for (OrderDetail detail:
             order.getOrderDetails()) {
            OrderItems orderItems = new OrderItems();
            orderItems.setNo(no++);
            orderItems.setSaleItemId(detail.getSaleItem().getId());
            orderItems.setPrice(detail.getPriceEachAtPurchase());
            orderItems.setQuantity(detail.getQuantity());
            orderItems.setDescription(detail.getSaleItem().getDescription());
            items.add(orderItems);
        }
        dto.setOrderItems(items);
        return dto;


    }

//    public Page<OrderRequest> MapAllOrder(Order order){
//
//    }


}
