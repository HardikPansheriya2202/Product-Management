package com.logilite.jdbc_product_order_management;

import java.io.IOException;
import java.util.logging.*;
	
public class LoggerConfig {
 
    public static Logger getLogger(String loggerName, String fileName) {
        Logger logger = Logger.getLogger(loggerName);
 
        logger.setUseParentHandlers(false);
 
        try {
            FileHandler fileHandler = new FileHandler(fileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
 
        return logger;
    }
}