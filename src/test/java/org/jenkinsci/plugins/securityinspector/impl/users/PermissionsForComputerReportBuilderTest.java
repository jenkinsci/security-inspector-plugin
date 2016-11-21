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

import hudson.model.Computer;
//import hudson.model.Item;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;
//import org.jenkinsci.plugins.securityinspector.util.JenkinsHelper;
//import org.jenkinsci.plugins.securityinspector.util.PermissionReportAssert;
import org.jenkinsci.plugins.securityinspector.util.ReportBuilderTestBase;
import org.junit.Test;

/**
 * Tests of {@link PermissionsForComputerReportBuilder}.
 * @author Ksenia Nenasheva <ks.nenasheva@gmail.com>
 */
public class PermissionsForComputerReportBuilderTest extends ReportBuilderTestBase<PermissionsForComputerReportBuilder> {

    public PermissionsForComputerReportBuilderTest() {
        super(PermissionsForComputerReportBuilder.class);
    }
    
    @Test
    public void shouldReportUser1Properly() throws Exception {
        initializeDefaultMatrixAuthSecurity();
        final PermissionsForComputerReportBuilder builder = getBuilder();

        final PermissionsForComputerReportBuilder.ReportImpl report = new PermissionsForComputerReportBuilder.ReportImpl(j.jenkins.getUser("user1"));
        Set<Computer> computers = new HashSet<>(Arrays.asList(j.jenkins.getComputers()));
        report.generateReport(computers);
        
        //PermissionReportAssert.assertHasPermissions(report, JenkinsHelper.getInstanceOrFail().getComputer("master"), 
        //        Item.READ, Item.CONFIGURE, Item.BUILD, Item.CANCEL, Item.DISCOVER);    
    }
    
}
