package de.kosit.xmlmutate.mutator;

import de.kosit.xmlmutate.mutation.MutationConfig;
import de.kosit.xmlmutate.mutation.MutationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple mutator which does not make any changes to the documents.
 * Purpose is to check schematron assertions of the given document.
 */
@Slf4j
public class NoopMutator implements Mutator {
    @Override
    public String getName() {
        return "noop";
    }

    @Override
    public void mutate(MutationContext context, MutationConfig config) {
        log.debug("Mutating e.g. do 'nothing'");
    }
}