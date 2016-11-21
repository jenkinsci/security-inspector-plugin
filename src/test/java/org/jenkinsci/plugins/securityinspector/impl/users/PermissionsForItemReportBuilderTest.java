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
package org.jenkinsci.plugins.securityinspector.impl.users;

import hudson.model.Item;
import hudson.model.TopLevelItem;
import java.util.HashSet;
import java.util.Set;
import org.jenkinsci.plugins.securityinspector.util.PermissionReportAssert;
import org.jenkinsci.plugins.securityinspector.util.ReportBuilderTestBase;
import org.junit.Test;

/**
 * Tests of {@link PermissionsForItemReportBuilder}.
 * @author Ksenia Nenasheva <ks.nenasheva@gmail.com>
 */
public class PermissionsForItemReportBuilderTest  extends ReportBuilderTestBase<PermissionsForItemReportBuilder> {

    public PermissionsForItemReportBuilderTest() {
        super(PermissionsForItemReportBuilder.class);
    }
    
    @Test
    public void shouldReportAdminProperly() throws Exception {
        initializeDefaultMatrixAuthSecurity();
        final PermissionsForItemReportBuilder builder = getBuilder();
        
        final PermissionsForItemReportBuilder.ReportImpl report = new PermissionsForItemReportBuilder.ReportImpl(j.jenkins.getUser("admin"));
        
        final Set<TopLevelItem> allItems = new HashSet<>(j.jenkins.getAllItems(TopLevelItem.class));     
        report.generateReport(allItems);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project1"), 
                Item.BUILD, Item.CANCEL, Item.CONFIGURE, Item.CREATE, Item.DELETE, 
                Item.DISCOVER, Item.READ, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project2"), 
                Item.BUILD, Item.CANCEL, Item.CONFIGURE, Item.CREATE, Item.DELETE, 
                Item.DISCOVER, Item.READ, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("folder"), 
                Item.BUILD, Item.CANCEL, Item.CONFIGURE, Item.CREATE, Item.DELETE, 
                Item.DISCOVER, Item.READ, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.BUILD, Item.CANCEL, Item.CONFIGURE, Item.CREATE, Item.DELETE, 
                Item.DISCOVER, Item.READ, Item.WORKSPACE);

    }
    
    @Test
    public void shouldReportUser1Properly() throws Exception {
        initializeDefaultMatrixAuthSecurity();
        final PermissionsForItemReportBuilder builder = getBuilder();
        
        final PermissionsForItemReportBuilder.ReportImpl report = new PermissionsForItemReportBuilder.ReportImpl(j.jenkins.getUser("user1"));
        
        final Set<TopLevelItem> allItems = new HashSet<>(j.jenkins.getAllItems(TopLevelItem.class));     
        report.generateReport(allItems);
                
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project1"), 
                Item.READ, Item.CONFIGURE, Item.BUILD, Item.CANCEL, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("project1"), 
                Item.CREATE, Item.DELETE, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project2"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("project2"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("folder"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("folder"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
    } 
    
    @Test
    public void shouldReportUser2Properly() throws Exception {
        initializeDefaultMatrixAuthSecurity();
        final PermissionsForItemReportBuilder builder = getBuilder();
        
        final PermissionsForItemReportBuilder.ReportImpl report = new PermissionsForItemReportBuilder.ReportImpl(j.jenkins.getUser("user2"));
        
        final Set<TopLevelItem> allItems = new HashSet<>(j.jenkins.getAllItems(TopLevelItem.class));     
        report.generateReport(allItems);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project1"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("project1"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project2"), 
                Item.READ, Item.DELETE, Item.BUILD, Item.CANCEL, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("project2"), 
                Item.CREATE, Item.CONFIGURE, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("folder"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("folder"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
    }
    
    @Test
    public void shouldReportUser3Properly() throws Exception {
        initializeDefaultMatrixAuthSecurity();
        final PermissionsForItemReportBuilder builder = getBuilder();
        
        final PermissionsForItemReportBuilder.ReportImpl report = new PermissionsForItemReportBuilder.ReportImpl(j.jenkins.getUser("user3"));
        
        final Set<TopLevelItem> allItems = new HashSet<>(j.jenkins.getAllItems(TopLevelItem.class));     
        report.generateReport(allItems);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project1"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("project1"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("project2"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("project2"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItem("folder"), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItem("folder"), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
        
        PermissionReportAssert.assertHasPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.READ, Item.DISCOVER);
        PermissionReportAssert.assertHasNotPermissions(report, j.jenkins.getItemByFullName("folder/projectInFolder", TopLevelItem.class), 
                Item.CONFIGURE, Item.CREATE, Item.DELETE, Item.BUILD, Item.CANCEL, Item.WORKSPACE);
    }
}
