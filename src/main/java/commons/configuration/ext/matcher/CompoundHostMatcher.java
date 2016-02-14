package commons.configuration.ext.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Combines several {@link HostMatcher}s into one. All matcher will be tried, in insertion order, or until a match is
 * found.
 * 
 * @author Timothy Storm
 */
public class CompoundHostMatcher implements HostMatcher {
    private List<HostMatcher> _matchers;

    public CompoundHostMatcher() {}

    public CompoundHostMatcher(Collection<HostMatcher> matchers) {
        addAll(matchers);
    }

    public CompoundHostMatcher addAll(Collection<HostMatcher> matchers) {
        if (matchers != null) getHostMatchersInternal().addAll(matchers);
        return this;
    }

    public CompoundHostMatcher clear() {
        getHostMatchersInternal().clear();
        return this;
    }

    protected Collection<HostMatcher> getHostMatchersInternal() {
        if (_matchers == null) _matchers = new ArrayList<>();
        return _matchers;
    }

    /**
     * {@inheritDoc}
     * Iterates each {@link HostMatcher} trying to find a matching host. If none are found then false is sreturned.
     */
    @Override
    public boolean matches(final String host) {
        for (HostMatcher matcher : getHostMatchersInternal()) {
            if (matcher.matches(host)) return true;
        }
        return false;
    }

    /**
     * Short cut for {@link #clear()} then {@link #addAll(Collection)}
     * 
     * @param matchers
     *            to add to this
     * @return this class for more setup
     */
    public CompoundHostMatcher setHostMatchers(Collection<HostMatcher> matchers) {
        getHostMatchersInternal().clear();
        return addAll(matchers);
    }
}
