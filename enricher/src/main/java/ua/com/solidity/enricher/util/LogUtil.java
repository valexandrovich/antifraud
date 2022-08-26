package ua.com.solidity.enricher.util;

import lombok.CustomLog;
import lombok.NoArgsConstructor;
import ua.com.solidity.common.DefaultErrorLogger;
import ua.com.solidity.common.ErrorReport;

@CustomLog
public final class LogUtil {
	private LogUtil() {}

	public static void logError(DefaultErrorLogger logger, long row, String info, String clarification) {
		if (logger != null)
			logger.logError(new ErrorReport(row, -1L, -1L, -1L, -1L, info, clarification));
	}
	public static void logStart(String table) {
		log.info("Data from {} are being transferred to OLAP zone", table);
	}

	public static void logFinish(String table, Long rows) {
		log.info("Imported {} records from {}", rows, table);
	}
}
