package ua.com.solidity.scheduler;

public class DaysSet extends Param {
    @Override
    protected void initialize()
    {
        doInitialize(-7, 364, true);
    }
}
