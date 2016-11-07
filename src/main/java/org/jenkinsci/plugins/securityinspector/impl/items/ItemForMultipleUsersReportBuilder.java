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
package org.jenkinsci.plugins.securityinspector.impl.items;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.User;
import hudson.model.View;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
import org.jenkinsci.plugins.securityinspector.util.UserFilter;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author Oleg Nenashev
 */
@Extension
public class ItemForMultipleUsersReportBuilder extends ItemReportBuilder {

    @Override
    public String getIcon() {
        return "user.png";
    }

    @Override
    public String getIndex() {
        return "users-for-item";
    }

    @Override
    public String getDisplayName() {
        return "Multiple users, single job";
    }

    @Override
    public String getDescription() {
        return "Display users permissions for the specified item";
    }

    @Override
    public void processParameters(StaplerRequest req) throws Descriptor.FormException, ServletException {
        final String regex = req.getParameter("_.includeRegex4User");
        try {
          Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
          throw new Descriptor.FormException(exception, "includeRegex4User");
        }
        final String selectedItem = req.getParameter("selectedJobs");
        UserFilter filter4user = new UserFilter(req);
        UserContextCache.updateSearchCache(filter4user, selectedItem);
    }
    
    //TODO: fix rawtype before the release
    @Nonnull
    @Restricted(NoExternalUse.class)
    public SecurityInspectorReport getReportUser() {
        Set<User> users = getRequestedUsers();
        Item job = getRequestedJob();

        ReportImpl report = ReportImpl.createReport(users, job);
        return report;
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<User> getRequestedUsers() throws HttpResponses.HttpResponseException {
        UserContext context = UserContextCache.getInstance().get(getSessionId());
        if (context == null) {
            // TODO: 
            throw HttpResponses.error(404, "Context has not been found");
        }

        final UserFilter userFilter = context.getUserFilter();
        if (userFilter == null) {
            throw HttpResponses.error(500, "The retrieved context does not contain user filter settings");
        }

        List<User> selectedUsers = userFilter.doFilter();
        final Set<User> res = new HashSet<>(selectedUsers.size());
        for (User item : selectedUsers) {
            if (item != null) {
                res.add(item);
            }
        }
        return res;
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public Item getRequestedJob() throws HttpResponses.HttpResponseException {
        UserContext context = UserContextCache.getInstance().get(getSessionId());
        if (context == null) {
            // TODO: 
            throw HttpResponses.error(404, "Context has not been found");
        }
        String jobName = context.getItem();
        Item job = JenkinsHelper.getInstanceOrFail().getItemByFullName(jobName, Item.class);
        if (job == null) {
            throw HttpResponses.error(404, "Job " + jobName + " does not exist");
        }
        return job;
    }

    public static class ReportImpl extends PermissionReport<User, Boolean> {

        @Nonnull
        final Item job4report;

        private ReportImpl(@Nonnull Item job) {
            this.job4report = job;
        }

        @Override
        protected Boolean getEntryReport(User column, Permission item) {
            // Impersonate to check the permission
            final Authentication auth;
            Boolean result;

            try {
                auth = column.impersonate();
            } catch (UsernameNotFoundException ex) {
                return Boolean.FALSE;
            }

            SecurityContext initialContext = null;
            try {
                initialContext = hudson.security.ACL.impersonate(auth);
                result = job4report.hasPermission(item);
            } finally {
                if (initialContext != null) {
                    SecurityContextHolder.setContext(initialContext);
                }
            }
            return result;
        }

        public final void generateReport(@Nonnull Set<User> rows) {
            Set<PermissionGroup> groups = new HashSet<>(PermissionGroup.getAll());
            groups.remove(PermissionGroup.get(Permission.class));
            groups.remove(PermissionGroup.get(Hudson.class));
            groups.remove(PermissionGroup.get(Computer.class));
            groups.remove(PermissionGroup.get(View.class));

            super.generateReport(rows, groups);
        }

        public static ReportImpl createReport(@Nonnull Set<User> rows, @Nonnull Item job) {
            ReportImpl report = new ReportImpl(job);
            report.generateReport(rows);
            return report;
        }

        @Override
        public String getRowColumnHeader() {
            return Messages.UserReport_RowColumnHeader();
        }

        @Override
        public String getRowTitle(User row) {
            return row.getId();
        }

        @Override
        public boolean isEntryReportOk(User row, Permission item, Boolean report) {
            return report != null ? report : false;
        }
    }
}
