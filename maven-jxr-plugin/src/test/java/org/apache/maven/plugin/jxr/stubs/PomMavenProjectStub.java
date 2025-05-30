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
package org.apache.maven.plugin.jxr.stubs;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 */
public class PomMavenProjectStub extends JxrProjectStub {
    private Build build;

    public PomMavenProjectStub() {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = null;

        try (InputStream is = Files.newInputStream(new File(getBasedir() + "/" + getPOM()).toPath())) {
            model = pomReader.read(is);
            setModel(model);
        } catch (Exception ignored) {
        }

        setGroupId(model.getGroupId());
        setArtifactId(model.getArtifactId());
        setVersion(model.getVersion());
        setName(model.getName());
        setUrl(model.getUrl());
        setPackaging(model.getPackaging());

        Build build = new Build();
        build.setFinalName(model.getArtifactId());
        build.setDirectory(getBasedir() + "/target");
        build.setSourceDirectory(getBasedir() + "/src/main/java");
        build.setOutputDirectory(getBasedir() + "/target/classes");
        build.setTestSourceDirectory(getBasedir() + "/src/test/java");
        build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
        setBuild(build);

        List<String> compileSourceRoots = new ArrayList<>();
        compileSourceRoots.add(getBasedir() + "/src/main/java");
        setCompileSourceRoots(compileSourceRoots);

        List<String> testCompileSourceRoots = new ArrayList<>();
        testCompileSourceRoots.add(getBasedir() + "/src/test/java");
        setTestCompileSourceRoots(testCompileSourceRoots);
    }

    /**
     * @see org.apache.maven.plugin.testing.stubs.MavenProjectStub#getBuild()
     */
    public Build getBuild() {
        return build;
    }

    /**
     * @see org.apache.maven.plugin.testing.stubs.MavenProjectStub#setBuild(org.apache.maven.model.Build)
     */
    public void setBuild(Build build) {
        this.build = build;
    }

    @Override
    public File getBasedir() {
        return new File(super.getBasedir() + "/pom-test");
    }

    @Override
    protected String getPOM() {
        return "pom-test-plugin-config.xml";
    }
}
