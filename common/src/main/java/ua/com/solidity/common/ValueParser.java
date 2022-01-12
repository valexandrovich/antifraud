package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class ValueParser {
    private static final String VAL_TRUE = "true";
    private static final String VAL_FALSE = "false";

    private static final String PATTERN_TRUE = "^[Tt][Rr][Uu][Ee]$";
    private static final String PATTERN_FALSE = "^[Ff][Aa][Ll][Ss][Ee]$";
    private static final String PATTERN_NULL = "^[Nn][Uu][Ll][Ll]$";
    private static final String PATTERN_DOUBLE_INVALIDS = "[^.Ee+\\-0-9]+";
    @SuppressWarnings("SpellCheckingInspection")
    public static final DateTimeFormatter dateTimeOutputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'[HH:mm:ss.[SSS[SSS]]][XXX]");
    @SuppressWarnings("SpellCheckingInspection")
    private static final String[] dateTimeFormatExpressions = new String[] {
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.S]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSSS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSSSS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSSSSS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSSSSSS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSSSSSSS]]]][XXX]",
            "yyyy-M-d[[' ']['T'][H:mm[:ss[.SSSSSSSSS]]]][XXX]",

            "d/M/yyyy[[' ']['T'][H:mm[:ss[.S]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSSS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSSSS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSSSSS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSSSSSS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSSSSSSS]]]][XXX]",
            "d/M/yyyy[[' ']['T'][H:mm[:ss[.SSSSSSSSS]]]][XXX]",

            "d.M.yyyy[[' ']['T'][H:mm[:ss[.S]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSSS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSSSS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSSSSS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSSSSSS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSSSSSSS]]]][XXX]",
            "d.M.yyyy[[' ']['T'][H:mm[:ss[.SSSSSSSSS]]]][XXX]"
    };

    private ValueParser() {

    }

    private static ArrayList<DateTimeFormatter> datetimeFormats = null;

    private static void getFormats() {
        if (datetimeFormats == null) {
            datetimeFormats = new ArrayList<>();

            for (String item : dateTimeFormatExpressions) {
                datetimeFormats.add(DateTimeFormatter.ofPattern(item));
            }
        }
    }

    private static LocalDate parseLocalDate(String value, DateTimeFormatter formatter) {
        LocalDate date;
        try {
            date = LocalDate.parse(value, formatter);
            return date;
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime parseLocalTime(String value, DateTimeFormatter formatter) {
        LocalTime time;
        try {
            time = LocalTime.parse(value, formatter);
            return time;
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime parseLocalDateTime(String value, DateTimeFormatter formatter) {
        LocalDateTime datetime;
        try {
            datetime = LocalDateTime.parse(value, formatter);
            return datetime;
        } catch (Exception e) {
            return null;
        }
    }

    private static ZonedDateTime parseZonedDateTime(String value, DateTimeFormatter formatter) {
        ZonedDateTime zonedDateTime;
        try {
            zonedDateTime = ZonedDateTime.parse(value, formatter);
            return zonedDateTime;
        } catch (Exception e) {
            return null;
        }
    }

    public static ZonedDateTime getDatetime(String value) {
        if (value == null) return null;
        LocalDate date;
        LocalTime time;
        LocalDateTime datetime;
        ZonedDateTime zonedDatetime;
        getFormats();
        for (DateTimeFormatter fmt : datetimeFormats) {
            date = parseLocalDate(value, fmt);
            time = parseLocalTime(value, fmt);
            datetime = parseLocalDateTime(value, fmt);
            zonedDatetime = parseZonedDateTime(value, fmt);

            if (zonedDatetime != null) {
                return zonedDatetime;
            }

            ZoneId zone = ZoneId.systemDefault();
            if (datetime != null) {
                return ZonedDateTime.of(datetime, zone);
            }

            if (date != null) {
                return time != null ? ZonedDateTime.of(date, time, zone) : ZonedDateTime.of(date, LocalTime.of(0, 0), zone);
            }
        }
        return null;
    }

    public static boolean isNull(String value) {
        return value.matches(PATTERN_NULL);
    }

    public static Object getBoolean(String value) {
        if (Objects.equals(value.replace(PATTERN_TRUE, VAL_TRUE), VAL_TRUE)) return true;
        if (Objects.equals(value.replace(PATTERN_FALSE, VAL_FALSE), VAL_FALSE)) return false;
        return null;
    }

    public static Object getInteger(String value) {
        long v;
        try {
            v = Long.parseLong(value);
            return v;
        } catch(Exception e)  {
            // nothing
        }
        return null;
    }

    public static Object getFloat(String value) {
        String v = value;
        double res;
        boolean hasDot = v.indexOf('.') >= 0;

        if (!hasDot) v = v.replace(',', '.');

        v = v.replaceAll(PATTERN_DOUBLE_INVALIDS, "");

        try {
            res = Double.parseDouble(v);
            return res;
        } catch (Exception e) {
            // nothing
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static JsonNode parse(String value) {
        if (value == null || isNull(value)) return null;
        Object obj;
        if ((obj = getDatetime(value)) != null) {
            return JsonNodeFactory.instance.textNode(formatZonedDateTime((ZonedDateTime) obj));
        } else if ((obj = getBoolean(value)) != null) {
            return JsonNodeFactory.instance.booleanNode((Boolean) obj);
        } else if ((obj = getFloat(value)) != null) {
            return JsonNodeFactory.instance.numberNode((Double) obj);
        } else if ((obj = getInteger(value)) != null) {
            return JsonNodeFactory.instance.numberNode((long) obj);
        }
        return JsonNodeFactory.instance.textNode(value);
    }

    public static String formatZonedDateTime(ZonedDateTime datetime) {
		return datetime.format(dateTimeOutputFormat);
    }

    public static String formatInstant(Instant instant) {
        return dateTimeOutputFormat.format(instant);
    }

    public static String formatLocalDateTime(LocalDateTime datetime, ZoneOffset offset) {
        return formatInstant(datetime.toInstant(offset));
    }
}
