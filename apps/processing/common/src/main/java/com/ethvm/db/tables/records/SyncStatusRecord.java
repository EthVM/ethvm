/*
 * This file is generated by jOOQ.
 */
package com.ethvm.db.tables.records;


import com.ethvm.db.tables.SyncStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.12"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SyncStatusRecord extends TableRecordImpl<SyncStatusRecord> implements Record5<String, BigDecimal, Timestamp, Timestamp, Long> {

    private static final long serialVersionUID = 1737918118;

    /**
     * Setter for <code>public.sync_status.component</code>.
     */
    public SyncStatusRecord setComponent(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.sync_status.component</code>.
     */
    public String getComponent() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.sync_status.block_number</code>.
     */
    public SyncStatusRecord setBlockNumber(BigDecimal value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.sync_status.block_number</code>.
     */
    public BigDecimal getBlockNumber() {
        return (BigDecimal) get(1);
    }

    /**
     * Setter for <code>public.sync_status.timestamp</code>.
     */
    public SyncStatusRecord setTimestamp(Timestamp value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.sync_status.timestamp</code>.
     */
    public Timestamp getTimestamp() {
        return (Timestamp) get(2);
    }

    /**
     * Setter for <code>public.sync_status.block_timestamp</code>.
     */
    public SyncStatusRecord setBlockTimestamp(Timestamp value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.sync_status.block_timestamp</code>.
     */
    public Timestamp getBlockTimestamp() {
        return (Timestamp) get(3);
    }

    /**
     * Setter for <code>public.sync_status.row_number</code>.
     */
    public SyncStatusRecord setRowNumber(Long value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.sync_status.row_number</code>.
     */
    public Long getRowNumber() {
        return (Long) get(4);
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<String, BigDecimal, Timestamp, Timestamp, Long> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row5<String, BigDecimal, Timestamp, Timestamp, Long> valuesRow() {
        return (Row5) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return SyncStatus.SYNC_STATUS.COMPONENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<BigDecimal> field2() {
        return SyncStatus.SYNC_STATUS.BLOCK_NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field3() {
        return SyncStatus.SYNC_STATUS.TIMESTAMP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field4() {
        return SyncStatus.SYNC_STATUS.BLOCK_TIMESTAMP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return SyncStatus.SYNC_STATUS.ROW_NUMBER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal component2() {
        return getBlockNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component3() {
        return getTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component4() {
        return getBlockTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component5() {
        return getRowNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal value2() {
        return getBlockNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value3() {
        return getTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value4() {
        return getBlockTimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getRowNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncStatusRecord value1(String value) {
        setComponent(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncStatusRecord value2(BigDecimal value) {
        setBlockNumber(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncStatusRecord value3(Timestamp value) {
        setTimestamp(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncStatusRecord value4(Timestamp value) {
        setBlockTimestamp(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncStatusRecord value5(Long value) {
        setRowNumber(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SyncStatusRecord values(String value1, BigDecimal value2, Timestamp value3, Timestamp value4, Long value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached SyncStatusRecord
     */
    public SyncStatusRecord() {
        super(SyncStatus.SYNC_STATUS);
    }

    /**
     * Create a detached, initialised SyncStatusRecord
     */
    public SyncStatusRecord(String component, BigDecimal blockNumber, Timestamp timestamp, Timestamp blockTimestamp, Long rowNumber) {
        super(SyncStatus.SYNC_STATUS);

        set(0, component);
        set(1, blockNumber);
        set(2, timestamp);
        set(3, blockTimestamp);
        set(4, rowNumber);
    }
}