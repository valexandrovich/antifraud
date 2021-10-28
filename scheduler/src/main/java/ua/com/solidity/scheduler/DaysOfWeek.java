package ua.com.solidity.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class DaysOfWeek {
    public static final int FIRST = 1;
    public static final int SECOND = 2;
    public static final int THIRD = 4;
    public static final int FOURTH = 8;
    public static final int LAST = 16;
    public static final int LAST_BUT_ONE = 32;
    public static final int LAST_BUT_TWO = 64;
    public static final int ALL = FIRST | SECOND | THIRD | FOURTH | LAST | LAST_BUT_ONE | LAST_BUT_TWO;

    @Autowired
    private Logger logger;

    private int[] mDayOfWeekMasks = {0, 0, 0, 0, 0, 0, 0};

    public final boolean add(int index, int weekMask) {
        if (index < 0 || index > 6) return false;
        if (weekMask == 0) return true;
        mDayOfWeekMasks[index] |= weekMask & ALL;
        return true;
    }

    public final boolean set(int index, int weakMask) {
        if (index < 0 || index > 6) return false;
        mDayOfWeekMasks[index] = weakMask & ALL;
        return true;
    }

    public final void clear() {
        for (int i = 0; i < 7; ++i) mDayOfWeekMasks[i] = 0;
    }

    public final boolean ignored() {
        for (int i = 0; i < 7; ++i) if (mDayOfWeekMasks[i] != 0) return false;
        return true;
    }

    protected final int getBitMaskForIndex(int index) {
        if (index > 3 || index < -3) {
            return 0;
        }
        return index >= 0 ? 1 << index : 8 << (-index);
    }

    public final boolean validateValue(int day, int dayOfWeek, int monthDayCount) {
        if (day < 0 || day >= monthDayCount || dayOfWeek < 0 || dayOfWeek > 6) return false;
        int monthFirstDayOfWeek = (dayOfWeek - day + 35) %  7; // magic solution
        int delta = dayOfWeek - monthFirstDayOfWeek;
        int firstDayOfWeekIndex = dayOfWeek >= monthFirstDayOfWeek ? delta : 7 - delta;
        delta = monthDayCount - firstDayOfWeekIndex;


        int count = delta / 7 + (delta % 7 == 0 ? 0 : 1);
        int index = (day - firstDayOfWeekIndex) / 7;

        return ((mDayOfWeekMasks[dayOfWeek] & getBitMaskForIndex(index)) != 0) ||
                ((mDayOfWeekMasks[dayOfWeek] & getBitMaskForIndex(index - count)) != 0);
    }

    public final JSONArray getJSONArray() {
        JSONArray arr = new JSONArray();
        if (ignored()) return arr;

        int count = 7;
        for (int i = 6; i >= 0; --i) {
            if (mDayOfWeekMasks[i] == 0) --count;
            else break;
        }

        for (int i = 0; i < count; ++i) {
            arr.add(mDayOfWeekMasks[i]);
        }

        return arr;
    }

    public final boolean putJSONArray(JSONArray obj) {
        clear();

        if (obj == null) return true;

        int[] newValues = {0, 0, 0, 0, 0, 0, 0};

        int v;

        for (int i = 0; i < obj.size() && i < 7; ++i) {
            try {
                v = obj.getInteger(i) & ALL;
            } catch (Exception e) {
                return false;
            }
            newValues[i] = v;
        }

        mDayOfWeekMasks = newValues;
        return true;
    }

    public final boolean putJSONString(String str) {
        try {
            return putJSONArray((JSONArray) JSON.parse(str));
        } catch (Exception e) {
            // logging
        }
        return false;
    }
}
