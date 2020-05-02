package ca.powerj;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

class LLogs {
	private Logger logger = null;
	
	LLogs(String appDir) {
		Handler logHandler  = null;
		logger = Logger.getLogger(LConstants.APP_NAME);
		try {
			logHandler = new FileHandler(appDir + "logs" +
					System.getProperty("file.separator") +
					LConstants.APP_NAME + "_log_%g.xml", 1000000, 5);
			// Set levels to handlers and logger
			logger.setLevel(Level.ALL);
			logHandler.setLevel(Level.ALL);
			// Assign handlers to logger object
			logger.addHandler(logHandler);
			// Disable output to stderr
			logger.setUseParentHandlers(false);
			// Test logging
			logger.log(Level.INFO, LConstants.APP_NAME + " started.");
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void close() {
		if (logger != null) {
			logger.log(Level.INFO, LConstants.APP_NAME + " exited.");
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