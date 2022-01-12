package ua.com.solidity.scheduler;

public class MonthsSet extends Param {
    @Override
    protected void initialize() {
        doInitialize(0, 11, true);
    }
}
