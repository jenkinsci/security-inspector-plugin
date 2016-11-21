/*
 * The MIT License
 *
 * Copyright 2016 Ksenia Nenasheva <ks.nenasheva@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.securityinspector.util;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Computer;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.JobProperty;
import hudson.model.User;
import hudson.security.AuthorizationMatrixProperty;
import hudson.security.Permission;
import hudson.security.ProjectMatrixAuthorizationStrategy;
import hudson.slaves.DumbSlave;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import org.jenkinsci.plugins.securityinspector.model.ReportBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Base class for testing {@link ReportBuilder}s.
 * @param <T> Class of the report builder to be tested
 * @author Ksenia Nenasheva <ks.nenasheva@gmail.com>
 */
public class ReportBuilderTestBase <T extends ReportBuilder> {
    
    @Rule
    public final JenkinsRule j = new JenkinsRule();
    
    private Class<T> myClass;

    public ReportBuilderTestBase(Class<T> myClass) {
        this.myClass = myClass;
    }
    
    @Nonnull
    public T getBuilder() throws AssertionError {
        final T builder = j.jenkins.getExtensionList(ReportBuilder.class).get(myClass);
        assertThat("Extension point must have been registered", builder, notNullValue());
        return builder;
    }
    
    @Test
    public void shouldOfferTheReportInUI() throws Exception {
        final T builder = getBuilder();
        final JenkinsRule.WebClient wc = j.createWebClient();
        final String rootPageStr = wc.goTo("security-inspector").asText();
        assertThat("Description must be offered for the item", rootPageStr, containsString(builder.getDescription()));
        assertThat("Display name must be offered for the item", rootPageStr, containsString(builder.getDisplayName()));
    }
    
    protected void initializeDefaultMatrixAuthSecurity() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        User.get("admin");
        User.get("user1");
        User.get("user2");
        User user3 = User.get("user3");
        j.jenkins.save();
        user3.save();
        
        FreeStyleProject project1 = j.createFreeStyleProject("project1");
        FreeStyleProject project2 = j.createFreeStyleProject("project2");
        final Folder f = j.createProject(Folder.class, "folder");
        f.createProject(FreeStyleProject.class, "projectInFolder");
        
        DumbSlave slave1 = j.createSlave("slave1", null, null);
        slave1.save();
                
        // Initialize global security
        final ProjectMatrixAuthorizationStrategy strategy = new ProjectMatrixAuthorizationStrategy();
        strategy.add(Jenkins.ADMINISTER, "admin");
        strategy.add(Jenkins.READ, "user1");
        strategy.add(Jenkins.READ, "user2");
        strategy.add(Jenkins.READ, "user3");
        strategy.add(Item.READ, "user1");
        strategy.add(Item.READ, "user2");
        strategy.add(Item.READ, "user3");
        j.jenkins.setAuthorizationStrategy(strategy);
        
        // Setup local security for project 1
        {
            final Set<String> user1 = Collections.singleton("user1");
            final Map<Permission, Set<String>> permissions = new HashMap<>();
            permissions.put(Item.BUILD, user1);
            permissions.put(Item.CONFIGURE, user1);
            final JobProperty prop = new AuthorizationMatrixProperty(permissions);
            project1.addProperty(prop);
        }
        
        // Setup local security for project 2
        {
            final Set<String> user2 = Collections.singleton("user2");
            final Map<Permission, Set<String>> permissions = new HashMap<>();
            permissions.put(Item.BUILD, user2);
            permissions.put(Item.DELETE, user2);
            final JobProperty prop = new AuthorizationMatrixProperty(permissions);
            project2.addProperty(prop);
        }
    }
}
