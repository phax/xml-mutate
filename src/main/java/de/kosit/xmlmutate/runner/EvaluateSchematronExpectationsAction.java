package de.kosit.xmlmutate.runner;

import de.kosit.xmlmutate.expectation.SchematronRuleExpectation;
import de.kosit.xmlmutate.mutation.Mutation;
import de.kosit.xmlmutate.mutation.Mutation.State;
import de.kosit.xmlmutate.mutation.MutationResult;
import de.kosit.xmlmutate.mutation.Schematron;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Check the assertions defined within the schematron rules againgst the validated state
 *
 * @author Andreas Penski
 */

public class EvaluateSchematronExpectationsAction implements RunAction {
    private static final Logger log = LoggerFactory.getLogger(EvaluateSchematronExpectationsAction.class);

    @Override
    public void run(final Mutation mutation) {

        if (!mutation.getResult().getSchematronResult().isEmpty()) {
            mutation.getConfiguration().getSchematronExpectations().forEach(e -> {

                final boolean unknownRuleName = this.checkeUnkknownRuleNames(e, mutation.getResult().getSchematronResult());

                final boolean valid = this.evaluate(e, mutation.getResult(), unknownRuleName);

                mutation.getResult().getSchematronExpectationMatches().put(e, valid);

                if (unknownRuleName) {
                    mutation.getMutationErrorContainer().addSchematronErrorMessage(e.getRuleName(), new MutationException(ErrorCode.SCHEMATRON_RULE_NOT_EXIST, e.getRuleName()));
                } else if (!valid) {
                    mutation.getMutationErrorContainer().addSchematronErrorMessage(e.getRuleName(), new MutationException(ErrorCode.SCHEMATRON_RULE_FAILED_EXPECTATION, e.getRuleName()));
                }

                log.trace(
                        "mutator={} rule={} mustPass={} mustFail={} evaluatedValid={}", mutation.getMutator().getNames(),
                        e.getRuleName(), e.expectValid(), e.expectInvalid(), valid);
            });
            mutation.setState(State.CHECKED);
        }
    }

    private boolean checkeUnkknownRuleNames(SchematronRuleExpectation e, Map<Schematron, SchematronOutput> schematronResult) {
        if (!schematronResult.isEmpty()) {
            final List<String> existingRuleNames = schematronResult.keySet().stream()
                    .map(Schematron::getRulesIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            return !existingRuleNames.contains(e.getRuleName());
        } else {
            return false;
        }
    }

    // Evalutes if result matches expectation
    private boolean evaluate(final SchematronRuleExpectation expectation, MutationResult result, final boolean unknownRule) {
        final Collection<SchematronOutput> targets;
        if (expectation.getSource() != null) {
            final Optional<SchematronOutput> schematronResult = result.getSchematronResult(expectation.getSource());

            targets = schematronResult.map(Collections::singletonList).orElseGet(ArrayList::new);
        } else {
            targets = result.getSchematronResult().values();
        }
        // get all failed assertions of schematron which matches the given expection
        final Optional<FailedAssert> failed = targets.stream().map(SchematronOutput::getFailedAsserts)
                .flatMap(List::stream).peek(f -> {
                }).filter(f -> f.getId().equals(expectation.getRuleName())).findFirst();
        // log.debug("Evaluation for {} ", failed.)
        boolean failedAsExpected = failed.isPresent() && expectation.expectInvalid();
        boolean noFailedButExpected = !failed.isPresent() && expectation.expectValid() && !unknownRule;
        log.trace("failedAsExpected={} or  noFailedButExpected={}", failedAsExpected, noFailedButExpected);
        return failedAsExpected || noFailedButExpected;
    }

    // xmute mutator = "noop" schematron-invalid="UBL-CR-001"

    // xmute mutator = "remove" schematron-valid="UBL-CR-001"
}
