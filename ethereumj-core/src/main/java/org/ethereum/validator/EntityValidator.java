/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.validator;

import java.util.Arrays;
import java.util.List;

/**
 * Composite Entity validator
 * aggregating list of simple validation rules
 */
public abstract class EntityValidator<R extends EntityRule<E>, E> extends EntityRule<E> {

    protected List<R> rules;

    public EntityValidator(List<R> rules) {
        this.rules = rules;
    }

    @SafeVarargs
    public EntityValidator(R ...rules) {
        this.rules = Arrays.asList(rules);
    }

    public void reInit(List<R> rules) {
        this.rules = rules;
    }

    @Override
    public ValidationResult validate(E entity) {
        for (R rule : rules) {
            ValidationResult result = rule.validate(entity);
            if (!result.success) {
                return result;
            }
        }
        return Success;
    }
}
