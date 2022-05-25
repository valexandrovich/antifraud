package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;


@CustomLog
@Getter
@Setter
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
    public final DaysSet daysOfYear = new DaysSet();
    public final DaysOfMonthSet daysOfMonth = new DaysOfMonthSet();
    public final MonthsSet months = new MonthsSet();
    public final WeeksSet weeks = new WeeksSet();
    public final DaysOfWeek daysOfWeek = new DaysOfWeek();

    private LocalDateTime mStart;
    private LocalDateTime mFinish;

    private Locale locale = Locale.getDefault();
    private WeekFields mWeekFields = WeekFields.of(locale);

    public Schedule() {
        mStart = LocalDateTime.now();
        mFinish = null;
    }

    public final void setLocale(Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        if (locale == this.locale) return;
        this.locale = locale;
        mWeekFields = WeekFields.of(locale);
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

    public final void assignStartIsInvalid(String start) {
        try {
            setStart(LocalDateTime.parse(start));
        } catch (Exception e) {
            log.warn("Schedule can't parse a start date: {}", start, e);
            throw e;
        }
    }

    public final void setStart(LocalDateTime start) {
        mStart = start == null ? LocalDateTime.now() : start.truncatedTo(ChronoUnit.MINUTES);
    }

    public final void assignFinish(String finish) {
        try{
            if (finish == null || finish.isEmpty()) setFinish(null);
            else setFinish(LocalDateTime.parse(finish));
        } catch (Exception e) {
            log.warn("Schedule can't parse a finish date: {}", finish, e);
            throw e;
        }
    }

    public final void setFinish(LocalDateTime finish) {
        mFinish = finish == null ? null : finish.truncatedTo(ChronoUnit.MINUTES);
    }

    private LocalDateTime getWeekStart(LocalDateTime datetime) {
        return datetime.minus((datetime.getDayOfWeek().getValue() -
                mWeekFields.getFirstDayOfWeek().getValue() + 7) % 7, ChronoUnit.DAYS);
    }

    private static class State {
        private final LocalDateTime start;
        private final LocalDateTime weekStart;
        private LocalDateTime datetime;
        private LocalDateTime dateWeekStart;
        private long deltaMinutes;
        private long deltaDays;

        public State(LocalDateTime start, LocalDateTime weekStart, LocalDateTime datetime) {
            this.start = start;
            this.weekStart = weekStart;
            this.datetime = datetime;
        }

        public final void newIteration(LocalDateTime dateWeekStart) {
            deltaMinutes = (datetime.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC)) / 60;
            deltaDays = deltaMinutes / 1440;
            this.dateWeekStart = dateWeekStart;
        }
    }

    private boolean doMonthCheck(State state) {
        if (months.isIgnored()) return true;
        if (months.isPeriodic()) {
            long offset = (state.datetime.getMonthValue() + 12 - mStart.getMonthValue()) % months.getPeriod();
            if (offset != 0) {
                state.datetime = state.datetime.plus(months.getPeriod() - offset, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.MONTHS);
                return false;
            }
        } else {
            while (months.invalidValue(state.datetime.get(ChronoField.MONTH_OF_YEAR) - 1, 12)) {
                state.datetime = state.datetime.plus(1, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.MONTHS);
            }
        }
        return true;
    }

    private boolean doWeekCheck(State state) {
        if (weeks.isIgnored()) return true;
        if (weeks.isPeriodic()) {
            long deltaWeeks = (state.dateWeekStart.toEpochSecond(ZoneOffset.UTC) - state.weekStart.toEpochSecond(ZoneOffset.UTC)) / 60 / 1440 / 7;
            long offset = deltaWeeks % weeks.getPeriod();
            if (offset != 0) {
                state.datetime = state.dateWeekStart.plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS); // shift to next week
                return false;
            }
        } else {
            if (weeks.invalidValue(state.datetime.get(mWeekFields.weekOfWeekBasedYear()) -
                            (int) state.datetime.range(mWeekFields.weekOfWeekBasedYear()).getMinimum(),
                    (int) state.datetime.range(mWeekFields.weekOfWeekBasedYear()).getMaximum())) {
                state.datetime = state.dateWeekStart.plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                return false;
            }
        }
        return true;
    }

    private boolean doDayOfYearCheck(State state) {
        if (daysOfYear.isIgnored()) return true;
        if (daysOfYear.isPeriodic()) {
            long offset = state.deltaDays % daysOfYear.getPeriod();
            if (offset != 0) {
                state.datetime = state.datetime.plus(daysOfYear.getPeriod() - offset, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                return false;
            }
        } else {
            int count = (int) state.datetime.range(ChronoField.DAY_OF_YEAR).getMaximum();
            if (daysOfYear.invalidValue(state.datetime.getDayOfYear() - 1, count)) {
                state.datetime = state.datetime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
                return false;
            }
        }
        return true;
    }

    private boolean doDayOfMonthCheck(State state) {
        if (daysOfMonth.isIgnored()) return true;

        int count = (int) state.datetime.range(ChronoField.DAY_OF_MONTH).getMaximum();
        if (daysOfMonth.invalidValue(state.datetime.getDayOfMonth() - 1, count)) {
            state.datetime = state.datetime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            return false;
        }
        return true;
    }

    private boolean doDayOfWeekCheck(State state) {
        if (daysOfWeek.isIgnored()) return true;
        if (!daysOfWeek.validateValue(state.datetime.getDayOfMonth() - 1, state.datetime.getDayOfWeek().getValue() - 1,
                (int) state.datetime.range(ChronoField.DAY_OF_MONTH).getMaximum())) {
            state.datetime = state.datetime.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            return false;
        }
        return true;
    }

    private boolean doMinuteCheck(State state) {
        if (minutes.isIgnored()) {
            minutes.setOnce(0);
        }
        if (minutes.isPeriodic()) {
            long offset = state.deltaMinutes % minutes.getPeriod();
            int day = state.datetime.getDayOfMonth();
            if (offset != 0) {
                state.datetime = state.datetime.plus(minutes.getPeriod() - offset, ChronoUnit.MINUTES);
                return state.datetime.getDayOfMonth() == day;
            }
        } else {
            int minuteBase = state.datetime.get(ChronoField.MINUTE_OF_DAY);
            int minute = minuteBase;
            boolean found = false;
            while (minute < 1440) {
                if (minutes.invalidValue(minute, 1440)) {
                    ++minute;
                } else {
                    found = true;
                    break;
                }
            }
            if (!found) {
                state.datetime = state.datetime.plus(1440L - minuteBase, ChronoUnit.MINUTES);
                return false;
            }
            state.datetime = state.datetime.plus((long) minute - minuteBase, ChronoUnit.MINUTES);
        }
        return true;
    }

    public final LocalDateTime nearest(LocalDateTime datetime, boolean after) { // return
        datetime = datetime.truncatedTo(ChronoUnit.MINUTES);
        if (after) datetime = datetime.plus(1, ChronoUnit.MINUTES);  // step forward

        if (mFinish != null && datetime.compareTo(mFinish) > 0) return null;
        if (datetime.compareTo(mStart) < 0) datetime = mStart;
        int dayCount = 0;
        LocalDateTime res = null;
        State state = new State(mStart, getWeekStart(mStart), datetime);

        while(dayCount < 367) {  // infinite loop protection
            ++dayCount;
            state.newIteration(getWeekStart(state.datetime));
            if (doMonthCheck(state) && doWeekCheck(state) && doDayOfYearCheck(state) &&
                    doDayOfMonthCheck(state) && doDayOfWeekCheck(state) && doMinuteCheck(state)) {
                res = state.datetime;
                break;
            }
        }
        return res;
    }

    private void setValueToObjectNode(ObjectNode node, String name, CustomParam param) {
        if (param != null && !param.isIgnored()) node.set(name, param.getNode());
    }

    public final JsonNode getNode() {
        ObjectNode res = JsonNodeFactory.instance.objectNode();
        res.put(FIELD_START, mStart.toString());
        if (mFinish != null) res.put(FIELD_FINISH, mFinish.toString());
        setValueToObjectNode(res, FIELD_MONTHS, months);
        setValueToObjectNode(res, FIELD_WEEKS, weeks);
        setValueToObjectNode(res, FIELD_DAYS, daysOfYear);
        setValueToObjectNode(res, FIELD_DAYS_OF_MONTH, daysOfMonth);
        setValueToObjectNode(res, FIELD_DAYS_OF_WEEK, daysOfWeek);
        setValueToObjectNode(res, FIELD_MINUTES, minutes);
        return res;
    }

    private void setObjectFieldByNode(JsonNode node, String fieldName, CustomParam target) {
        if (node.hasNonNull(fieldName)) {
            target.setNode(node.get(fieldName));
        }
    }

    public final boolean assignNode(JsonNode obj) {
        clear();
        if (obj == null || obj.isEmpty() || !obj.isObject() || !obj.hasNonNull(FIELD_START)) return false;
        try {
            assignStartIsInvalid(obj.get(FIELD_START).asText());
            assignFinish(obj.hasNonNull(FIELD_FINISH) ? obj.get(FIELD_FINISH).asText() : null);
            setObjectFieldByNode(obj, FIELD_MONTHS, months);
            setObjectFieldByNode(obj, FIELD_WEEKS, weeks);
            setObjectFieldByNode(obj, FIELD_DAYS, daysOfYear);
            setObjectFieldByNode(obj, FIELD_DAYS_OF_MONTH, daysOfMonth);
            setObjectFieldByNode(obj, FIELD_DAYS_OF_WEEK, daysOfWeek);
            setObjectFieldByNode(obj, FIELD_MINUTES, minutes);
        } catch (Exception e) {
            log.warn("Error parsing Schedule.", e);
            return false;
        }
        return true;
    }

    public final boolean putJSONString(String str) {
        try {
            JsonNode node = new ObjectMapper().readTree(str);
            if (node == null || !node.isObject()) {
                log.warn("JSON schedule node must be an object. {}", str);
            } else return assignNode(node);
        } catch (Exception e) {
            log.warn("JSON parse error for Schedule", e);
        }
        return false;
    }

    @Override
    public String toString() {
        return getNode().toPrettyString();
    }
}
