package com.restaurant.tablemanagement.model;

public class Table {
    private int tableId;
    private int capacity;
    private TableStatus status;
    private long cleaningEndTime;
    private Integer customerId;  // Tracks which customer (if any) is at this table

    public Table(int tableId, int capacity) {
        this.tableId = tableId;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
        this.customerId = null;
    }

    public int getTableId() { return tableId; }
    public int getCapacity() { return capacity; }
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    public long getCleaningEndTime() { return cleaningEndTime; }
    public void setCleaningEndTime(long time) { this.cleaningEndTime = time; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
}
