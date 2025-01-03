/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.jxr;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple unit-testtest that illustrates a line with more
 * than one "token" to replace
 */
class JXR141Test {
    private JXR jxr;

    @BeforeEach
    void setUp() {
        FileManager fileManager = new FileManager();
        PackageManager packageManager = new PackageManager(fileManager);
        JavaCodeTransform codeTransform = new JavaCodeTransform(packageManager, fileManager);

        jxr = new JXR(packageManager, codeTransform);
    }

    @Test
    void processPath() throws Exception {
        jxr.setDest(Paths.get("target/jxr-141"));
        jxr.setOutputEncoding("UTF-8");
        jxr.xref(
                Collections.singletonList("src/test/resources/jxr141"),
                "templates/jdk4",
                "title",
                "title",
                "copyright");

        List<String> lines = Files.readAllLines(Paths.get("target/jxr-141/Test141.html"), StandardCharsets.UTF_8);

        // Find line #27...
        String line27 = null;
        for (String line : lines) {
            if (line.contains("#L27")) {
                line27 = line;
                break;
            }
        }

        assertNotNull(line27, "Line #27 not found - has source of Test141.java changed?");
        assertEquals(
                "<a class=\"jxr_linenumber\" name=\"L27\" href=\"#L27\">27</a>      "
                        + "<strong class=\"jxr_keyword\">public</strong> <strong class=\"jxr_keyword\">static</strong> "
                        + "<strong class=\"jxr_keyword\">final</strong> "
                        + "<a name=\"Test141\" href=\"..//Test141.html#Test141\">Test141</a> instance = <strong class=\"jxr_keyword\">new</strong> "
                        + "<a name=\"Test141\" href=\"..//Test141.html#Test141\">Test141</a>();",
                line27);
    }
}
