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
package org.apache.maven.plugin.jxr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract class to test reports generation.
 */
public abstract class AbstractJxrTestCase extends AbstractMojoTestCase {
    private ArtifactStubFactory artifactStubFactory;

    /**
     * The current project to be test.
     */
    private MavenProject testMavenProject;

    @BeforeEach
    public void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();

        artifactStubFactory = new DependencyArtifactStubFactory(getTestFile("target"), true, false);
        artifactStubFactory.getWorkingDir().mkdirs();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Gets the current Maven project.
     *
     * @return the maven project
     */
    protected MavenProject getTestMavenProject() {
        return testMavenProject;
    }

    /**
     * Gets the generated report as file in the test maven project.
     *
     * @param name the name of the report
     * @return the generated report as file
     * @throws IOException if the return file doesnt exist
     */
    protected File getGeneratedReport(String name) throws IOException {
        String outputDirectory =
                getBasedir() + "/target/test/unit/" + getTestMavenProject().getArtifactId();

        File report = new File(outputDirectory, name);
        if (!report.exists()) {
            throw new IOException("File not found. Attempted: " + report);
        }

        return report;
    }

    /**
     * Generate the report and return the generated file
     *
     * @param goal the mojo goal
     * @param pluginXml the name of the XML file in "src/test/resources/plugin-configs/".
     * @return the generated HTML file
     * @throws Exception if any
     */
    protected File generateReport(String goal, String pluginXml) throws Exception {
        File pluginXmlFile = new File(getBasedir(), "src/test/resources/unit/" + pluginXml);
        AbstractJxrReport mojo = createReportMojo(goal, pluginXmlFile);
        return generateReport(mojo, pluginXmlFile);
    }

    protected AbstractJxrReport createReportMojo(String goal, File pluginXmlFile) throws Exception {
        AbstractJxrReport mojo = (AbstractJxrReport) lookupMojo(goal, pluginXmlFile);
        assertNotNull("Mojo not found.", mojo);

        LegacySupport legacySupport = lookup(LegacySupport.class);
        legacySupport.setSession(newMavenSession(new MavenProjectStub()));
        DefaultRepositorySystemSession repoSession =
                (DefaultRepositorySystemSession) legacySupport.getRepositorySession();
        repoSession.setLocalRepositoryManager(new SimpleLocalRepositoryManagerFactory()
                .newInstance(repoSession, new LocalRepository(artifactStubFactory.getWorkingDir())));

        List<MavenProject> reactorProjects =
                mojo.getReactorProjects() != null ? mojo.getReactorProjects() : Collections.emptyList();

        setVariableValueToObject(mojo, "mojoExecution", getMockMojoExecution());
        setVariableValueToObject(mojo, "session", legacySupport.getSession());
        setVariableValueToObject(mojo, "repoSession", legacySupport.getRepositorySession());
        setVariableValueToObject(mojo, "reactorProjects", reactorProjects);
        setVariableValueToObject(
                mojo, "remoteProjectRepositories", mojo.getProject().getRemoteProjectRepositories());
        setVariableValueToObject(
                mojo, "siteDirectory", new File(mojo.getProject().getBasedir(), "src/site"));
        return mojo;
    }

    protected File generateReport(AbstractJxrReport mojo, File pluginXmlFile) throws Exception {
        mojo.execute();

        ProjectBuilder builder = lookup(ProjectBuilder.class);

        ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest();
        buildingRequest.setRepositorySession(lookup(LegacySupport.class).getRepositorySession());

        testMavenProject = builder.build(pluginXmlFile, buildingRequest).getProject();

        File outputDir = mojo.getReportOutputDirectory();
        String filename = mojo.getOutputPath() + ".html";

        return new File(outputDir, filename);
    }

    /**
     * Read the contents of the specified file object into a string.
     */
    protected String readFile(File xrefTestDir, String fileName) throws IOException {
        return new String(Files.readAllBytes(xrefTestDir.toPath().resolve(fileName)));
    }

    private MojoExecution getMockMojoExecution() {
        MojoDescriptor md = new MojoDescriptor();
        md.setGoal(getGoal());

        MojoExecution me = new MojoExecution(md);

        PluginDescriptor pd = new PluginDescriptor();
        Plugin p = new Plugin();
        p.setGroupId("org.apache.maven.plugins");
        p.setArtifactId("maven-jxr-plugin");
        pd.setPlugin(p);
        md.setPluginDescriptor(pd);

        return me;
    }

    protected abstract String getGoal();
}
