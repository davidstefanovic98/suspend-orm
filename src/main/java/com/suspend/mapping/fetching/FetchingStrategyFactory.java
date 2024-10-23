package com.suspend.mapping.fetching;

import com.suspend.core.exception.SuspendException;
import com.suspend.mapping.FetchType;

import java.util.List;

public class FetchingStrategyFactory {

    private static final List<FetchStrategy> strategies = List.of(
            new EagerFetchStrategy(),
            new LazyFetchStrategy()
    );

    public static FetchStrategy getFetchStrategy(FetchType fetchType) {
        return strategies
                .stream()
                .filter(f -> f.supports(fetchType))
                .findFirst()
                .orElseThrow(() ->
                        new SuspendException(String.format("No matching strategy for fetching type %s found.", fetchType.name())));
    }
}
