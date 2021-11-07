package ua.com.solidity.scheduler;

public class DaysOfMonthSet extends Param {
    @Override
    protected void initialize() {
        doInitialize(-7, 27, false);
    }
}
