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
package org.ethereum.jsontestsuite.suite;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.ethereum.core.CallTransaction;
import org.spongycastle.util.encoders.Hex;

import java.util.List;

/**
 * @author Mikhail Kalinin
 * @since 28.09.2017
 */
public class ABITestCase {

    @JsonIgnore
    String name;

    List<Object> args;
    String[] types;
    String result;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEncoded() {

        CallTransaction.Function f = CallTransaction.Function.fromSignature("test", types);
        byte[] encoded = f.encodeArguments(args.toArray());

        return Hex.toHexString(encoded);
    }

    @Override
    public String toString() {
        return "ABITestCase{" +
                "name='" + name + '\'' +
                ", args=" + args +
                ", types=" + types +
                ", result='" + result + '\'' +
                '}';
    }
}
