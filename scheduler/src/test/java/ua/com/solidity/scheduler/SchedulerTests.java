package ua.com.solidity.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerTests {
    private static final Locale localeIE = new Locale("en_IE");
    private static class SchedulerTestInfo {
        private final String mMsg;
        private final String mInfo;
        private final LocalDateTime mCheck;
        private final LocalDateTime[] mTests;
        private final Locale locale;

        public SchedulerTestInfo(Locale locale, String msg, String info, String checkDateTime, String[] testDateTime) {
            this.locale = locale;
            mMsg = msg;
            mInfo = info.replace('\'', '"');
            mCheck = LocalDateTime.parse(checkDateTime);

            mTests = new LocalDateTime[testDateTime.length];

            if (mTests.length > 0) {
                for (int i = 0; i < testDateTime.length; ++i) {
                    mTests[i] = testDateTime[i] == null ? null : LocalDateTime.parse(testDateTime[i]);
                }
            }
        }

        public final void test(Schedule schedule) {
            assertThat(schedule.putJSONString(mInfo)).as(mMsg + "(Parsing JSON)").isTrue();
            LocalDateTime check = mCheck;

            try {
                schedule.setLocale(locale);
                for (int i = 0; i < mTests.length; ++i) {
                    LocalDateTime value = schedule.nearest(check, i != 0);
                    assertThat(value).as(mMsg + "(" + (i + 1) + ")").isEqualTo(mTests[i]);
                    check = value;
                }
            } finally {
                schedule.setLocale(null);
            }
        }
    }

    public static final SchedulerTestInfo[] tests ={

            new SchedulerTestInfo(localeIE, "Once at 15:00 test",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'ONCE', 'value': '15:00'}}",
                    "2021-01-01T00:00", new String[]{"2021-01-01T15:00", "2021-01-02T15:00", "2021-01-03T15:00"}),

            new SchedulerTestInfo(localeIE, "Twice at 15:00 and 17:00 test",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'SET', 'value': ['15:00', '17:00']}}",
                    "2021-01-01T00:00", new String[]{"2021-01-01T15:00", "2021-01-01T17:00", "2021-01-02T15:00", "2021-01-02T17:00"}),

            new SchedulerTestInfo(localeIE, "Periodic 1 minute test",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'PERIODIC', 'value': 1}}",
                    "2021-01-01T00:00", new String[]{"2021-01-01T00:00", "2021-01-01T00:01", "2021-01-01T00:02"}),

            new SchedulerTestInfo(localeIE,"Periodic (12 hours) test",
                    "{'start' : '2021-01-05T00:00', 'minutes': {'type': 'PERIODIC', 'value': '12h'}}",
                    "2021-01-01T00:00", new String[]{"2021-01-05T00:00", "2021-01-05T12:00", "2021-01-06T00:00"}),

            new SchedulerTestInfo(localeIE, "Periodic 2 day test",
                    "{'start' : '2021-01-01T00:00', 'days': {'type': 'PERIODIC', 'value': 2}}",
                    "2021-02-01T00:00", new String[]{"2021-02-02T00:00", "2021-02-04T00:00", "2021-02-06T00:00"}),

            new SchedulerTestInfo(localeIE,"Periodic 3 week at tuesday and thursday at 1:00 and 5:00 test",
                    "{'start' : '2021-01-01T00:00', " +
                            "'weeks': {'type': 'PERIODIC', 'value': 3}, 'days_of_week': {'tuesday': 'all', 'thursday': 'all'}, 'minutes': {'type': 'SET', 'value': ['1:00', '5:00']}}",
                    "2021-01-01T00:00", new String[]{
                            "2021-01-19T01:00", "2021-01-19T05:00",
                            "2021-01-21T01:00", "2021-01-21T05:00",
                            "2021-02-09T01:00", "2021-02-09T05:00",
                            "2021-02-11T01:00", "2021-02-11T05:00",
                            "2021-03-02T01:00", "2021-03-02T05:00",
                            "2021-03-04T01:00", "2021-03-04T05:00"}),

            new SchedulerTestInfo(localeIE,"Last but one day of month test at 1:00",
                    "{'start' : '2021-01-05T00:00', 'days_of_month': {'type': 'ONCE', 'value': -2}, 'minutes': {'type': 'ONCE', 'value': '1:00'}}",
                    "2021-02-05T01:00", new String[]{"2021-02-27T01:00", "2021-03-30T01:00", "2021-04-29T01:00"}),

            new SchedulerTestInfo(localeIE, "Every last but two saturday at 3:00",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'ONCE', 'value': '3:00'}, 'days_of_week' : {'saturday': [-3]}}",
                    "2021-01-01T00:00", new String[]{"2021-01-16T03:00", "2021-02-13T03:00", "2021-03-13T03:00"})
    };

    @Test
    void schedulerTaskShouldWork() {
        Schedule schedule = new Schedule();
        for (SchedulerTestInfo test : tests) {
            test.test(schedule);
        }
    }
}
