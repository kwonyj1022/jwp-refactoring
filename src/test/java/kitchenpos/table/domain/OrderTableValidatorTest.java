package kitchenpos.table.domain;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menugroup.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.product.domain.Product;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderLineItems;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.menugroup.repository.MenuGroupRepository;
import kitchenpos.menu.repository.MenuRepository;
import kitchenpos.order.repository.OrderRepository;
import kitchenpos.table.repository.OrderTableRepository;
import kitchenpos.product.repository.ProductRepository;
import kitchenpos.tablegroup.repository.TableGroupRepository;
import kitchenpos.tablegroup.domain.TableGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

import static kitchenpos.menu.domain.MenuFixture.menu;
import static kitchenpos.order.domain.OrderFixture.order;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("NonAsciiCharacters")
class OrderTableValidatorTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private OrderTableValidator orderTableValidator;

    @Autowired
    private OrderTableRepository orderTableRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MenuGroupRepository menuGroupRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private TableGroupRepository tableGroupRepository;

    @Test
    @DisplayName("주어진 테이블 아이디에 해당하는 테이블에 존재하는 주문이 모두 완료된 주문이라면 테이블의 비어있음 정보를 변경할 수 있다")
    void validateCanChangeEmpty() {
        // given
        final OrderTable 두명_테이블 = orderTableRepository.save(new OrderTable(2, true));

        em.flush();
        em.clear();

        // when & then
        assertThatCode(() -> orderTableValidator.validateCanChangeEmpty(두명_테이블))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("주어진 테이블이 그룹화된 테이블이라면 테이블의 비어있음 정보를 변경할 수 없다")
    void validateCanChangeEmpty_grouped() {
        // given
        final OrderTable 그룹화된_테이블 = orderTableRepository.save(new OrderTable(2, true));
        final TableGroup 테이블_그룹 = tableGroupRepository.save(new TableGroup());
        그룹화된_테이블.groupBy(테이블_그룹.getId());

        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> orderTableValidator.validateCanChangeEmpty(그룹화된_테이블))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("그룹화된 테이블의 상태를 변경할 수 없습니다.");

    }

    @ParameterizedTest(name = "주문 상태가 {0}인 주문이 포함되어 있을 때")
    @EnumSource(value = OrderStatus.class, names = {"COOKING", "MEAL"})
    @DisplayName("주어진 테이블 아이디에 해당하는 테이블에 존재하는 주문 중 완료되지 않은 주문이 포함되어 있으면 테이블의 비어있음 정보를 변경할 수 없다")
    void validateCanChangeEmpty_fail(final OrderStatus notCompleteStatus) {
        // given
        final Product 후라이드 = productRepository.save(new Product("후라이드", BigDecimal.valueOf(16000)));
        final MenuGroup 두마리메뉴 = menuGroupRepository.save(new MenuGroup("두마리메뉴"));
        final MenuProduct 후라이드_2개 = new MenuProduct(후라이드.getId(), 2L);
        final Menu 후라이드_2개_메뉴 = menuRepository.save(menu("후라이드+후라이드", BigDecimal.valueOf(30000), 두마리메뉴.getId(), List.of(후라이드_2개)));
        final OrderLineItem 주문_항목 = new OrderLineItem(후라이드_2개_메뉴.getId(), 2);
        final OrderTable 두명_테이블 = orderTableRepository.save(new OrderTable(2, false));
        orderRepository.save(order(두명_테이블.getId(), OrderStatus.COMPLETION, new OrderLineItems(List.of(주문_항목))));
        orderRepository.save(order(두명_테이블.getId(), notCompleteStatus, new OrderLineItems(List.of(주문_항목))));

        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> orderTableValidator.validateCanChangeEmpty(두명_테이블))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 상태가 조리중이거나 식사중인 주문이 남아있다면 테이블 상태를 변경할 수 없습니다.");
    }
}