package com.levware.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import egovframework.rte.fdl.cmmn.exception.handler.ExceptionHandler;

public class JdbcLoggingExcepHndlr implements ExceptionHandler {

    Logger logger = LogManager.getLogger(JdbcLoggingExcepHndlr.class.getName());
    
    public void occur(Exception ex, String packageName) {
    	logger.error(ex.getMessage());
    }

}
