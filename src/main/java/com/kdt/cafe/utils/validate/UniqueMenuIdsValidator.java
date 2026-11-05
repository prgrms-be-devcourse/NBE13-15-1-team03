package com.kdt.cafe.utils.validate;

import com.kdt.cafe.dto.order.OrderItemRequestDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class UniqueMenuIdsValidator implements ConstraintValidator<UniqueMenuIds, List<OrderItemRequestDTO>> {

    @Override
    public boolean isValid(List<OrderItemRequestDTO> items, ConstraintValidatorContext context) {
        if (items == null) {
            return true; // 누락은 @NotNull 담당
        }
        List<Integer> menuIds = items.stream()
                .filter(item -> item != null && item.menuId() != null) // 빈 항목·menuId 누락은 항목 검증(@NotNull)이 담당
                .map(OrderItemRequestDTO::menuId)
                .toList();
        return menuIds.size() == menuIds.stream().distinct().count(); // 개수가 그대로면 중복이 없다는 것
    }
}
