package org.ethereum.sharding.pubsub;

import org.ethereum.sharding.service.ValidatorRegistrationService;

/**
 * @author Mikhail Kalinin
 * @since 29.08.2018
 */
public class ValidatorStateUpdated extends Event<ValidatorStateUpdated.Data> {

    public static class Data {
        private final ValidatorRegistrationService.State newState;

        public Data(ValidatorRegistrationService.State newState) {
            this.newState = newState;
        }

        public ValidatorRegistrationService.State getNewState() {
            return newState;
        }
    }

    public ValidatorStateUpdated(ValidatorRegistrationService.State newState) {
        super(new Data(newState));
    }
}
