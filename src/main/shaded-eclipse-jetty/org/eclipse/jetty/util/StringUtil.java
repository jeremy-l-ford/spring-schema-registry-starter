//
// ========================================================================
// Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.util;

/**
 * Fast String Utilities.
 * <p>
 * These string utilities provide both convenience methods and
 * performance improvements over most standard library versions. The
 * main aim of the optimizations is to avoid object creation unless
 * absolutely required.
 */
public class StringUtil {

    /**
     * Test if a string is not null and contains at least 1 non-whitespace characters in it.
     * <p>
     * Note: uses codepoint version of {@link Character#isWhitespace(int)} to support Unicode better.
     *
     * <pre>
     *   isNotBlank(null)   == false
     *   isNotBlank("")     == false
     *   isNotBlank("\r\n") == false
     *   isNotBlank("\t")   == false
     *   isNotBlank("   ")  == false
     *   isNotBlank("a")    == true
     *   isNotBlank(".")    == true
     *   isNotBlank(";\n")  == true
     * </pre>
     *
     * @param str the string to test.
     * @return true if string is not null and has at least 1 non-whitespace character, false if null or all-whitespace characters.
     */
    public static boolean isNotBlank(String str) {
        if (str == null) {
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(str.codePointAt(i))) {
                // found a non-whitespace, we can stop searching  now
                return true;
            }
        }
        // only whitespace
        return false;
    }


}