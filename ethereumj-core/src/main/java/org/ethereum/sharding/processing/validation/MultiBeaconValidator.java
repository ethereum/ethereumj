package org.ethereum.sharding.processing.validation;

import org.ethereum.sharding.domain.Beacon;

import java.util.Arrays;
import java.util.List;

/**
 * Simply aggregates a list of {@link BeaconValidator}.
 *
 * @author Mikhail Kalinin
 * @since 15.11.2018
 */
public class MultiBeaconValidator implements BeaconValidator {

    List<BeaconValidator> validators;

    public MultiBeaconValidator(List<BeaconValidator> validators) {
        this.validators = validators;
    }

    public MultiBeaconValidator(BeaconValidator... validators) {
        this(Arrays.asList(validators));
    }

    @Override
    public ValidationResult validateAndLog(Beacon block) {
        for (BeaconValidator validator : validators) {
            ValidationResult res = validator.validateAndLog(block);
            if (res != ValidationResult.Success) {
                return res;
            }
        }

        return ValidationResult.Success;
    }
}
