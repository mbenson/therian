package therian.behavior;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.ext.LoggerWrapper;

import therian.Therian;

/**
 * Logging mode {@link Behavior} {@code enum}. When {@code PANIC} is specified, {@code TRACE} messages will log to
 * {@code DEBUG} level and {@code DEBUG} messages will log to {@code INFO} level.
 */
//@formatter:off
public enum LoggingMode implements Behavior {
    NORMAL, PANIC;
//@formatter:on

    public static class StatefulLogger extends LoggerWrapper {
        private final Therian parent;

        public StatefulLogger(Therian parent, Logger wrapped, String fqcn) {
            super(wrapped, fqcn);
            this.parent = Validate.notNull(parent, "parent");
        }

        @Override
        public void debug(Marker arg0, String arg1, Object arg2, Object arg3) {
            if (isPanicMode()) {
                super.info(arg0, arg1, arg2, arg3);
            } else {
                super.debug(arg0, arg1, arg2, arg3);
            }
        }

        @Override
        public void debug(Marker arg0, String arg1, Object... arg2) {
            if (isPanicMode()) {
                super.info(arg0, arg1, arg2);
            } else {
                super.debug(arg0, arg1, arg2);
            }
        }

        @Override
        public void debug(Marker arg0, String arg1, Object arg2) {
            if (isPanicMode()) {
                super.info(arg0, arg1, arg2);
            } else {
                super.debug(arg0, arg1, arg2);
            }
        }

        @Override
        public void debug(Marker marker, String msg, Throwable t) {
            if (isPanicMode()) {
                super.info(marker, msg, t);
            } else {
                super.debug(marker, msg, t);
            }
        }

        @Override
        public void debug(Marker marker, String msg) {
            if (isPanicMode()) {
                super.info(marker, msg);
            } else {
                super.debug(marker, msg);
            }
        }

        @Override
        public void debug(String arg0, Object arg1, Object arg2) {
            if (isPanicMode()) {
                super.info(arg0, arg1, arg2);
            } else {
                super.debug(arg0, arg1, arg2);
            }
        }

        @Override
        public void debug(String arg0, Object... arg1) {
            if (isPanicMode()) {
                super.info(arg0, arg1);
            } else {
                super.debug(arg0, arg1);
            }
        }

        @Override
        public void debug(String arg0, Object arg1) {
            if (isPanicMode()) {
                super.info(arg0, arg1);
            } else {
                super.debug(arg0, arg1);
            }
        }

        @Override
        public void debug(String msg, Throwable t) {
            if (isPanicMode()) {
                super.info(msg, t);
            } else {
                super.debug(msg, t);
            }
        }

        @Override
        public void debug(String msg) {
            if (isPanicMode()) {
                super.info(msg);
            } else {
                super.debug(msg);
            }
        }

        @Override
        public boolean isDebugEnabled() {
            return isPanicMode() ? super.isInfoEnabled() : super.isDebugEnabled();
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return isPanicMode() ? super.isInfoEnabled(marker) : super.isDebugEnabled(marker);
        }

        @Override
        public boolean isTraceEnabled() {
            return isPanicMode() ? super.isDebugEnabled() : super.isTraceEnabled();
        }

        @Override
        public boolean isTraceEnabled(Marker marker) {
            return isPanicMode() ? super.isDebugEnabled(marker) : super.isTraceEnabled(marker);
        }

        @Override
        public void trace(Marker arg0, String arg1, Object arg2, Object arg3) {
            if (isPanicMode()) {
                super.debug(arg0, arg1, arg2, arg3);
            } else {
                super.trace(arg0, arg1, arg2, arg3);
            }
        }

        @Override
        public void trace(Marker arg0, String arg1, Object... arg2) {
            if (isPanicMode()) {
                super.debug(arg0, arg1, arg2);
            } else {
                super.trace(arg0, arg1, arg2);
            }
        }

        @Override
        public void trace(Marker arg0, String arg1, Object arg2) {
            if (isPanicMode()) {
                super.debug(arg0, arg1, arg2);
            } else {
                super.trace(arg0, arg1, arg2);
            }
        }

        @Override
        public void trace(Marker marker, String msg, Throwable t) {
            if (isPanicMode()) {
                super.debug(marker, msg, t);
            } else {
                super.trace(marker, msg, t);
            }
        }

        @Override
        public void trace(Marker marker, String msg) {
            if (isPanicMode()) {
                super.debug(marker, msg);
            } else {
                super.trace(marker, msg);
            }
        }

        @Override
        public void trace(String arg0, Object arg1, Object arg2) {
            if (isPanicMode()) {
                super.debug(arg0, arg1, arg2);
            } else {
                super.trace(arg0, arg1, arg2);
            }
        }

        @Override
        public void trace(String arg0, Object... arg1) {
            if (isPanicMode()) {
                super.debug(arg0, arg1);
            } else {
                super.trace(arg0, arg1);
            }
        }

        @Override
        public void trace(String arg0, Object arg1) {
            if (isPanicMode()) {
                super.debug(arg0, arg1);
            } else {
                super.trace(arg0, arg1);
            }
        }

        @Override
        public void trace(String msg, Throwable t) {
            if (isPanicMode()) {
                super.debug(msg, t);
            } else {
                super.trace(msg, t);
            }
        }

        @Override
        public void trace(String msg) {
            if (isPanicMode()) {
                super.debug(msg);
            } else {
                super.trace(msg);
            }
        }

        private boolean isPanicMode() {
            return parent.getBehavior(LoggingMode.class, NORMAL) == PANIC;
        }
    }

    @Override
    public Class<? extends Behavior> getType() {
        return LoggingMode.class;
    }
}
