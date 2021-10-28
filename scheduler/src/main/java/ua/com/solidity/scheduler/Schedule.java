package ua.com.solidity.scheduler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Schedule {
    public static final String FIELD_START = "start";
    public static final String FIELD_FINISH = "finish";
    public static final String FIELD_MONTHS = "months";
    public static final String FIELD_WEEKS = "weeks";
    public static final String FIELD_DAYS = "days";
    public static final String FIELD_DAYS_OF_MONTH = "days_of_month";
    public static final String FIELD_DAYS_OF_WEEK = "days_of_week";
    public static final String FIELD_MINUTES = "minutes";

    public final MinutesSet minutes = new MinutesSet();
    public final DaysSet daysOfYear = new DaysSet(); // used as OR operator with DaysOfMonthSet
    public final DaysOfMonthSet daysOfMonth = new DaysOfMonthSet();
    public final MonthsSet months = new MonthsSet();
    public final WeeksSet weeks = new WeeksSet(); // Used as AND operator with DaysOfWeek
    public final DaysOfWeek daysOfWeek = new DaysOfWeek();

    private LocalDateTime mStart;
    private LocalDateTime mFinish;

    private static WeekFields mWeekFields = WeekFields.of(Locale.getDefault());

    private static class Check {
        public LocalDateTime datetime;
        public LocalDateTime result;

        public Check(LocalDateTime aDatetime, boolean greatest) {
            datetime = datetime.plus(greatest ? 1 : 0, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
        }

        public boolean completed() {
            return result != null;
        }

        public void alignToDay() {
            datetime.truncatedTo(ChronoUnit.DAYS);
        }

        public void complete() {
            result = datetime;
        }
    }

    public Schedule() {
        mStart = LocalDateTime.now();
        mFinish = null;
    }

    public static final void setWeekFields(WeekFields fields) {
        if (fields != null) mWeekFields = fields;
    }

    public final void clear() {
        mStart = LocalDateTime.now();
        mFinish = null;
        minutes.clear();
        daysOfYear.clear();
        months.clear();
        daysOfMonth.clear();
        weeks.clear();
        daysOfWeek.clear();
    }

    public final boolean setStart(String start) {
        try {
            setStart(LocalDateTime.parse(start));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public final void setStart(LocalDateTime start) {
        mStart = start == null ? LocalDateTime.now() : start.truncatedTo(ChronoUnit.MINUTES);
    }

    public final LocalDateTime getStart() {
        return mStart;
    }

    public final boolean setFinish(String finish) {
        try {
            if (finish == null) setFinish((LocalDateTime) null);
            else setFinish(LocalDateTime.parse(finish));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public final void setFinish(LocalDateTime finish) {
        mFinish = finish == null ? null : finish.truncatedTo(ChronoUnit.MINUTES);
    }

    public final LocalDateTime getFinish() {
        return mFinish;
    }

    public final LocalDateTime nearest(LocalDateTime datetime, boolean after) { // return
        datetime = datetime.truncatedTo(ChronoUnit.MINUTES);
        if (after) datetime = datetime.plus(1, ChronoUnit.MINUTES);  // step forward

        if (mFinish != null && datetime.compareTo(mFinish) > 0) return null;

        if (datetime.compareTo(mStart) < 0) datetime = mStart;

        long offset = 0;
        long value = 0;
        int count = 0;
        int dayCount = 0;
        LocalDateTime res = null;

        while(dayCount < 367) {  // infinite loop protection
            ++dayCount;
            long deltaMinutes = (datetime.toEpochSecond(ZoneOffset.UTC) - mStart.toEpochSecond(ZoneOffset.UTC)) / 60;
            long deltaDays = deltaMinutes / 1440;

            // ---------- MONTH CHECK

            if (!months.isIgnored()) {
                if (months.isPeriodic()) {
                    offset = (datetime.getMonthValue() + 12 - mStart.getMonthValue()) % months.getPeriod();
                    if (offset != 0) {
                        datetime = datetime.plus(months.getPeriod() - offset, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.MONTHS);
                        continue;
                    }
                } else {
                    while (!months.validateValue(datetime.get(ChronoField.MONTH_OF_YEAR) - 1, 12)) {
                        datetime = datetime.plus(1, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.MONTHS);
                    }
                }
            }

            // ---------- WEEK CHECK

            if (!weeks.isIgnored()) {
                if (weeks.isPeriodic()) {
                    long deltaWeeks = deltaDays / 7;
                    offset = deltaWeeks % weeks.getPeriod();
                    if (offset != 0) {
                        datetime = datetime.plus(weeks.getPeriod() - offset, ChronoUnit.WEEKS);
                        // align to first day of week
                        value = (datetime.get(mWeekFields.dayOfWeek()) - mWeekFields.getFirstDayOfWeek().getValue() + 7) % 7;
                        datetime = datetime.plus(7 - value, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                        continue;
                    }
                } else {
                    if (!weeks.validateValue(datetime.get(mWeekFields.weekOfWeekBasedYear()) - 1,
                            (int) datetime.range(mWeekFields.weekOfWeekBasedYear()).getMaximum())) {
                        // align to first day of week
                        value = (datetime.get(mWeekFields.dayOfWeek()) - mWeekFields.getFirstDayOfWeek().getValue() + 7) % 7;
                        datetime = datetime.plus(7 - value, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                        continue;
                    }
                }
            }

            // ---------- DAY OF YEAR CHECK

            if (!daysOfYear.isIgnored()) {
                if (daysOfYear.isPeriodic()) {
                    offset = deltaDays % daysOfYear.getPeriod();
                    if (offset != 0) {
                        datetime = datetime.plus(daysOfYear.getPeriod() - offset, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                        continue;
                    }
                } else {
                    count = (int) datetime.range(ChronoField.DAY_OF_YEAR).getMaximum();
                    if (!daysOfYear.validateValue(datetime.getDayOfYear() - 1, count)) {
                        datetime = datetime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                        continue;
                    }
                }
            }

            // ---------- DAY OF MONTH CHECK

            if (!daysOfMonth.isIgnored()) {
                count = (int) datetime.range(ChronoField.DAY_OF_MONTH).getMaximum();
                if (!daysOfMonth.validateValue(datetime.getDayOfMonth() - 1, count)) {
                    datetime = datetime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                    continue;
                }
            }

            // ---------- DAY OF WEEK CHECK

            if (!daysOfWeek.ignored()) {
                int dayOfWeek = (datetime.get(mWeekFields.dayOfWeek()) - mWeekFields.getFirstDayOfWeek().getValue() + 7) % 7;
                if (!daysOfWeek.validateValue(datetime.getDayOfMonth() - 1, dayOfWeek, (int) datetime.range(ChronoField.DAY_OF_MONTH).getMaximum())) {
                    datetime = datetime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                    continue;
                }
            }

            // ---------- MINUTES CHECK

            if (minutes.isIgnored()) {
                minutes.setOnce(0);
            }

            if (minutes.isPeriodic()) {
                offset = deltaMinutes % minutes.getPeriod();
                int day = datetime.getDayOfMonth();
                if (offset != 0) {
                    datetime = datetime.plus(minutes.getPeriod() - offset, ChronoUnit.MINUTES);
                    if (datetime.getDayOfMonth() != day) continue; // other day
                } else {
                    res = datetime;
                    break;
                }
            } else {
                int minuteBase = datetime.get(ChronoField.MINUTE_OF_DAY);
                int minute = minuteBase;
                boolean found = false;
                while (minute < 1440) {
                    if (!minutes.validateValue(minute, 1440)) {
                        ++minute;
                    } else {
                        found = true;
                        res = datetime;
                        break;
                    }
                }

                if (!found) {
                    datetime = datetime.plus(1440L - minuteBase, ChronoUnit.MINUTES);
                    continue;
                }

                res = datetime = datetime.plus((long) minute - minuteBase, ChronoUnit.MINUTES);
                break;
            }
        }

        return res;
    }

    public final JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        obj.put(FIELD_START, mStart.toString());
        if (mFinish != null) obj.put(FIELD_FINISH, mFinish.toString());
        if (!months.isIgnored()) obj.put(FIELD_MONTHS, months.getJSONObject());
        if (!weeks.isIgnored()) obj.put(FIELD_WEEKS, weeks.getJSONObject());
        if (!daysOfYear.isIgnored()) obj.put(FIELD_DAYS, daysOfYear.getJSONObject());
        if (!daysOfMonth.isIgnored()) obj.put(FIELD_DAYS_OF_MONTH, daysOfMonth.getJSONObject());
        if (!daysOfWeek.ignored()) obj.put(FIELD_DAYS_OF_WEEK, daysOfWeek.getJSONArray());
        if (!minutes.isIgnored()) obj.put(FIELD_MINUTES, minutes.getJSONObject());
        return obj;
    }

    public final boolean putJSONObject(JSONObject obj) {
        if (obj == null || obj.isEmpty() || !obj.containsKey(FIELD_START)) return false;
        clear();

        if (!setStart(obj.getString(FIELD_START))) return false;
        if (obj.containsKey(FIELD_FINISH) && !setFinish(obj.getString(FIELD_FINISH))) return false;

        if (obj.containsKey(FIELD_MONTHS) && !months.putJSONObject(obj.getJSONObject(FIELD_MONTHS))) return false;
        if (obj.containsKey(FIELD_WEEKS) && !weeks.putJSONObject(obj.getJSONObject(FIELD_WEEKS))) return false;
        if (obj.containsKey(FIELD_DAYS) && !daysOfYear.putJSONObject(obj.getJSONObject(FIELD_DAYS))) return false;
        if (obj.containsKey(FIELD_DAYS_OF_MONTH) && !daysOfMonth.putJSONObject(obj.getJSONObject(FIELD_DAYS_OF_MONTH))) return false;
        if (obj.containsKey(FIELD_DAYS_OF_WEEK) && !daysOfWeek.putJSONArray(obj.getJSONArray(FIELD_DAYS_OF_WEEK))) return false;
        return !(obj.containsKey(FIELD_MINUTES) && !minutes.putJSONObject(obj.getJSONObject(FIELD_MINUTES)));
    }

    public final boolean putJSONString(String str) {
        try {
            return putJSONObject((JSONObject) JSON.parse(str));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return getJSONObject().toJSONString();
    }
}
