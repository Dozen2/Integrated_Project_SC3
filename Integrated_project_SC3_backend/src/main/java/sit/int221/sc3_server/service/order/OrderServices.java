package sit.int221.sc3_server.service.order;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sit.int221.sc3_server.DTO.order.*;
import sit.int221.sc3_server.entity.*;
import sit.int221.sc3_server.exception.ForbiddenException;
import sit.int221.sc3_server.exception.crudException.ItemNotFoundException;
import sit.int221.sc3_server.exception.crudException.OutOfStockException;
import sit.int221.sc3_server.exception.crudException.UpdateFailedException;
import sit.int221.sc3_server.repository.order.OrderDetailRepository;
import sit.int221.sc3_server.repository.order.OrderRepository;
import sit.int221.sc3_server.repository.saleItem.SaleItemImageRepository;
import sit.int221.sc3_server.repository.saleItem.SaleitemRepository;
import sit.int221.sc3_server.repository.user.BuyerRepository;
import sit.int221.sc3_server.repository.user.SellerRepository;
import sit.int221.sc3_server.utils.Role;

import java.math.BigDecimal;
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
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

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
        order.setPaymentDate(null);

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
                throw new OutOfStockException("Quantity must no equal or lower than 0");
            }
            if (saleItem.getQuantity() < itemRequest.getQuantity()){
                throw new OutOfStockException("Not enough stock for " + saleItem.getModel());
            }
            saleItem.setQuantity(saleItem.getQuantity() - itemRequest.getQuantity());
            saleitemRepository.save(saleItem);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setSaleItem(saleItem);
            orderDetail.setPriceEachAtPurchase(saleItem.getPrice());
            orderDetail.setQuantity(itemRequest.getQuantity()); // จำนวนที่ user สั่ง\
            orderDetail.setDescription(itemRequest.getDescription());
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
            String image = saleItemImageRepository.findBySaleItemId(detail.getSaleItem().getId());
            OrderItems orderItems = new OrderItems();
            orderItems.setNo(no++);
            orderItems.setSaleItemId(detail.getSaleItem().getId());
            orderItems.setPrice(detail.getPriceEachAtPurchase());
            orderItems.setQuantity(detail.getQuantity());
            orderItems.setDescription(detail.getDescription());
            orderItems.setMainImageFileName(image);
            items.add(orderItems);
        }
        dto.setOrderItems(items);
        return dto;


    }

//    public Page<OrderResponseBuyer> mapSellerOrder(Order order){
//
//    }

    public Order payOrder(Integer orderId){
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new ItemNotFoundException("order does not exist"));
        if(order.getPaymentDate() != null || order.getOrderStatus().equals("PAID")){
            throw new UpdateFailedException("user already paid");
        }

        order.setPaymentDate(Instant.now());
        order.setPaymentStatus("PAID");
         return order;
    }

    public Page<OrderResponseSeller> findAllBuyersOrderResponse(Integer buyerId, int page, int size) {
        Page<Order> orders = orderRepository.findByBuyerId(buyerId, PageRequest.of(page, size));

        // map order -> response
        return orders.map(this::mapOrderToResponse);
    }

    private OrderResponseSeller mapOrderToResponse(Order order) {
        OrderResponseSeller response = new OrderResponseSeller();

        response.setId(order.getId());

        // map buyer
        BuyerDTO buyerDTO = new BuyerDTO();
        buyerDTO.setId(order.getBuyer().getId());
        buyerDTO.setUsername(order.getBuyer().getEmail());
        response.setBuyerDTO(buyerDTO);

        // map field ตรงๆ
        response.setOrderDate(order.getOrderDate());
        response.setPaymentDate(order.getPaymentDate());
        response.setShippingAddress(order.getShippingAddress());
        response.setOrderNote(order.getOrderNote());
        response.setOrderStatus(order.getOrderStatus());

        // map order items
        List<OrderItems> items = order.getOrderDetails()
                .stream()
                .map(item -> {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setNo(item.getId());
                    orderItem.setSaleItemId(item.getSaleItem().getId());
                    orderItem.setPrice(item.getPriceEachAtPurchase());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setDescription(item.getDescription());
                    return orderItem;
                }).toList();

        response.setOrderItems(items);

        return response;
    }
    public void set(){

    }

//    public Page<OrderRequest> MapAllOrder(Order order){
//
//    }


}
