package ua.com.solidity.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.BitSet;

public class Param {
    public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";

    protected int mMinIndex = 0; // <= 0
    protected int mMaxIndex = 1; // > 0

    protected int mPeriod = 0; // 1..max, 0 - unused value
    protected int mOnce = 0; // min .. max

    private BitSet mSet;

    private boolean mAcceptPeriod = false;
    private boolean mAcceptSet = false;

    protected Activation mActivation = Activation.IGNORED;

    public Param(int minIndex, int maxIndex, boolean acceptPeriod, boolean acceptSet) {
        if (minIndex > 0) minIndex = 0;
        if (maxIndex < 1) maxIndex = 1;
        mMinIndex = minIndex;
        mMaxIndex = maxIndex;
        mAcceptPeriod = acceptPeriod;
        mAcceptSet = acceptSet;

        if (mAcceptSet) mSet = new BitSet();
    }

    public final int minIndex() {
        return mMinIndex;
    }

    public final int maxIndex() {
        return mMaxIndex;
    }

    public final Activation activation() {
        return mActivation;
    }

    public final boolean isIgnored() {
        return mActivation == Activation.IGNORED;
    }

    public final boolean isPeriodic() {
        return mActivation == Activation.PERIODIC;
    }

    public final boolean isOnce() {
        return mActivation == Activation.ONCE;
    }

    public final boolean add() {
        return mActivation == Activation.SET;
    }

    public final int getPeriod() {
        return isPeriodic() ? mPeriod : -1;
    }

    public final int getOnce() {
        return isOnce() ? mOnce : mMinIndex - 1;
    }

    protected final boolean validateIndex(int index) {
        return index >= mMinIndex && index <= mMaxIndex;
    }

    protected int zoneCheck(int[] indexes, int indexCount) {
        return indexCount;
    }

    private final boolean validateOnceValue(int[] indexes, int indexCount) {
        return indexes[0] == mOnce || (indexCount > 1 && indexes[1] == mOnce) ||
                (indexCount > 2 && indexes[2] == mOnce);
    }

    private final boolean validateSetValue(int[] indexes, int indexCount) {
        return mSet.get(indexes[0] - mMinIndex) || (indexCount > 1 && mSet.get(indexes[1] - mMinIndex)) ||
                (indexCount > 2 && mSet.get(indexes[2] - mMinIndex));
    }

    public final boolean validateValue(int index, int count) { // index - test, count - real items count
        if (index < 0 || index >= count || count <= mMaxIndex) return false; // count must be > then mMaxIndex

        if (mActivation == Activation.IGNORED) return true;
        if (mActivation == Activation.PERIODIC) return false;

        // some indexes has mirror negative values

        int[] indexes = {0, 0, 0}; // third index for future purposes (Zone check)
        int indexCount = 0;

        if (validateIndex(index)) indexes[indexCount++] = index;
        if (validateIndex(index - count)) indexes[indexCount++] = index - count;
        indexCount = zoneCheck(indexes, indexCount);

        if (indexCount == 0) return false;

        if (mActivation == Activation.ONCE) {
            return validateOnceValue(indexes, indexCount);
        }

        if (!mAcceptSet) return false;
        return validateSetValue(indexes, indexCount);
    }

    public final boolean setOnce(int index) {
        if (!validateIndex(index)) return false;
        clear();
        mOnce = index;
        mActivation = Activation.ONCE;
        return true;
    }

    public final void clear() { // virtual
        mPeriod = 0;
        mOnce = 0;
        if (mSet != null) mSet.clear();
        mActivation = Activation.IGNORED;
    }

    public final boolean setPeriod(int index) {
        if (!mAcceptPeriod || index <= 0) return false;
        clear();
        mPeriod = index;
        mActivation = Activation.PERIODIC;
        return true;
    }

    public final boolean add(int index) {
        if (!mAcceptSet || !validateIndex(index)) return false;
        if (isIgnored()) return setOnce(index);
        if (mActivation == Activation.ONCE) {
            if (index == mOnce) return true;
            mSet.set(mOnce - mMinIndex);
            mActivation = Activation.SET;
        }

        mSet.set(index - mMinIndex);
        return true;
    }

    public final boolean remove(int index) {
        if (!mAcceptSet || !validateIndex(index)) return false;

        if (mActivation == Activation.ONCE && index == mOnce) {
            clear();
            mActivation = Activation.IGNORED;
            return true;
        }

        if (mSet.get(index - mMinIndex)) {
            mSet.clear(index - mMinIndex);
            int idx = mSet.nextSetBit(0);
            if (idx < 0) {
                clear();
                mActivation = Activation.IGNORED; //
                return true;
            }

            if (idx < mSet.length() && mSet.nextSetBit(idx + 1) < 0) {
                return setOnce(idx - mMinIndex);
            }
        }
        return true;
    }

    //------------------------------------------------------

    public final JSONObject getJSONObject() {
        if (mActivation == Activation.IGNORED) return null;

        JSONObject res = new JSONObject();

        res.put(KEY_TYPE, mActivation.toString());
        switch (mActivation) {
            case PERIODIC:
                res.put(KEY_VALUE, mPeriod);
                break;
            case ONCE:
                res.put(KEY_VALUE, mOnce);
                break;
            case SET:
                JSONArray arr = new JSONArray();
                for (int i = 0; i < mSet.length(); ++i) {
                    if (mSet.get(i)) arr.add(mMinIndex + i);
                }
                res.put(KEY_VALUE, arr);
                break;
            default:
                return null;
        }
        return res;
    }

    private final boolean checkJSONObject(JSONObject obj) {
        return obj != null && !obj.isEmpty();
    }

    public final boolean putJSONObject(JSONObject obj) {
        clear();
        if (!checkJSONObject(obj)) {
            return false;
        }

        Activation newActivation;

        if (!obj.containsKey(KEY_TYPE)) {
            return false; // logging
        }
        try {
            newActivation = Activation.valueOf(obj.getString("type").toUpperCase());
        } catch (Exception e) {
            return false; // logging
        }

        if (newActivation == Activation.IGNORED) {
            return true;
        }

        if (!obj.containsKey(KEY_VALUE)) {
            return false; // logging
        }

        if (newActivation == Activation.PERIODIC) {
            if (!mAcceptPeriod) return false; // logging
            try {
                int value = obj.getInteger(KEY_VALUE);
                if (value < 0 || value > mMaxIndex) return false; // logging
                setPeriod(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        if (newActivation == Activation.ONCE) {
            try {
                int value = obj.getInteger(KEY_VALUE);
                if (!validateIndex(value)) return false; // logging
                setOnce(value);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        if (!mAcceptSet) {
            return false; // logging
        }

        try {
            JSONArray arr = obj.getJSONArray(KEY_VALUE);
            if (arr == null) return false; // logging
            if (arr.isEmpty()) return true;

            for (int i = 0; i < arr.size(); ++i) {
                int index = arr.getInteger(i);
                if (!validateIndex(index)) {
                    clear();
                    return false; // logging
                }
                add(index);
            }
        } catch (Exception e) {
            clear();
            return false; // logging
        }


        return true;
    }

    //------------------------------------------------------

    public final boolean putJSONString(String str) {
        try {
            return putJSONObject((JSONObject) JSON.parse(str));
        } catch (Exception e) {
            // logging
        }
        return false;
    }

    //------------------------------------------------------

    @Override
    public String toString() {
        JSONObject obj = getJSONObject();
        return obj == null ? null : obj.toJSONString();
    }
}
