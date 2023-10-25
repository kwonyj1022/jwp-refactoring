package kitchenpos.dto;

public class CreateMenuProductRequest {

    private Long productId;
    private long quantity;

    public CreateMenuProductRequest() {
    }

    public CreateMenuProductRequest(final Long productId, final long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public long getQuantity() {
        return quantity;
    }
}
