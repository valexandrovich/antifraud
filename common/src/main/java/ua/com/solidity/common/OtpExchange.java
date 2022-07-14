package ua.com.solidity.common;

import lombok.Getter;

@Getter
public class OtpExchange {
    public static final String SCHEDULER = "otp-etl.scheduler";
    public static final String SCHEDULER_INIT = "otp-etl.scheduler.init";
    public static final String SCHEDULER_TEST = "otp-etl.scheduler.test";
    public static final String DOWNLOADER = "otp-etl.downloader";
    public static final String IMPORTER = "otp-etl.importer";
    public static final String ENRICHER = "otp-etl.enricher";
    public static final String STATUS_LOGGER = "otp-etl.statuslogger";
    public static final String NOTIFICATION = "otp-etl.notification";
    public static final String DWH = "otp-etl.dwh";
    public static final String REPORT = "otp-etl.report";
}
