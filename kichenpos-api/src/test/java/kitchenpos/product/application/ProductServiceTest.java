package kitchenpos.product.application;

import kitchenpos.common.Price;
import kitchenpos.product.dto.CreateProductDto;
import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품을 등록한다")
    void create() {
        // given
        final CreateProductDto request = new CreateProductDto("강정치킨", BigDecimal.valueOf(17000));

        // when
        final Product actual = productService.create(request);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getId()).isPositive();
            softAssertions.assertThat(actual.getName()).isEqualTo(request.getName());
            softAssertions.assertThat(actual.getPrice()).isEqualTo(new Price(request.getPrice()));
        });
    }

    @Test
    @DisplayName("상품 등록 시 상품의 가격이 0원 미만이면 예외가 발생한다")
    void create_invalidPrice() {
        // given
        final BigDecimal invalidPrice = BigDecimal.valueOf(-1);
        final CreateProductDto invalidRequest = new CreateProductDto("-1원 상품", invalidPrice);

        // when & then
        assertThatThrownBy(() -> productService.create(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 0원 이상이어야 합니다.");
    }

    @Test
    @DisplayName("상품 목록을 조회한다")
    void list() {
        // given
        final Product expect1 = productRepository.save(new Product("후라이드", BigDecimal.valueOf(17000)));
        final Product expect2 = productRepository.save(new Product("양념치킨", BigDecimal.valueOf(20000)));

        em.flush();
        em.clear();

        // when
        final List<Product> actual = productService.list();

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).hasSize(2);
            softAssertions.assertThat(actual.get(0)).isEqualTo(expect1);
            softAssertions.assertThat(actual.get(1)).isEqualTo(expect2);
        });
    }
}