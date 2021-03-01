package ca.powerj.lib;
import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class LibLogs {
	private Logger logger = null;
	
	LibLogs(String appDir) {
		Handler logHandler  = null;
		logger = Logger.getLogger(LibConstants.APP_NAME);
		try {
			File theDir = new File(appDir + "logs");
			if (theDir.exists()) {
				logHandler = new FileHandler(appDir + "logs" +
						System.getProperty("file.separator") +
						LibConstants.APP_NAME + "_log_%g.xml", 1000000, 5);
			} else {
				logHandler = new ConsoleHandler();
			}
			// Set levels to handlers and logger
			logger.setLevel(Level.ALL);
			logHandler.setLevel(Level.ALL);
			// Assign handlers to logger object
			logger.addHandler(logHandler);
			// Enable output to stderr
			logger.setUseParentHandlers(true);
			// Test logging
			logger.log(Level.INFO, LibConstants.APP_NAME + " started.");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void close() {
		if (logger != null) {
			logger.log(Level.INFO, LibConstants.APP_NAME + " exited.");
			Handler[] handler =  logger.getHandlers();
			for(Handler h: handler) {
				try {
					h.close();
					logger.removeHandler(h);
				} catch (SecurityException ignore) {}
			}
		}
	}

	void logError(String msg) {
		log(Level.SEVERE, msg);
	}
	
	void logError(String msg, Throwable e) {
		log(Level.SEVERE, msg, e);
	}
	
	void logInfo(String msg) {
		log(Level.INFO, msg);
	}
	
	void logInfo(String msg, Throwable e) {
		log(Level.INFO, msg, e);
	}
	
	private void log(Level level, String msg) {
		logger.log(level, msg);
	}
	
	private void log(Level level, String msg, Throwable e) {
		logger.log(level, msg + " ", e);
	}
}
