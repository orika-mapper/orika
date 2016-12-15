package ma.glasnost.orika;

import ma.glasnost.orika.impl.mapping.strategy.MappingStrategyRecorder;

public interface MappingStrategyFactory {
    /**
     * Identifies if a mapping strategy can be applied to a scenario with the given context.
     */
    boolean isApplicable(MappingStrategyRecorder data);

    /**
     * Builds the mapping strategy to be applied to the given context.
     */
    MappingStrategy build(MappingStrategyRecorder data);
}
