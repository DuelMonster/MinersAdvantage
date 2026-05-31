package uk.co.duelmonster.minersadvantage.common.log;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.duelmonster.minersadvantage.ModCommon;

/**
 * Logging utility that standardizes message format and debug-mode behavior across loaders.
 * This keeps logs readable and saves future-you from grep archaeology marathons.
 */
public final class LogUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ModCommon.MOD_NAME);
    private static final String DEBUG_PROPERTY = "minersadvantage.debugLogging";
    private static final String DEBUG_ENV = "MINERSADVANTAGE_DEBUG_LOGGING";

    /**
     * Utility class only; all functionality is static on purpose.
     */
    private LogUtils() {
    }

    /**
     * Prefix all formatted log calls with mod name placeholder while preserving original args.
     */
    private static Object[] mergeArgs(Object... args) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (args == null || args.length == 0) {
            return new Object[] { ModCommon.MOD_NAME };
        }
        Object[] out = new Object[args.length + 1];
        out[0] = ModCommon.MOD_NAME;
        System.arraycopy(args, 0, out, 1, args.length);
        return out;
    }

    /**
     * Debug mode is enabled by property, env var, or attached debugger detection.
     */
    public static boolean isDebugLoggingEnabled() {
        return propertyEnabled(System.getProperty(DEBUG_PROPERTY))
            || propertyEnabled(System.getenv(DEBUG_ENV))
            || ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(argument -> argument.contains("jdwp"));
    }

    /**
     * Parse the usual truthy string values users tend to invent in env vars.
     */
    private static boolean propertyEnabled(String value) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("on");
    }

    /**
     * Try to push logger level to DEBUG at runtime for both Log4j2 and Logback environments.
     */
    public static void applyConfiguredLogging() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!isDebugLoggingEnabled()) {
            return;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
            // Log4j2 path unavailable? Fine, we'll try Logback next without theatrics.
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            Class<?> loggerContextClass = Class.forName("ch.qos.logback.classic.LoggerContext");
            Class<?> levelClass = Class.forName("ch.qos.logback.classic.Level");
            Object loggerFactory = LoggerFactory.getILoggerFactory();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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
            // Logback path also unavailable; final warning below keeps this visible for diagnostics.
        }

        LOG.warn("[{}] Could not programmatically set logger level for debug logging", ModCommon.MOD_NAME);
    }

    /**
     * Standard info logging entry point with mod-name prefixing.
     */
    public static void logInfo(String format, Object... args) {
        LOG.info("[{}] " + format, mergeArgs(args));
    }

    /**
     * Debug logging helper that no-ops when debug mode is disabled.
     */
    public static void logDebug(String format, Object... args) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isDebugLoggingEnabled()) {
            LOG.debug("[{}] " + format, mergeArgs(args));
        }
    }

    /**
     * Throwable overload for debug logs so stack traces stay gated behind debug mode too.
     */
    public static void logDebug(String format, Throwable throwable) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (isDebugLoggingEnabled()) {
            LOG.debug("[" + ModCommon.MOD_NAME + "] " + format, throwable);
        }
    }

    /**
     * Warn-level logging entry point.
     */
    public static void logWarn(String format, Object... args) {
        LOG.warn("[{}] " + format, mergeArgs(args));
    }

    /**
     * Warn-level throwable overload.
     */
    public static void logWarn(String format, Throwable throwable) {
        LOG.warn("[" + ModCommon.MOD_NAME + "] " + format, throwable);
    }

    /**
     * Error-level logging entry point.
     */
    public static void logError(String format, Object... args) {
        LOG.error("[{}] " + format, mergeArgs(args));
    }

    /**
     * Error-level throwable overload.
     */
    public static void logError(String format, Throwable throwable) {
        LOG.error("[" + ModCommon.MOD_NAME + "] " + format, throwable);
    }

    /**
     * Trace-level logging entry point.
     */
    public static void logTrace(String format, Object... args) {
        LOG.trace("[{}] " + format, mergeArgs(args));
    }

    /**
     * Trace-level throwable overload.
     */
    public static void logTrace(String format, Throwable throwable) {
        LOG.trace("[" + ModCommon.MOD_NAME + "] " + format, throwable);
    }
}
