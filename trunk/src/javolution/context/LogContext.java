/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.context;

import javolution.util.StandardLog;
import j2me.lang.UnsupportedOperationException;
import j2me.lang.CharSequence;

/**
 * <p> This class represents a logging context; it allows for 
 *     object-based/thread-based logging and logging specializations 
 *     (such as {@link StandardLog StandardLog} to leverage   
 *     <code>java.util.logging</code> capabilities).</p>
 *
 * <p> The default logging context is {@link #STANDARD} (log events forwarded
 *     to the root <code>java.util.logging.Logger</code>). Users may 
 *     changes the default logging for all threads:[code]
 *     // Logging disabled by default.
 *     LogContext.setDefault(LogContext.NULL); 
 *     [/code]</p>
 *     
 * <p> Applications may extend this base class to address specific logging
 *     requirements. For example:[code]
 *     // This class allows for custom logging of session events. 
 *     public abstract class SessionLog extends LogContext  {
 *         public static void start(Session session) {
 *             LogContext log = LogContext.current();
 *             if (log instanceof SessionLog.Loggable) { 
 *                 ((SessionLog.Loggable)log).logStart(session);
 *             } else if (log.isInfoLogged()) {
 *                 log.logInfo("Session " + session.id() + " started");
 *             }
 *         }
 *         public static void end(Session session) { ... }
 *         public interface Loggable { 
 *             void logStart(Session session);
 *             void logEnd(Session session);
 *         }
 *     }[/code]</p>
 *     
 * <p> The use of interfaces (such as <code>Loggable</code> above) makes it easy
 *     for any context to support customs logging events.
 *     For example:[code]
 *     class MyLog extends StandardLog implements SessionLog.Loggable, DatabaseLog.Loggable { 
 *         ...   // Specialized logging for session and database events. 
 *     }
 *     MyLog myLog = new MyLog();
 *     LogContext.enter(myLog);
 *     try {
 *         ...
 *         LogContext.info("Informative message"); // Standard logging.   
 *         ...
 *         DatabaseLog.fail(transaction); // Database custom logging.
 *         ... 
 *         SessionLog.start(session); // Session custom logging.
 *         ...
 *     } finally {
 *         LogContext.exit(myLog);
 *     }[/code]</p>    
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.6, November 16, 2005
 */
public abstract class LogContext extends Context {
    
    /**
     * Holds a context forwarding log events to the root <code>
     * java.util.logging.Logger</code> framework (default logging context).
     * The info/warning/error events are mapped to the info/warning/severe 
     * log levels respectively.
     */
    public static final StandardLog STANDARD = new StandardLog() {
        protected void enterAction() {
            throw new UnsupportedOperationException(
                "Shared context (static) cannot be directly entered");
        }
    };

    /**
     * Holds a context ignoring logging events.
     */
    public static final LogContext NULL = new NullLog();

    /**
     * Holds a context logging informative messages to 
     * <code>System.out</code> and warnings/errors events to 
     * <code>System.err</code>.
     */
    public static final LogContext SYSTEM = new SystemLog();

    /**
     * Holds a context logging warnings/errors events to 
     * <code>System.err</code> and ignoring informative messages.
     */
    public static final LogContext SYSTEM_ERR = new SystemErrLog();

    /**
     * Holds the default logging context.
     */
    private static volatile LogContext _Default = STANDARD;

    /**
     * Default constructor.
     */
    protected LogContext() {
    }

    /**
     * Returns the current logging context (or {@link #getDefault()}
     * if the current thread has not entered any logging context).
     *
     * @return the current logging context.
     */
    public static/*LogContext*/Context current() {
        for (Context ctx = Context.current(); ctx != null; ctx = ctx.getOuter()) {
            if (ctx instanceof LogContext)
                return (LogContext) ctx;
        }
        return LogContext._Default;
    }

    /**
     * Returns the default logging context for new threads 
     * ({@link #STANDARD} when not explicitly {@link #setDefault set}).
     * 
     * @return the default logging context.
     */
    public static LogContext getDefault() {
        return LogContext._Default;
    }

    /**
     * Sets the specified logging context as default.
     * 
     * @param context the default security context.
     */
    public static void setDefault(LogContext context) {
        LogContext._Default = context;
    }

    /**
     * Logs the specified informative message.
     * 
     * @param message the informative message being logged.
     * @see #logInfo(CharSequence)
     */
    public static void info(CharSequence message) {
        LogContext logContext = (LogContext) LogContext.current();
        logContext.logInfo(message);
    }

    /**
     * Logs the specified warning message to the current logging context.
     * 
     * @param message the warning message being logged.
     * @see #logWarning(CharSequence)
     */
    public static void warning(CharSequence message) {
        LogContext logContext = (LogContext) LogContext.current();
        logContext.logWarning(message);
    }

    /**
     * Logs the specified error to the current logging context.
     * 
     * @param error the error being logged.
     */
    public static void error(Throwable error) {
        LogContext logContext = (LogContext) LogContext.current();
        logContext.logError(error, null);
    }

    /**
     * Logs the specified error and error message to the current logging
     * context. 
     * 
     * @param error the error being logged.
     * @param message the supplementary message.
     */
    public static void error(Throwable error, CharSequence message) {
        LogContext logContext = (LogContext) LogContext.current();
        logContext.logError(error, message);
    }

    /**
     * Logs the specified error message to the current logging
     * context. 
     * 
     * @param message the error message.
     */
    public static void error(CharSequence message) {
        LogContext logContext = (LogContext) LogContext.current();
        logContext.logError(null, message);
    }
    /**
     * Indicates if informative messages are logged.
     * 
     * @return <code>true</code> if informative messages are logged;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isInfoLogged();

    /**
     * Logs the specified informative message.
     * 
     * @param message the informative message being logged.
     */
    public abstract void logInfo(CharSequence message);

    /**
     * Indicates if warning messages are logged.
     * 
     * @return <code>true</code> if warnings message are logged;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isWarningLogged();
    
    /**
     * Logs the specified warning message.
     * 
     * @param message the warning message being logged.
     */
    public abstract void logWarning(CharSequence message);

    /**
     * Indicates if errors are logged.
     * 
     * @return <code>true</code> if errors are logged;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isErrorLogged();

    /**
     * Logs the specified error.
     * 
     * @param error the error being logged or <code>null</code> if none.
     * @param message the associated message or <code>null</code> if none.
     */
    public abstract void logError(Throwable error, CharSequence message);

    // Implements Context abstract method.
    protected void enterAction() {
        // Do nothing.
    }

    // Implements Context abstract method.
    protected void exitAction() {
        // Do nothing.
    }

    /**
     * This class represents a non-logging context.
     */
    private static final class NullLog extends LogContext {

        protected void enterAction() {
            throw new UnsupportedOperationException(
                "Shared context (static) cannot be directly entered");
        }
        
        public boolean isInfoLogged() {
            return false;
        }

        public boolean isWarningLogged() {
            return false;
        }

        public boolean isErrorLogged() {
            return false;
        }

        public void logInfo(CharSequence message) {
            // Do nothing.
        }

        public void logWarning(CharSequence message) {
            // Do nothing.
        }

        public void logError(Throwable error, CharSequence message) {
            // Do nothing.
        }
    }

    /**
     * This class represents the system logging context.
     */
    private static class SystemLog extends LogContext {

        protected void enterAction() {
            throw new UnsupportedOperationException(
                "Shared context (static) cannot be directly entered");
        }

        public boolean isInfoLogged() {
            return true;
        }

        public boolean isWarningLogged() {
            return true;
        }

        public boolean isErrorLogged() {
            return true;
        }

        public void logInfo(CharSequence message) {
            System.out.print("[info] ");
            System.out.println(message);
        }

        public void logWarning(CharSequence message) {
            System.err.print("[warning] ");
            System.err.println(message);
        }

        public void logError(Throwable error, CharSequence message) {
            SYSTEM_ERR.logError(error, message);
        }
    }

    /**
     * This class represents the system error logging context.
     */
    private static class SystemErrLog extends LogContext {

        protected void enterAction() {
            throw new UnsupportedOperationException(
                "Shared context (static) cannot be directly entered");
        }

        public boolean isInfoLogged() {
            return false;
        }

        public boolean isWarningLogged() {
            return true;
        }

        public boolean isErrorLogged() {
            return true;
        }

        public void logInfo(CharSequence message) {
            // Do nothing.
        }

        public void logWarning(CharSequence message) {
            System.err.print("[warning] ");
            System.err.println(message);
        }

        public void logError(Throwable error, CharSequence message) {
            System.err.print("[error] ");
            if (error != null) {
                 System.err.print(error.getClass().getName());
                 System.err.print(" - ");
            }
            String description = (message != null) ?
                message.toString() : (error != null) ? error.getMessage() : "";
            System.err.println(description);
            System.err.println();
            if (error != null) {
                error.printStackTrace();
            }
        }
    }
}