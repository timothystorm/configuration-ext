package commons.configuration.ext.resolver;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;

import commons.configuration.ext.EnvironmentResolver;

/**
 * Utility to assist in resolving runtime environments. Extensions need only implement the
 * {@link #resolves(String, String)} method. If a resolution is successful then the current environment when calling the
 * {@link #resolvesTo()}.
 * 
 * @author Timothy Storm
 */
public abstract class AbstractEnvironmentResolver implements EnvironmentResolver {
    private String _to;

    /** Stores the logger. */
    private Log _log;

    public AbstractEnvironmentResolver() {
        setLogger(null);
    }

    public void setLogger(Log log) {
        _log = (log != null) ? log : new NoOpLog();
    }

    protected Log getLogger() {
        return _log;
    }

    /**
     * <ol>
     * <li>Verifies the existence of the env and hosts.</li>
     * <li>iterates the hosts
     * <ol>
     * <li>calls {@link #resolves(String, String)} for each env and host combination</li>
     * <li>if an env/host successfully resolves the resolution will be the current environment</li>
     * </ol>
     * </li>
     * </ol>
     */
    @Override
    public boolean resolves(final String env, final Collection<String> hosts) {
        if (env == null || hosts == null) throw new NullPointerException();

        _to = null;
        for (String h : hosts) {
            if (resolves(env, h)) {
                _to = env;
                return true;
            }
        }
        return false;
    }

    protected abstract boolean resolves(String env, String host);

    @Override
    public String resolvesTo() {
        if (_to == null) throw new IllegalStateException("no resolution available!");
        return _to;
    }

}
