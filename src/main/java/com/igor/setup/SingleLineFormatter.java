package com.igor.setup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {

	private static final ZoneId ZONE_ID = ZoneId.systemDefault();
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("kk:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {

		String threadName = Thread.currentThread().getName();
		Instant recordInstant = Instant.ofEpochMilli(record.getMillis());
		LocalDateTime recordDateTime = LocalDateTime.ofInstant(recordInstant, ZONE_ID);

		StringBuilder sb = new StringBuilder(180);
		sb.append("[");
		sb.append(threadName);
		sb.append("] ");
		sb.append(record.getLevel());
		sb.append(" ");
		sb.append(recordDateTime.format(FORMATTER));
		sb.append(" - ");
		sb.append(record.getLoggerName());
		sb.append(": ");
		sb.append(formatMessage(record));
		sb.append("\n");

		Throwable throwable = record.getThrown();
		if (throwable != null) {
			StringWriter sink = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sink, true));
			sb.append(sink);
		}

		return sb.toString();
	}
}