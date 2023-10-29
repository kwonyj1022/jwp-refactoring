package kitchenpos.menu.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CreateMenuRequest {

    private String name;
    private BigDecimal price;
    private Long menuGroupId;
    private List<CreateMenuProductRequest> menuProducts;

    public CreateMenuRequest() {
    }

    public CreateMenuRequest(final String name, final BigDecimal price, final Long menuGroupId, final List<CreateMenuProductRequest> menuProducts) {
        this.name = name;
        this.price = price;
        this.menuGroupId = menuGroupId;
        this.menuProducts = menuProducts;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getMenuGroupId() {
        return menuGroupId;
    }

    public CreateMenuDto toCreateMenuDto() {
        return new CreateMenuDto(name, price, menuGroupId, toMenuProductDtos());
    }

    private List<CreateMenuProductDto> toMenuProductDtos() {
        return menuProducts.stream()
                           .map(CreateMenuProductRequest::toCreateMenuProductDto)
                           .collect(Collectors.toList());
    }
}