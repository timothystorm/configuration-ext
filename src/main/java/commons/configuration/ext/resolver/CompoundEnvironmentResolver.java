package commons.configuration.ext.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import commons.configuration.ext.EnvironmentResolver;

/**
 * Combines several {@link EnvironmentResolver}s into one. Resolutions will be tried in insertion order until a match is
 * found or until there are no more resolvers to try.
 * 
 * @author Timothy Storm
 */
public class CompoundEnvironmentResolver implements EnvironmentResolver {
    private List<EnvironmentResolver> _resolvers;
    private String                    _to;

    public CompoundEnvironmentResolver() {}

    public CompoundEnvironmentResolver(Collection<EnvironmentResolver> resolvers) {
        addAll(resolvers);
    }

    public CompoundEnvironmentResolver(EnvironmentResolver... resolvers) {
        addAll(resolvers);
    }

    public CompoundEnvironmentResolver addAll(Collection<EnvironmentResolver> resolvers) {
        if (resolvers != null) getResolversInternal().addAll(resolvers);
        return this;
    }

    public CompoundEnvironmentResolver addAll(EnvironmentResolver... resolvers) {
        if (resolvers != null) getResolversInternal().addAll(Arrays.asList(resolvers));
        return this;
    }

    public CompoundEnvironmentResolver addHostMatcher(EnvironmentResolver resolver) {
        if (resolver != null) getResolversInternal().add(resolver);
        return this;
    }

    public CompoundEnvironmentResolver clear() {
        getResolversInternal().clear();
        return this;
    }

    protected Collection<EnvironmentResolver> getResolversInternal() {
        if (_resolvers == null) _resolvers = Collections.synchronizedList(new ArrayList<EnvironmentResolver>());
        return _resolvers;
    }

    @Override
    public boolean resolves(String environment, Collection<String> hosts) {
        _to = null;

        for (EnvironmentResolver r : getResolversInternal()) {
            if(r == null) continue;
            
            if (r.resolves(environment, hosts)) {
                _to = r.resolvesTo();
                return true;
            }
        }
        return false;
    }

    @Override
    public String resolvesTo() {
        if (_to == null) throw new IllegalStateException("no resolution available!");
        return _to;
    }
}
