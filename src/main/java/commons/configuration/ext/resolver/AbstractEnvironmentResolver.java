package commons.configuration.ext.resolver;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;

import commons.configuration.ext.EnvironmentResolver;

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
