/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.dbsupport.MemSQL;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting MemSQL-specific delimiter changes.
 */
public class MemSQLSqlStatementBuilder extends SqlStatementBuilder {
    /**
     * The keyword that indicates a change in delimiter.
     */
    private static final String DELIMITER_KEYWORD = "DELIMITER";

    //Only UTF8 charset is supported by MemSQL
    private final String[] charSets = {
            "UTF8"
    };

    /*private -> testing*/ public boolean isInMultiLineCommentDirective = false;

    @Override
    public Delimiter extractNewDelimiterFromLine(String line) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return null;
    }

    @Override
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        if (line.toUpperCase().startsWith(DELIMITER_KEYWORD)) {
            return new Delimiter(line.substring(DELIMITER_KEYWORD.length()).trim(), false);
        }

        return delimiter;
    }

    @Override
    public boolean isCommentDirective(String line) {
        // single-line comment directive
        if (line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*" + Pattern.quote("*/") + "\\s*;?")) {
            return true;
        }
        // start of multi-line comment directive
        if (line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*")) {
            isInMultiLineCommentDirective = true;
            return true;
        }
        // last line of multi-line comment directive
        if (isInMultiLineCommentDirective && line.matches(".*" + Pattern.quote("*/") + "\\s*;?")) {
            isInMultiLineCommentDirective = false;
            return true;
        }
        return isInMultiLineCommentDirective;
    }

    @Override
    protected boolean isSingleLineComment(String token) {
        return token.startsWith("--") || (token.startsWith("#") && !("#".equals(delimiter.getDelimiter()) && "#".equals(token)));
    }

    @Override
    protected String removeEscapedQuotes(String token) {
        String noEscapedBackslashes = StringUtils.replaceAll(token, "\\\\", "");
        String noBackslashEscapes = StringUtils.replaceAll(StringUtils.replaceAll(noEscapedBackslashes, "\\'", ""), "\\\"", "");
        return StringUtils.replaceAll(noBackslashEscapes, "''", "").replace("'", " ' ");
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("B'") || token.startsWith("X'")) {
            return token.substring(token.indexOf("'"));
        }

        if (token.startsWith("_")) {
            for (String charSet : charSets) {
                String cast = "_" + charSet;
                if (token.startsWith(cast)) {
                    return token.substring(cast.length());
                }
            }
        }

        // If no matches are found for charset casting then return token
        return token;
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("\"")) {
            return "\"";
        }
        return null;
    }
}
