package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

import static kitchenpos.fixture.OrderFixture.order;
import static kitchenpos.fixture.OrderTableFixture.orderTable;
import static kitchenpos.fixture.TableGroupFixture.tableGroup;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TableGroupServiceTest {

    @Autowired
    private TableGroupService tableGroupService;

    @Autowired
    private OrderTableDao orderTableDao;

    @Autowired
    private TableGroupDao tableGroupDao;

    @Autowired
    private OrderDao orderDao;

    @Test
    @DisplayName("테이블 그룹을 등록한다")
    void create() {
        // given
        final OrderTable 두명_테이블 = orderTableDao.save(orderTable(2, true));
        final OrderTable 네명_테이블 = orderTableDao.save(orderTable(4, true));
        final TableGroup 두명_네명_테이블 = tableGroup(List.of(두명_테이블, 네명_테이블));

        // when
        final TableGroup actual = tableGroupService.create(두명_네명_테이블);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getId()).isPositive();
            softAssertions.assertThat(actual.getCreatedDate()).isNotNull();
        });
    }

    @Test
    @DisplayName("테이블 그룹을 등록할 때 테이블 목록이 비어있으면 예외가 발생한다")
    void create_emptyOrderTables() {
        // given
        final TableGroup invalidTableGroup = tableGroup(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(invalidTableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테이블 그룹을 등록할 때 테이블 목록이 1개이면 예외가 발생한다")
    void create_oneOrderTable() {
        // given
        final OrderTable 두명_테이블 = orderTableDao.save(orderTable(2, true));
        final TableGroup invalidTableGroup = tableGroup(List.of(두명_테이블));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(invalidTableGroup))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테이블 그룹을 등록할 때 등록하려는 테이블이 모두 존재하지 않으면 예외가 발생한다")
    void create_invalidNumberOfTable() {
        // given
        final OrderTable 두명_테이블 = orderTableDao.save(orderTable(2, true));
        final OrderTable 네명_테이블 = orderTable(4, true);
        네명_테이블.setId(10L);

        final TableGroup 두명_네명_테이블 = tableGroup(List.of(두명_테이블, 네명_테이블));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(두명_네명_테이블))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테이블 그룹을 등록할 때 테이블이 비어있지 않으면 예외가 발생한다")
    void create_notEmptyTable() {
        // given
        final OrderTable 두명_테이블 = orderTableDao.save(orderTable(2, true));
        final OrderTable 네명_테이블_사용중 = orderTableDao.save(orderTable(4, false));
        final TableGroup 두명_네명_테이블 = tableGroup(List.of(두명_테이블, 네명_테이블_사용중));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(두명_네명_테이블))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테이블 그룹을 등록할 때 테이블이 이미 그룹화 되어 있다면 예외가 발생한다")
    void create_alreadyGroup() {
        // given
        final OrderTable 두명_테이블 = orderTableDao.save(orderTable(2, true));
        final OrderTable 세명_테이블 = orderTableDao.save(orderTable(3, true));
        final OrderTable 네명_테이블 = orderTableDao.save(orderTable(4, true));
        final TableGroup 그룹화된_세명_네명_테이블 = tableGroupDao.save(tableGroup());
        orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 세명_테이블));
        orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 네명_테이블));

        final TableGroup 두명_네명_테이블 = tableGroup(List.of(두명_테이블, 네명_테이블));

        // when & then
        assertThatThrownBy(() -> tableGroupService.create(두명_네명_테이블))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("테이블 그룹을 해제한다")
    void ungroup() {
        // given
        final OrderTable 세명_테이블 = orderTableDao.save(orderTable(3, true));
        final OrderTable 네명_테이블 = orderTableDao.save(orderTable(4, true));
        final TableGroup 그룹화된_세명_네명_테이블 = tableGroupDao.save(tableGroup());
        final OrderTable 그룹화된_세명_테이블 = orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 세명_테이블));
        final OrderTable 그룹화된_네명_테이블 = orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 네명_테이블));

        // when
        tableGroupService.ungroup(그룹화된_세명_네명_테이블.getId());

        // then
        final List<OrderTable> actual = orderTableDao.findAllByIdIn(List.of(그룹화된_세명_테이블.getId(), 그룹화된_네명_테이블.getId()));

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.get(0).getTableGroupId()).isNull();
            softAssertions.assertThat(actual.get(0).isEmpty()).isFalse();
            softAssertions.assertThat(actual.get(1).getTableGroupId()).isNull();
            softAssertions.assertThat(actual.get(1).isEmpty()).isFalse();
        });
    }

    @Test
    @DisplayName("테이블 그룹을 해제할 때 해제하려는 테이블의 주문이 조리중이나 식사중이면 예외가 발생한다")
    void ungroup_invalidOrderStatus() {
        // given
        final OrderTable 세명_테이블 = orderTableDao.save(orderTable(3, true));
        final OrderTable 네명_테이블 = orderTableDao.save(orderTable(4, true));
        final TableGroup 그룹화된_세명_네명_테이블 = tableGroupDao.save(tableGroup());
        final OrderTable 그룹화된_세명_테이블 = orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 세명_테이블));
        final OrderTable 그룹화된_네명_테이블 = orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 네명_테이블));

        // when
        tableGroupService.ungroup(그룹화된_세명_네명_테이블.getId());

        // then
        final List<OrderTable> actual = orderTableDao.findAllByIdIn(List.of(그룹화된_세명_테이블.getId(), 그룹화된_네명_테이블.getId()));

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.get(0).getTableGroupId()).isNull();
            softAssertions.assertThat(actual.get(1).getTableGroupId()).isNull();
        });
    }

    @ParameterizedTest(name = "주문 상태가 {0}일 때")
    @EnumSource(value = OrderStatus.class, names = {"COOKING", "MEAL"})
    @DisplayName("테이블 그룹을 해제할 때 해제하려는 테이블의 주문이 조리중이나 식사중이면 예외가 발생한다")
    void ungroup_invalidOrderStatus(final OrderStatus orderStatus) {
        // given
        final OrderTable 세명_테이블 = orderTableDao.save(orderTable(3, true));
        final OrderTable 네명_테이블 = orderTableDao.save(orderTable(4, true));
        final TableGroup 그룹화된_세명_네명_테이블 = tableGroupDao.save(tableGroup());
        final OrderTable 그룹화된_세명_테이블 = orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 세명_테이블));
        final OrderTable 그룹화된_네명_테이블 = orderTableDao.save(orderTable(그룹화된_세명_네명_테이블.getId(), 네명_테이블));

        orderDao.save(order(그룹화된_세명_테이블.getId(), orderStatus));
        orderDao.save(order(그룹화된_네명_테이블.getId(), orderStatus));

        // when & then
        assertThatThrownBy(() -> tableGroupService.ungroup(그룹화된_세명_네명_테이블.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
