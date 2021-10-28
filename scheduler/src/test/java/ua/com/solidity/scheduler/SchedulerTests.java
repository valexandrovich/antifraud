package ua.com.solidity.scheduler;

import ua.com.solidity.scheduler.Schedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SchedulerTests {

    private static class SchedulerTestInfo {
        private String mMsg;
        private String mInfo;
        private LocalDateTime mCheck;
        private LocalDateTime[] mTests;
        private boolean isValid = false;

        public SchedulerTestInfo(String msg, String info, String checkDateTime, String[] testDateTime) {
            mMsg = msg;
            mInfo = info.replace('\'', '"');
            mCheck = LocalDateTime.parse(checkDateTime);

            mTests = new LocalDateTime[testDateTime.length];

            if (mTests.length > 0)

            for (int i = 0; i < testDateTime.length; ++i) {
                mTests[i] = testDateTime[i] == null ? null : LocalDateTime.parse(testDateTime[i]);
            }
        }

        public final void test(Schedule schedule) {

            assertThat(schedule.putJSONString(mInfo)).as(mMsg + "(Parsing JSON)").isTrue();
            LocalDateTime check = mCheck;

            for (int i = 0; i < mTests.length; ++i) {
                LocalDateTime value = schedule.nearest(check, i != 0);
                assertThat(value).as(mMsg + "(" + (i + 1) + ")").isEqualTo(mTests[i]);
                check = value;
            }
        }
    }

    public static final SchedulerTestInfo[] tests ={

            new SchedulerTestInfo("Once at 15:00 test",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'ONCE', 'value': 900}}",
                    "2021-01-01T00:00", new String[]{"2021-01-01T15:00", "2021-01-02T15:00", "2021-01-03T15:00"}),

            new SchedulerTestInfo("Twice at 15:00 and 17:00 test",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'SET', 'value': [900, 1020]}}",
                    "2021-01-01T00:00", new String[]{"2021-01-01T15:00", "2021-01-01T17:00", "2021-01-02T15:00", "2021-01-02T17:00"}),

            new SchedulerTestInfo("Periodic 1 minute test",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'PERIODIC', 'value': 1}}",
                    "2021-01-01T00:00", new String[]{"2021-01-01T00:00", "2021-01-01T00:01", "2021-01-01T00:02"}),

            new SchedulerTestInfo("Periodic (12 hours) test",
                    "{'start' : '2021-01-05T00:00', 'minutes': {'type': 'PERIODIC', 'value': 720}}",
                    "2021-01-01T00:00", new String[]{"2021-01-05T00:00", "2021-01-05T12:00", "2021-01-06T00:00"}),

            new SchedulerTestInfo("Periodic 2 day test",
                    "{'start' : '2021-01-01T00:00', 'days': {'type': 'PERIODIC', 'value': 2}}",
                    "2021-02-01T00:00", new String[]{"2021-02-02T00:00", "2021-02-04T00:00", "2021-02-06T00:00"}),

//            new SchedulerTestInfo("Periodic 3 week at second and fourth days of week at 1:00 and 5:00 test",
//                    "{'start' : '2021-01-01T00:00', " +
//                            "'weeks': {'type': 'PERIODIC', 'value': 3}, 'days_of_week': [0, 127, 0, 127], 'minutes': {'type': 'SET', 'value': [60, 300]}}",
//                    "2021-01-01T00:00", new String[]{
//                            "2021-01-05T01:00", "2021-01-05T05:00",
//                            "2021-01-07T01:00", "2021-01-07T05:00",
//                            "2021-01-26T01:00", "2021-01-26T05:00",
//                            "2021-01-28T01:00", "2021-01-28T05:00",
//                            "2021-02-16T01:00", "2021-02-16T05:00"}),

            new SchedulerTestInfo("Last but one day of month test at 1:00",
                    "{'start' : '2021-01-05T00:00', 'days_of_month': {'type': 'ONCE', 'value': -2}, 'minutes': {'type': 'ONCE', 'value': 60}}",
                    "2021-02-05T01:00", new String[]{"2021-02-27T01:00", "2021-03-30T01:00", "2021-04-29T01:00"}),

            new SchedulerTestInfo("Every last but two saturday at 3:00",
                    "{'start' : '2021-01-01T00:00', 'minutes': {'type': 'ONCE', 'value': 180}, 'days_of_week' : [0, 0, 0, 0, 0, 64]}",
                    "2021-01-01T00:00", new String[]{"2021-01-16T03:00", "2021-02-13T03:00", "2021-03-13T03:00"})
    };

    @Test
    public void schedulerTaskShouldWork() {
/*
        Schedule schedule = new Schedule();
        for (int i = 0; i < tests.length; ++i) {
            tests[i].test(schedule);
        }*/
    }
}
