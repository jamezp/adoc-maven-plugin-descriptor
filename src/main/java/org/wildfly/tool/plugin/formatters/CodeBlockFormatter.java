/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.tool.plugin.formatters;

import java.util.regex.Pattern;

import org.apache.maven.tools.plugin.generator.GeneratorUtils;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CodeBlockFormatter implements Formatter {
    private static final Pattern CODE_BLOCK_START = Pattern.compile("<code>|<pre>");
    private static final Pattern CODE_BLOCK_END = Pattern.compile("</code>|</pre>");
    private final String lineSeparator = System.lineSeparator();

    @Override
    public String format(final String text) {
        String result = text;
        // Determine if we need a code block or the block is on a single line
        if (result.contains(lineSeparator)) {
            final StringBuilder builder = new StringBuilder();
            boolean inCodeBlock = false;
            // Process this line by line to discover if we need a code block
            final String[] lines = result.split("\\r?\\n");
            for (String line : lines) {
                final boolean startCodeBlock = CODE_BLOCK_START.matcher(line).find();
                final boolean endCodeBlock = CODE_BLOCK_END.matcher(line).find();
                if (startCodeBlock && !endCodeBlock) {
                    builder.append("----")
                            .append(lineSeparator);
                    inCodeBlock = true;
                }
                if (inCodeBlock) {
                    line = line.replace("<code>", "");
                    line = line.replace("<pre>", "");
                    line = line.replace("</code>", "");
                    line = line.replace("</pre>", "");
                    // Honor spaces and tabs
                    for (char c : line.toCharArray()) {
                        if (c == ' ') {
                            builder.append(' ');
                        } else if (c == '\t') {
                            // Assume 4 spaces for a tab
                            builder.append("    ");
                        } else {
                            // No longer a leading space or tab
                            break;
                        }
                    }
                    line = GeneratorUtils.toText(line);
                } else {
                    line = line.replace("<code>", "`");
                    line = line.replace("</code>", "`");
                }

                if (!line.isEmpty()) {
                    builder.append(line)
                            .append(lineSeparator);
                }
                if (!startCodeBlock && endCodeBlock) {
                    builder.append("----")
                            .append(lineSeparator);
                    inCodeBlock = false;
                }
            }
            result = builder.toString();
        } else {
            result = result.replaceAll("<code>", "`");
            result = result.replaceAll("</code>", "`");
        }
        return result;
    }
}
