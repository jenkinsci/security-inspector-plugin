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
package org.jenkinsci.plugins.securityinspector.impl.users;

import hudson.Extension;
import hudson.model.AllView;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.model.View;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.jenkinsci.plugins.securityinspector.Messages;
import static org.jenkinsci.plugins.securityinspector.SecurityInspectorAction.getSessionId;
import org.jenkinsci.plugins.securityinspector.UserContext;
import org.jenkinsci.plugins.securityinspector.UserContextCache;
import org.jenkinsci.plugins.securityinspector.model.PermissionReport;
import org.jenkinsci.plugins.securityinspector.model.SecurityInspectorReport;
import org.jenkinsci.plugins.securityinspector.util.JenkinsHelper;
import org.jenkinsci.plugins.securityinspector.util.JobFilter;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Reports user permissions for {@link Item}.
 */
@Extension(ordinal = 1)
public class PermissionsForItemReportBuilder extends UserReportBuilder {

    @Override
    public String getIcon() {
        return "fingerprint.png";
    }

    @Override
    public String getIndex() {
        return "items-for-user";
    }

    @Override
    public String getDisplayName() {
        return "Single user, multiple jobs";
    }

    @Override
    public String getDescription() {
        return "Display job permissions for the specified user";
    }

    @Override
    public void processParameters(StaplerRequest req) throws Descriptor.FormException, ServletException {
        final String regex = req.getParameter("_.includeRegex");
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            throw new Descriptor.FormException(exception, "includeRegex");
        }
        final String selectedItem = req.getParameter("selectedUser");
        JobFilter filters = new JobFilter(req);
        UserContextCache.updateSearchCache(filters, selectedItem);
    }

    //TODO: fix rawtype before the release
    @Nonnull
    @Restricted(NoExternalUse.class)
    public SecurityInspectorReport getReportJob() {
        Set<TopLevelItem> items = getRequestedJobs();
        User user = getRequestedUser();
        final JobReport report;

        // Impersonate to check the permission
        final Authentication auth;
        try {
            auth = user.impersonate();
        } catch (UsernameNotFoundException ex) {
            return new JobReport();
        }

        //TODO: rework the logic to guarantee that report is initialized
        SecurityContext initialContext = null;
        try {
            initialContext = hudson.security.ACL.impersonate(auth);
            report = JobReport.createReport(items);
        } finally {
            if (initialContext != null) {
                SecurityContextHolder.setContext(initialContext);
            }
        }
        return report;
    }

    @CheckForNull
    private AllView getAllView() {
        for (View view : JenkinsHelper.getInstanceOrFail().getViews()) {
            if (view instanceof AllView) {
                return (AllView) view;
            }
        }
        return null;
    }

    /**
     * Get Jobs/Slaves/Users from the context
     *
     * @return res List of the requested jobs
     */
    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<TopLevelItem> getRequestedJobs() throws HttpResponses.HttpResponseException {
        final UserContext context = UserContextCache.getInstance().get(getSessionId());
        if (context == null) {
            // TODO: 
            throw HttpResponses.error(404, "Context has not been found");
        }

        final JobFilter jobfilter = context.getJobFilter();
        if (jobfilter == null) {
            throw HttpResponses.error(500, "The retrieved context does not contain job filter settings");
        }

        // TODO: Ideally the plugin should not depend on the AllView existense
        final AllView sourceView = getAllView();
        if (sourceView == null) {
            throw HttpResponses.error(404, "Cannot find the All view in the Jenkins root");
        }

        List<TopLevelItem> selectedJobs = jobfilter.doFilter(sourceView);
        final Set<TopLevelItem> res = new HashSet<>(selectedJobs.size());
        for (TopLevelItem item : selectedJobs) {
            if (item != null) {
                res.add(item);
            }
        }
        return res;
    }

    public static class JobReport extends PermissionReport<TopLevelItem, Boolean> {

        @Override
        protected Boolean getEntryReport(TopLevelItem column, Permission item) {
            Item i = JenkinsHelper.getInstanceOrFail().getItemByFullName(column.getFullName());
            if (i == null) {
                // Item is not accessible anymore, so we cannot check it's permission
                return false;
            }
            return i.hasPermission(item);
        }

        public final void generateReport(@Nonnull Set<TopLevelItem> rows) {
            Set<PermissionGroup> groups = new HashSet<PermissionGroup>(PermissionGroup.getAll());
            groups.remove(PermissionGroup.get(Permission.class));
            groups.remove(PermissionGroup.get(Hudson.class));
            groups.remove(PermissionGroup.get(Computer.class));
            groups.remove(PermissionGroup.get(View.class));

            super.generateReport(rows, groups);
        }

        @Nonnull
        public static JobReport createReport(@Nonnull Set<TopLevelItem> rows) {
            JobReport report = new JobReport();
            report.generateReport(rows);
            return report;
        }

        @Override
        public String getRowColumnHeader() {
            return Messages.JobReport_RowColumnHeader();
        }

        @Override
        public String getRowTitle(TopLevelItem row) {
            return row.getFullDisplayName();
        }

        @Override
        public boolean isEntryReportOk(TopLevelItem row, Permission item, Boolean report) {
            return report != null ? report : false;
        }
    }
}
