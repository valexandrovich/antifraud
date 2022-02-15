package ua.com.solidity.common;

import ua.com.solidity.common.data.DataObject;

public class ZeroParser extends CustomParser {
    @Override
    protected boolean doOpen() {
        return true;
    }

    @Override
    public DataObject internalDataObject() {
        return null;
    }

    @Override
    protected boolean doNext() {
        return false;
    }
}
