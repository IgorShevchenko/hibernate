package com.igor.setup;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerConfig {

	public static void setup() {
		Logger rootLog = Logger.getLogger("");
		rootLog.setLevel(Level.FINE);
		rootLog.getHandlers()[0].setLevel(Level.FINE);
	}
}
