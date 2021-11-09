package com.dmytrobilokha.fbpom;

import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class OptionsState {

    private final SortedSet<String> enabledOptions = new TreeSet<>();
    private final SortedSet<String> disabledOptions = new TreeSet<>();

    public void addEnabledOptions(Collection<String> options) {
        enabledOptions.addAll(options);
    }

    public void removeEnabledOptions(Collection<String> options) {
        enabledOptions.removeAll(options);
    }

    public void addDisabledOptions(Collection<String> options) {
        disabledOptions.addAll(options);
    }

    public void removeDisabledOptions(Collection<String> options) {
        disabledOptions.removeAll(options);
    }

    public Set<String> getEnabledOptions() {
        return Set.copyOf(enabledOptions);
    }

    public Set<String> getDisabledOptions() {
        return Set.copyOf(disabledOptions);
    }

    public boolean isEmpty() {
        return enabledOptions.isEmpty() && disabledOptions.isEmpty();
    }

    @Override
    public String toString() {
        return "OptionsState{"
                + "enabledOptions=" + enabledOptions
                + ", disabledOptions=" + disabledOptions
                + '}';
    }

}
