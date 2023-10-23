package kitchenpos.ui.dto;

import kitchenpos.domain.OrderLineItem;

public class CreateOrderLineItemRequest {

    private Long menuId;
    private long quantity;

    public CreateOrderLineItemRequest() {
    }

    public CreateOrderLineItemRequest(final Long menuId, final long quantity) {
        this.menuId = menuId;
        this.quantity = quantity;
    }

    public Long getMenuId() {
        return menuId;
    }

    public long getQuantity() {
        return quantity;
    }

    public OrderLineItem toEntity() {
        final OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setMenuId(menuId);
        orderLineItem.setQuantity(quantity);

        return orderLineItem;
    }
}