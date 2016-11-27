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
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.User;
import hudson.model.View;
import hudson.scm.SCM;
import hudson.security.AuthorizationStrategy;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
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
import org.jenkinsci.plugins.securityinspector.util.ComputerFilter;
import org.jenkinsci.plugins.securityinspector.util.JenkinsHelper;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Builds a permission report for computers.
 *
 * @author Oleg Nenashev
 */
@Extension(ordinal = 0)
public class PermissionsForComputerReportBuilder extends UserReportBuilder {

    @Override
    public String getIcon() {
        return "fingerprint.png";
    }

    @Override
    public String getIndex() {
        return "slave-filter";
    }

    @Override
    public String getDisplayName() {
        return "Single user, multiple nodes";
    }

    @Override
    public String getDescription() {
        return "Display node permissions for the specified user";
    }

    @Override
    public void processParameters(StaplerRequest req) throws Descriptor.FormException {
        String valid = req.getParameter("_.includeRegex4Slave");
        try {
            Pattern.compile(valid);
        } catch (PatternSyntaxException exception) {
            throw new Descriptor.FormException(exception, "includeRegex4Slave");
        }
        String selectedItem = req.getParameter("selectedUser");
        ComputerFilter filter4slave = new ComputerFilter(req);
        UserContextCache.updateSearchCache(filter4slave, selectedItem);
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public Set<Computer> getRequestedSlaves() throws HttpResponses.HttpResponseException {
        UserContext context = UserContextCache.getInstance().get(getSessionId());
        if (context == null) {
            // TODO:  What todo?
            throw HttpResponses.error(404, "Context has not been found");
        }

        final ComputerFilter slaveFilter = context.getSlaveFilter();
        if (slaveFilter == null) {
            throw HttpResponses.error(500, "The retrieved context does not contain slave filter settings");
        }

        List<Computer> selectedSlaves = slaveFilter.doFilter();
        final Set<Computer> res = new HashSet<>(selectedSlaves.size());
        for (Computer item : selectedSlaves) {
            if (item != null) {
                res.add(item);
            }
        }
        return res;
    }

    //TODO: Rename Slave => Node
    @Nonnull
    @Restricted(NoExternalUse.class)
    public SecurityInspectorReport getReportSlave() {
        Set<Computer> computers = getRequestedSlaves();
        Set<Computer> slaves = new HashSet<>();
        for (Computer c : computers) {
            Node slave = c.getNode();
            if (slave != null) {
                slaves.add(c);
            }
        }

        final User user = getRequestedUser();
        ReportImpl report;

        // Impersonate to check the permission
        final Authentication auth;
        try {
            auth = user.impersonate();
        } catch (UsernameNotFoundException ex) {
            return new ReportImpl(user);
        }

        SecurityContext initialContext = null;
        try {
            initialContext = hudson.security.ACL.impersonate(auth);
            report = ReportImpl.createReport(slaves, user);
        } finally {
            if (initialContext != null) {
                SecurityContextHolder.setContext(initialContext);
            }
        }

        return report;
    }

    /**
     * Reports user permissions for a specified computer.
     *
     * @author Ksenia Nenasheva
     */
    public static class ReportImpl extends PermissionReport<Computer, Boolean> {

        @Nonnull
        final User user4report;

        /**package*/ ReportImpl(@Nonnull User user) {
            this.user4report = user;
        }
        
        @Override
        protected Boolean getEntryReport(Computer column, Permission item) {
            
            final Authentication auth;
            try {
                auth = user4report.impersonate();
            } catch (UsernameNotFoundException ex) {
                return Boolean.FALSE;
            }
            
            SecurityContext initialContext = null;
            AuthorizationStrategy strategy = JenkinsHelper.getInstanceOrFail().getAuthorizationStrategy();
            try {
                initialContext = hudson.security.ACL.impersonate(auth);
                return strategy.getACL(column).hasPermission(item);
            } finally {
                if (initialContext != null) {
                    SecurityContextHolder.setContext(initialContext);
                }
            }
        }

        public final void generateReport(@Nonnull Set<Computer> rows) {
            Set<PermissionGroup> groups = new HashSet<>(PermissionGroup.getAll());
            groups.remove(PermissionGroup.get(Permission.class));
            groups.remove(PermissionGroup.get(Hudson.class));
            groups.remove(PermissionGroup.get(View.class));
            groups.remove(PermissionGroup.get(Job.class));
            groups.remove(PermissionGroup.get(Item.class));
            groups.remove(PermissionGroup.get(SCM.class));
            groups.remove(PermissionGroup.get(Run.class));

            super.generateReport(rows, groups);
        }

        public static ReportImpl createReport(@Nonnull Set<Computer> rows, @Nonnull User user) {
            ReportImpl report = new ReportImpl(user);
            report.generateReport(rows);
            return report;
        }

        @Override
        public String getRowColumnHeader() {
            return Messages.SlaveReport_RowColumnHeader();
        }

        @Override
        public String getRowTitle(Computer row) {
            return row.getDisplayName();
        }

        @Override
        public boolean isEntryReportOk(Computer row, Permission item, Boolean report) {
            return report != null ? report : false;
        }
    }
}
