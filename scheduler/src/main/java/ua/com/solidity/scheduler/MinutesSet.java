package ua.com.solidity.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.CustomLog;

@CustomLog
public class MinutesSet extends Param {
    @Override
    protected void initialize() {
        doInitialize(0, 1439, true);
    }

    private int parseIndex(String value) {
        int delimiterIndex = value.indexOf(":");

        try {
            if (delimiterIndex < 0) {
                return Integer.parseInt(value);
            }
            return Integer.parseInt(value.substring(0, delimiterIndex).trim()) * 60 +
                    Integer.parseInt(value.substring(delimiterIndex + 1).trim());
        } catch (Exception e) {
            log.warn("Illegal JSON value passed to minutes.value: {}", value, e);
            throw e;
        }
    }

    private String makeIndex(int index) {
        int minutes = index % 60;
        int hours = index / 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    private String periodToString(int period) {
        int minutes = period % 60;
        int hours = period / 60;

        String minutesString = minutes + "m";

        if (hours > 0) {
            String hoursString = hours + "h";
            if (minutes > 0) {
                return hoursString + " " + minutesString;
            } else return hoursString;
        }
        return minutesString;
    }

    private int stringToPeriod(String aPeriod) {
        String period = aPeriod.toUpperCase();
        try {
            int hourPos = period.indexOf("H");
            int hours = 0;
            int minutes = 0;
            String value;

            if (hourPos > 0) {
                value = period.substring(0, hourPos).trim();
                if (value.length() > 0) {
                    hours = Integer.parseInt(value);
                }
                period = period.substring(hourPos + 1);
            }
            int minuteMarkerPos = period.indexOf("M");
            if (minuteMarkerPos < 0) minuteMarkerPos = period.length();

            value = period.substring(0, minuteMarkerPos);
            if (value.length() > 0) {
                minutes = Integer.parseInt(value);
            }
            return hours * 60  + minutes;
        } catch (Exception e) {
            log.warn("Schedule minute period parse error: {}", aPeriod);
            throw e;
        }
    }

    @Override
    protected JsonNode convertPeriodToNode(int period) {
        return period >= 60 ? JsonNodeFactory.instance.textNode(periodToString(period)) :
                JsonNodeFactory.instance.numberNode(period);
    }

    @Override
    protected int convertNodeToPeriod(JsonNode node) {
        return node.isNumber() ? node.asInt() : stringToPeriod(node.asText());
    }

    @Override
    protected JsonNode convertIndexToNode(int index) {
        return index >= 60 ? JsonNodeFactory.instance.textNode(makeIndex(index)) :
                JsonNodeFactory.instance.numberNode(index);
    }

    @Override
    protected int convertNodeToIndex(JsonNode node) {
        int res;
        if (node.isNumber()) {
            res = node.asInt(getMinIndex() - 1);
        } else {
            res = parseIndex(node.asText());
        }
        return res;
    }
}
