package uk.co.duelmonster.minersadvantage.common.log;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.duelmonster.minersadvantage.ModCommon;

/**
 * Central logging utility for Miners Advantage.
 */
public final class LogUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ModCommon.MOD_NAME);
    private static final String DEBUG_PROPERTY = "minersadvantage.debugLogging";
    private static final String DEBUG_ENV = "MINERSADVANTAGE_DEBUG_LOGGING";

    private LogUtils() {
    }

    private static Object[] mergeArgs(Object... args) {
        if (args == null || args.length == 0) {
            return new Object[] { ModCommon.MOD_NAME };
        }
        Object[] out = new Object[args.length + 1];
        out[0] = ModCommon.MOD_NAME;
        System.arraycopy(args, 0, out, 1, args.length);
        return out;
    }

    public static boolean isDebugLoggingEnabled() {
        return propertyEnabled(System.getProperty(DEBUG_PROPERTY))
            || propertyEnabled(System.getenv(DEBUG_ENV))
            || ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(argument -> argument.contains("jdwp"));
    }

    private static boolean propertyEnabled(String value) {
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on");
    }

    public static void applyConfiguredLogging() {
        if (!isDebugLoggingEnabled()) {
            return;
        }
        try {
            Class<?> configuratorClass = Class.forName("org.apache.logging.log4j.core.config.Configurator");
            Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
            Object debugLevel = levelClass.getField("DEBUG").get(null);
            java.lang.reflect.Method setLevel = configuratorClass.getMethod("setLevel", String.class, levelClass);
            setLevel.invoke(null, ModCommon.MOD_NAME, debugLevel);
            setLevel.invoke(null, "uk.co.duelmonster.minersadvantage", debugLevel);
            LOG.info("[{}] Programmatically set Log4j2 logger level to DEBUG", ModCommon.MOD_NAME);
            return;
        } catch (Exception ignored) {
        }

        try {
            Class<?> loggerContextClass = Class.forName("ch.qos.logback.classic.LoggerContext");
            Class<?> levelClass = Class.forName("ch.qos.logback.classic.Level");
            Object loggerFactory = LoggerFactory.getILoggerFactory();
            if (loggerContextClass.isInstance(loggerFactory)) {
                java.lang.reflect.Method getLogger = loggerContextClass.getMethod("getLogger", String.class);
                Object rootLogger = getLogger.invoke(loggerFactory, "ROOT");
                Class<?> loggerClass = Class.forName("ch.qos.logback.classic.Logger");
                java.lang.reflect.Method setLevel = loggerClass.getMethod("setLevel", levelClass);
                Object debugLevel = levelClass.getField("DEBUG").get(null);
                setLevel.invoke(rootLogger, debugLevel);
                LOG.info("[{}] Programmatically set Logback root logger level to DEBUG", ModCommon.MOD_NAME);
                return;
            }
        } catch (Exception ignored) {
        }

        LOG.warn("[{}] Could not programmatically set logger level for debug logging", ModCommon.MOD_NAME);
    }

    public static void logInfo(String format, Object... args) {
        LOG.info("[{}] " + format, mergeArgs(args));
    }

    public static void logDebug(String format, Object... args) {
        if (isDebugLoggingEnabled()) {
            LOG.debug("[{}] " + format, mergeArgs(args));
        }
    }

    public static void logDebug(String format, Throwable throwable) {
        if (isDebugLoggingEnabled()) {
            LOG.debug("[" + ModCommon.MOD_NAME + "] " + format, throwable);
        }
    }

    public static void logWarn(String format, Object... args) {
        LOG.warn("[{}] " + format, mergeArgs(args));
    }

    public static void logWarn(String format, Throwable throwable) {
        LOG.warn("[" + ModCommon.MOD_NAME + "] " + format, throwable);
    }

    public static void logError(String format, Object... args) {
        LOG.error("[{}] " + format, mergeArgs(args));
    }

    public static void logError(String format, Throwable throwable) {
        LOG.error("[" + ModCommon.MOD_NAME + "] " + format, throwable);
    }

    public static void logTrace(String format, Object... args) {
        LOG.trace("[{}] " + format, mergeArgs(args));
    }

    public static void logTrace(String format, Throwable throwable) {
        LOG.trace("[" + ModCommon.MOD_NAME + "] " + format, throwable);
    }
}
