package org.ethereum.sharding.pubsub;

import org.ethereum.sharding.service.ValidatorService;

/**
 * @author Mikhail Kalinin
 * @since 29.08.2018
 */
public class ValidatorStateUpdated extends Event<ValidatorStateUpdated.Data> {

    public static class Data {
        private final ValidatorService.State newState;

        public Data(ValidatorService.State newState) {
            this.newState = newState;
        }

        public ValidatorService.State getNewState() {
            return newState;
        }
    }

    public ValidatorStateUpdated(ValidatorService.State newState) {
        super(new Data(newState));
    }
}
