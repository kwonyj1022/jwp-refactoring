package kitchenpos.tablegroup.domain;

public class UngroupTableEvent {

    private final TableGroup tableGroup;

    public UngroupTableEvent(final TableGroup tableGroup) {
        this.tableGroup = tableGroup;
    }

    public TableGroup getTableGroup() {
        return tableGroup;
    }
}