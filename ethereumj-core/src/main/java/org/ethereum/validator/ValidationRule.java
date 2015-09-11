package org.ethereum.validator;

import java.util.List;

/**
 * Topmost interface for validation rules
 *
 * @author Mikhail Kalinin
 * @since 02.09.2015
 */
public interface ValidationRule {

    /**
     * Returns errors occurred during most recent validation run
     *
     * @return list of errors
     */
    List<String> getErrors();
}
