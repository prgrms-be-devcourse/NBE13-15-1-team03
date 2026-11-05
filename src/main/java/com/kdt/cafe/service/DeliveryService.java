package com.kdt.cafe.service;

import com.kdt.cafe.dto.DeliveryBatch;
import com.kdt.cafe.dto.DeliveryRequestDto;
import com.kdt.cafe.dto.OrderItemResponseDTO;
import com.kdt.cafe.entity.Order;
import com.kdt.cafe.entity.OrderItem;
import com.kdt.cafe.entity.OrderStatus;
import com.kdt.cafe.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    // 항목 합산 기준이자 정렬 기준: 메뉴 ID → 단가 → 메뉴명 오름차순
    private record ItemKey(int menuId, int unitPrice, String menuName) {}

    private static final Comparator<ItemKey> ITEM_ORDER = Comparator.comparingInt(ItemKey::menuId)
            .thenComparingInt(ItemKey::unitPrice)
            .thenComparing(ItemKey::menuName);

    private final OrderRepository orderRepository;

    public DeliveryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<DeliveryBatch> getOrdersByShippingDate(DeliveryRequestDto dto) {
        // 주문 + 항목
        List<Order> orders = orderRepository.findByShippingDateAndStatus(dto.shippingDate(), OrderStatus.PLACED);

        // TreeMap → 이메일 오름차순(대소문자 구분), 그룹 안은 id 오름차순 유지
        Map<String, List<Order>> ordersByEmail = orders.stream()
                .collect(Collectors.groupingBy(Order::getEmail, TreeMap::new, Collectors.toList()));

        List<DeliveryBatch> batches = new ArrayList<>(ordersByEmail.size());
        for (List<Order> group : ordersByEmail.values()) {
            batches.add(toBatch(dto.shippingDate(), group));
        }
        return batches;
    }

    private DeliveryBatch toBatch(LocalDate shippingDate, List<Order> group) {
        Order first = group.getFirst();
        List<Long> orderIds = new ArrayList<>(group.size());

        // TreeMap → 넣는 순간 정렬, 같은 키면 수량 합산
        Map<ItemKey, Integer> quantityByItem = new TreeMap<>(ITEM_ORDER);
        int totalQuantity = 0;
        int totalAmount = 0;

        // 항목을 한 번만 순회하면서 메뉴별 합산과 묶음 합계를 같이 계산
        for (Order order : group) {
            orderIds.add(order.getId());
            for (OrderItem item : order.getOrderItems()) {
                ItemKey key = new ItemKey(item.getMenu().getId(), item.getUnitPrice(), item.getMenuName());
                quantityByItem.merge(key, item.getQuantity(), Integer::sum);
                totalQuantity += item.getQuantity();
                totalAmount += item.getLineAmount();
            }
        }

        List<OrderItemResponseDTO> items = new ArrayList<>(quantityByItem.size());
        for (Map.Entry<ItemKey, Integer> entry : quantityByItem.entrySet()) {
            ItemKey key = entry.getKey();
            int quantity = entry.getValue();
            items.add(new OrderItemResponseDTO(key.menuId(), key.menuName(), key.unitPrice(),
                    quantity, key.unitPrice() * quantity));
        }

        return new DeliveryBatch(shippingDate, first.getEmail(), first.getAddress(), first.getPostcode(),
                orderIds, orderIds.size(), totalQuantity, totalAmount, items);
    }
}
