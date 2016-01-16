package commons.configuration.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class CompoundHostMatcher implements HostMatcher {
    private final List<HostMatcher> _matchers = new ArrayList<>();

    CompoundHostMatcher() {}

    CompoundHostMatcher(HostMatcher... matchers) {
        addAll(Arrays.asList(matchers));
    }

    CompoundHostMatcher(Collection<HostMatcher> matchers) {
        addAll(matchers);
    }

    CompoundHostMatcher add(HostMatcher matcher) {
        if (matcher != null) _matchers.add(matcher);
        return this;
    }

    CompoundHostMatcher addAll(Collection<HostMatcher> matchers) {
        if (matchers != null) _matchers.addAll(matchers);
        return this;
    }

    @Override
    public boolean matches(final String host) {
        return _matchers.stream().anyMatch(m -> m.matches(host));
    }
}
