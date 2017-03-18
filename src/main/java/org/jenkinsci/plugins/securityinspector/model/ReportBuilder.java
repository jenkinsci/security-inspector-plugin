/*
 * The MIT License
 *
 * Copyright (c) 2016 Oleg Nenashev.
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
package org.jenkinsci.plugins.securityinspector.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.securityinspector.util.JenkinsHelper;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Defines a report builder for the Security Inspector UI.
 *
 * @author Oleg Nenashev
 */
public abstract class ReportBuilder implements ExtensionPoint {

    @Nonnull
    public abstract Type getType();

    @Nonnull
    public abstract String getIcon();

    @Nonnull
    public abstract String getIndex();

    @Nonnull
    public abstract String getDisplayName();

    @Nonnull
    public abstract String getDescription();
    
    @Nonnull
    @Restricted(NoExternalUse.class)
    public abstract SecurityInspectorReport getReport();
    
    @Nonnull
    public static ExtensionList<ReportBuilder> all() {
        return ExtensionList.lookup(ReportBuilder.class);
    }

    @Nonnull
    public static List<ReportBuilder> all(@Nonnull Type type) {
        final ExtensionList<ReportBuilder> all = all();
        final ArrayList<ReportBuilder> res = new ArrayList<>();
        for (ReportBuilder rb : all) {
            if (rb.getType() == type) {
                res.add(rb);
            }
        }
        return res;
    }

    public abstract void processParameters(@Nonnull StaplerRequest req)
            throws Descriptor.FormException, ServletException;

    @Nonnull
    @Restricted(NoExternalUse.class)
    public HttpResponse doFilterSubmit(@Nonnull StaplerRequest req)
            throws ServletException, Descriptor.FormException {

        final Jenkins jenkins = JenkinsHelper.getInstanceOrFail();
        jenkins.checkPermission(Jenkins.ADMINISTER);

        SubmittedOperation action = SubmittedOperation.fromRequest(req);
        switch (action) {
            case Submit:
                processParameters(req);
                break;

            case Back:
                return HttpResponses.redirectTo(jenkins.getRootUrl() + "security-inspector");

            default:
                throw new Descriptor.FormException("Action " + action + " is not supported", "submit");
        }

        // Redirect to the search report page
        return HttpResponses.redirectTo("report");
    }
    
    @Restricted(NoExternalUse.class)
    public void doProcessReportAction(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp)
            throws ServletException, Descriptor.FormException, IOException {

        final Jenkins jenkins = JenkinsHelper.getInstanceOrFail();
        jenkins.checkPermission(Jenkins.ADMINISTER);

        SubmittedOperation action = SubmittedOperation.fromRequest(req);
        switch (action) {
            case GoHome:
                rsp.sendRedirect("..");
                break;

            case Download:
                doDownloadReport(rsp);
                break;

            default:
                throw new Descriptor.FormException("Action " + action + " is not supported", "submit");
        }
    }

    /**
     * Buttons: 
     * - Submit report 
     * - Go to Home Page (for report and for configure report pages)
     * - Download report
     */
    private enum SubmittedOperation {

        Submit,
        Back,
        GoHome,
        Download;

        /**
         * Locates the operation in the submitted form.
         *
         * @param req Request
         * @return Located operation
         * @throws Descriptor.FormException Cannot find any command field from
         * the enum
         */
        @Nonnull
        static SubmittedOperation fromRequest(@Nonnull StaplerRequest req) 
                throws Descriptor.FormException {
            final Map<?, ?> map = req.getParameterMap();
            for (SubmittedOperation val : SubmittedOperation.values()) {
                if (map.containsKey(val.toString())) {
                    return val;
                }
            }
            throw new Descriptor.FormException("Cannot find an action in the request", "submit");
        }
    }

    public static enum Type {

        ITEM,
        USER,
        COMPUTER
    }
    
    private void doDownloadReport(@Nonnull StaplerResponse rsp) {
        
        SecurityInspectorReport report4Download = getReport();
        
        rsp.setCharacterEncoding("UTF_8");
        rsp.setContentType("text/csv;charset=UTF-8");
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        rsp.setHeader("Content-Disposition", "attachment; "
                + "filename=\"report-for-"
                +report4Download.getReportTargetName()
                +"-" + f.format(new Date()) + ".csv\"");
        
        try (OutputStream outputStream = rsp.getOutputStream()) {
            String report = report4Download.getReportInCSV();
            outputStream.write(report.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            outputStream.flush();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        
    }
}
