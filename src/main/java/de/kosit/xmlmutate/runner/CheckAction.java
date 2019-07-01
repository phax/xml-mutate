package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;

/**
 * Prüft die definierten Assertions bei den Schematron-Regeln gegebenüber dem validierten Zustand.
 * 
 * @author Andreas Penski
 */
public class CheckAction implements RunAction {

    @Override
    public void run(final Mutation mutation) {
        mutation.getConfiguration().getSchematronExpectations().forEach(e -> {
            final boolean valid = e.evaluate(mutation.getResult());
            mutation.getResult().getExpectationResult().put(e, valid);
            if (!valid) {
                mutation.setErrorMessage("failed expectation assert");
            }
        });
        mutation.setState(State.CHECKED);
    }
}