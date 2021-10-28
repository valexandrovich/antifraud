package ua.com.solidity.scheduler;

public class MinutesSet extends Param {
    public MinutesSet() {
        super(0, 1439, true, true);
    }

    @Override
    protected int zoneCheck(int[] indexes, int indexCount) {
        if (indexCount == 0) return 0;
        return indexCount; // check for Zone and replace indexes or add alternative index
    }
}
