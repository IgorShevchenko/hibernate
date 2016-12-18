package com.igor.setup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SingleLineFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {

		String threadName = Thread.currentThread().getName();
		Date recordDate = new Date(record.getMillis());
		DateFormat dateFormat = new SimpleDateFormat("kk:mm:ss.SS");

		StringBuilder sb = new StringBuilder(180);
		sb.append("[");
		sb.append(threadName);
		sb.append("] ");
		sb.append(record.getLevel());
		sb.append(" ");
		sb.append(dateFormat.format(recordDate));
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