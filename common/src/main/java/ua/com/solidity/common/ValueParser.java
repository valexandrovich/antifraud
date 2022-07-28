package ua.com.solidity.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class ValueParser {
    private static final String VAL_TRUE = "true";
    private static final String VAL_FALSE = "false";

    public static final String PATTERN_TRUE = "^([Tt][Rr][Uu][Ee])|([Yy][Ee][Ss])$";
    public static final String PATTERN_FALSE = "^([Ff][Aa][Ll][Ss][Ee])|([Nn][Oo])$";
    public static final String PATTERN_NULL = "^[Nn][Uu][Ll][Ll]$";
    public static final String PATTERN_DOUBLE_INVALIDS = "[^.Ee+\\-\\d]+"; // old value: "[^.Ee+\\-0-9]+"

    public static final DateTimeFormatter zonedDateTimeOutputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
    public static final DateTimeFormatter dateTimeOutputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    public static final DateTimeFormatter dateOutputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter zonedDateOutputFormat = DateTimeFormatter.ofPattern("yyyy-MM-ddZ");
    public static final DateTimeFormatter timeOutputFormat = DateTimeFormatter.ofPattern("hh:mm:ss.SSSSSS");

    private static final String[] datePrefixes = new String[] {
            "yyyy-M-d",
            "d/M/yyyy",
            "d.M.yyyy"
    };

    private static final String TIME_FORMAT = "H:mm[:ss[.n]]";
    private static final String TIME_ZONE_SUFFIX = "[XXX]";
    private static final String DATE_TIME_SUFFIX = "[' ']['T'][" + TIME_FORMAT + "]";
    private static final String ZONED_DATE_TIME_SUFFIX = DATE_TIME_SUFFIX + TIME_ZONE_SUFFIX;

    private static final FloatObjectList dateTimeFormats = new FloatObjectList();
    private static final FloatObjectList zonedDateTimeFormats = new FloatObjectList();
    private static final FloatObjectList dateFormats = new FloatObjectList();
    private static final FloatObjectList zonedDateFormats = new FloatObjectList();

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(TIME_FORMAT);

    private static boolean formatsAssigned = false;

    private ValueParser() {
        // nothing
    }

    private static void getFormats() {
        if (!formatsAssigned) {
            for (String prefix : datePrefixes) {
                dateFormats.add(DateTimeFormatter.ofPattern(prefix));
                dateTimeFormats.add(DateTimeFormatter.ofPattern(prefix + DATE_TIME_SUFFIX));
                zonedDateTimeFormats.add(DateTimeFormatter.ofPattern(prefix + ZONED_DATE_TIME_SUFFIX));
                zonedDateFormats.add(prefix + TIME_ZONE_SUFFIX);
            }
            formatsAssigned = true;
        }
    }

    public static ZonedDateTime getZonedDateTime(String value) {
        getFormats();
        ZonedDateTime zonedDateTime = internalGetZonedDateTime(value);
        if (zonedDateTime == null) {
            LocalDateTime datetime = internalGetLocalDateTime(value);
            if (datetime != null) {
                return ZonedDateTime.of(datetime, ZoneOffset.UTC);
            }
        }
        return zonedDateTime;
    }

    private static ZonedDateTime internalGetZonedDateTime(String value) {
        FloatObjectListItem root = zonedDateTimeFormats.root;
        FloatObjectListItem item = root;
        if (item != null) {
            do {
                try {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, item.get(DateTimeFormatter.class));
                    if (zonedDateTime != null) {
                        item.floatUp();
                        return zonedDateTime;
                    }
                } catch (Exception e) {
                    // nothing
                }
                item = item.next;
            } while (item != root);
        }
        return null;
    }

    private static LocalDateTime internalGetLocalDateTime(String value) {
        FloatObjectListItem root = dateTimeFormats.root;
        FloatObjectListItem item = root;
        if (item != null) {
            do {
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(value, item.get(DateTimeFormatter.class));
                    item.floatUp();
                    return localDateTime;
                } catch (Exception e) {
                    // nothing
                }
                item = item.next;
            } while (item != root);
        }
        return null;
    }

    public static LocalDateTime getLocalDateTime(String value) {
        getFormats();
        LocalDateTime localDateTime = internalGetLocalDateTime(value);
        if (localDateTime == null) {
            ZonedDateTime zonedDateTime = internalGetZonedDateTime(value);
            if (zonedDateTime != null) {
                return zonedDateTime.toLocalDateTime();
            }
        }
        return localDateTime;
    }

    public static LocalDate getLocalDate(String value) {
        getFormats();
        FloatObjectListItem root = dateFormats.root;
        FloatObjectListItem item = root;
        if (item != null) {
            do {
                try {
                    LocalDate localDate = LocalDate.parse(value, item.get(DateTimeFormatter.class));
                    if (localDate != null) {
                        item.floatUp();
                        return localDate;
                    }
                } catch (Exception e) {
                    // nothing
                }
                item = item.next;
            } while (item != root);
        }

        LocalDateTime datetime = getLocalDateTime(value);
        if (datetime != null) {
            return datetime.toLocalDate();
        }
        return null;
    }

    public static ZonedDateTime getZonedDate(String value) {
        getFormats();
        FloatObjectListItem root = zonedDateFormats.root;
        FloatObjectListItem item = root;
        if (item != null) {
            do {
                try {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, item.get(DateTimeFormatter.class));
                    if (zonedDateTime != null) {
                        item.floatUp();
                        return zonedDateTime.truncatedTo(ChronoUnit.DAYS);
                    }
                } catch (Exception e) {
                    // nothing
                }
                item = item.next;
            } while (item != root);
        }

        ZonedDateTime datetime = internalGetZonedDateTime(value);
        if (datetime != null) {
            return datetime.truncatedTo(ChronoUnit.DAYS);
        }
        return null;
    }

    public static LocalTime getTime(String value) {
        getFormats();
        LocalTime time;
        try {
            time = LocalTime.parse(value, timeFormat);
            return time;
        } catch (Exception e) {
            // nothing
        }
        return null;
    }

    public static boolean isNull(String value) {
        return value.matches(PATTERN_NULL);
    }

    public static Object getBoolean(String value) {
        return getBoolean(value, PATTERN_TRUE, PATTERN_FALSE);
    }

    public static Object getBoolean(String value, String truePattern, String falsePattern) {
        if (truePattern != null && Objects.equals(value.replace(truePattern, VAL_TRUE), VAL_TRUE)) return true;
        if (falsePattern != null && Objects.equals(value.replace(falsePattern, VAL_FALSE), VAL_FALSE)) return false;
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
        v = v.replace(',', '.');
        v = v.replaceAll(PATTERN_DOUBLE_INVALIDS, "");

        try {
            res = Double.parseDouble(v);
            return res;
        } catch (Exception e) {
            // nothing
        }
        return null;
    }

    private static Object getTemporal(String value) {
        Object obj;
        obj = getTime(value);
        if (obj == null) {
            obj = getZonedDateTime(value);
            if (obj == null) {
                obj = getLocalDateTime(value);
                if (obj == null) {
                    obj = getLocalDate(value);
                }
            }
        }
        return obj;
    }

    @SuppressWarnings("unused")
    public static JsonNode parse(String value) {
        if (value == null || isNull(value)) return null;
        Object obj;
        if ((obj = getBoolean(value)) != null) {
            return JsonNodeFactory.instance.booleanNode((Boolean) obj);
        } else if ((obj = getFloat(value)) != null) {
            return JsonNodeFactory.instance.numberNode((Double) obj);
        } else if ((obj = getInteger(value)) != null) {
            return JsonNodeFactory.instance.numberNode((long) obj);
        } else {
            obj = getTemporal(value);
            if (obj != null) {
                value = obj.toString();
            }
        }
        return JsonNodeFactory.instance.textNode(value);
    }

    public static String formatZonedDateTime(ZonedDateTime datetime) {
		return zonedDateTimeOutputFormat.format(datetime);
    }

    public static String formatInstant(Instant instant) {
        return dateTimeOutputFormat.format(instant);
    }

    public static String formatLocalDateTime(LocalDateTime datetime) {
        return dateTimeOutputFormat.format(datetime);
    }

    public static String formatLocalDate(LocalDate date) {
        return date.format(dateOutputFormat);
    }

    public static String formatZonedDate(ZonedDateTime datetime) {
        return datetime.format(zonedDateOutputFormat);
    }

    @SuppressWarnings("unused")
    public static String formatTime(LocalTime time) {
        return time.format(timeOutputFormat);
    }
}
