package kitchenpos.application;

import kitchenpos.domain.order.Orders;
import kitchenpos.domain.table.OrderTable;
import kitchenpos.domain.table.TableGroup;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.repository.TableGroupRepository;
import kitchenpos.dto.CreateTableGroupRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TableGroupService {

    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;

    public TableGroupService(
            final OrderRepository orderRepository,
            final OrderTableRepository orderTableRepository,
            final TableGroupRepository tableGroupRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
    }

    @Transactional
    public TableGroup create(final CreateTableGroupRequest request) {
        final List<Long> orderTableIdRequests = request.getOrderTableIds();
        validateTableCount(orderTableIdRequests);
        final List<OrderTable> savedOrderTables = orderTableRepository.findAllByIdIn(orderTableIdRequests);

        validateTableIdAllExists(orderTableIdRequests, savedOrderTables);

        return saveTableGroup(savedOrderTables);
    }

    private void validateTableCount(final List<Long> orderTableIds) {
        if (orderTableIds.isEmpty() || orderTableIds.size() < 2) {
            throw new IllegalArgumentException("그룹화하려는 테이블은 2개 이상이어야 합니다.");
        }
    }

    private void validateTableIdAllExists(final List<Long> orderTableIds, final List<OrderTable> savedOrderTables) {
        if (orderTableIds.size() != savedOrderTables.size()) {
            throw new IllegalArgumentException("그룹화를 요청한 테이블 중에 존재하지 않는 테이블이 포함되어 있습니다.");
        }
    }

    private TableGroup saveTableGroup(final List<OrderTable> savedOrderTables) {
        final TableGroup newTableGroup = tableGroupRepository.save(new TableGroup());

        for (final OrderTable orderTable : savedOrderTables) {
            orderTable.groupBy(newTableGroup);
        }

        return newTableGroup;
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final List<OrderTable> orderTables = orderTableRepository.findAllByTableGroupId(tableGroupId);

        validateCanUngroup(orderTables);

        ungroupOrderTables(orderTables);
    }

    private void validateCanUngroup(final List<OrderTable> orderTables) {
        final Orders orders = new Orders(orderRepository.findAllByOrderTableIn(orderTables));
        if (orders.containsNotCompleteOrder()) {
            throw new IllegalArgumentException("테이블 그룹을 해제하려면 그룹화된 테이블의 모든 주문이 완료 상태이어야 합니다.");
        }
    }

    private void ungroupOrderTables(final List<OrderTable> orderTables) {
        for (final OrderTable orderTable : orderTables) {
            orderTable.ungroup();
        }
    }
}
