/*
 * The MIT License
 *
 * Copyright 2014 Ksenia Nenasheva <ks.nenasheva@gmail.com>
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

package org.jenkinsci.plugins.securityinspector;

import hudson.Extension;
import hudson.model.AllView;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ManagementLink;
import hudson.model.Node;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.model.View;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.QueryParameter;

@Extension
public class SecurityInspectorAction extends ManagementLink {

    private final SecurityInspectorHelper helper = new SecurityInspectorHelper();
    
    public SecurityInspectorAction() {
        this.contextMap = new UserContextCache();
    } 
    
    protected Object readResolve() {
        if (contextMap == null) {
            contextMap = new UserContextCache();
        }
        return this;
    }
    
    @Nonnull
    transient UserContextCache contextMap;

    @Override
    public String getIconFileName() {
        return "secure.gif";
    }

    @Override
    public String getDisplayName() {
        return "Security Inspector";
    }
    
    @Override
    public String getDescription() {
        return "Inspect permissions configured by Jenkins security settings";
    }

    @Override
    public String getUrlName() {
        return "security-inspector";
    }

    public SecurityInspectorHelper getHelper() {
        return helper;
    }
    
    public SecurityInspectorReport getReportJob() {
        Set<TopLevelItem> items = getRequestedJobs();
        User user = getRequestedUser();
        JobReport report;

        // Impersonate to check the permission
        
        final Authentication auth;
        try {
          auth = user.impersonate();
        } catch (UsernameNotFoundException ex) {
          return new JobReport();
        }
        
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

    public SecurityInspectorReport getReportUser() {
        Set<User> users = getRequestedUsers();
        Item job = getRequestedJob();

        UserReport report = UserReport.createReport(users, job);
        return report;
    }

    public SecurityInspectorReport getReportSlave() {
        Set<Computer> computers = getRequestedSlaves();
        Set<Computer> slaves = new HashSet<Computer>();
        for (Computer c : computers) {
            Node slave = c.getNode();
            if (slave != null) {
                slaves.add(c);
            }
        }

        User user = getRequestedUser();
        SlaveReport report;

        // Impersonate to check the permission
        final Authentication auth;
        try {
          auth = user.impersonate();
        } catch (UsernameNotFoundException ex) {
          return new SlaveReport();
        }

        SecurityContext initialContext = null;
        try {
            initialContext = hudson.security.ACL.impersonate(auth);
            report = SlaveReport.createReport(slaves);
        } finally {
            if (initialContext != null) {
                SecurityContextHolder.setContext(initialContext);
            }
        }

        return report;
    }

    private View getSourceView() {
        for (View view : Jenkins.getInstance().getViews()) {
            if (view instanceof AllView) {
                return view;
            }
        }
        throw new IllegalStateException("Cannot retrieve All view");
    }

    public HttpResponse doFilterSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, UnsupportedEncodingException, ServletException, Descriptor.FormException {
        Jenkins.getActiveInstance().checkPermission(Jenkins.ADMINISTER);
        String selectedItem;
        String valid;
        StringBuilder b = new StringBuilder();
        UserSubmit action = UserSubmit.fromRequest(req);

        switch (action) {
            case Submit4jobs:
                valid = req.getParameter("_.includeRegex");
                try {
                    Pattern.compile(valid);
                } catch (PatternSyntaxException exception) {
                    return HttpResponses.redirectTo(Jenkins.getActiveInstance().getRootUrl() + "security-inspector/error");
                }
                selectedItem = req.getParameter("selectedUser");
                b.append("search_report_user_4_job");
                View sourceView = getSourceView();
                JobFilter filters = new JobFilter(req, sourceView);
                updateSearchCache(filters, selectedItem);
                break;
              
            case Submit4slaves:
                valid = req.getParameter("_.includeRegex4Slave");
                try {
                    Pattern.compile(valid);
                } catch (PatternSyntaxException exception) {
                    return HttpResponses.redirectTo(Jenkins.getActiveInstance().getRootUrl() + "security-inspector/error");
                }
                selectedItem = req.getParameter("selectedUser");
                b.append("search_report_user_4_slave");
                SlaveFilter filter4slave = new SlaveFilter(req);
                updateSearchCache(filter4slave, selectedItem);
                break;
              
            case Submit4user:
                valid = req.getParameter("_.includeRegex4User");
                try {
                    Pattern.compile(valid);
                } catch (PatternSyntaxException exception) {
                    return HttpResponses.redirectTo(Jenkins.getActiveInstance().getRootUrl() + "security-inspector/error");
                }
                selectedItem = req.getParameter("selectedJobs");
                b.append("search_report_job");
                UserFilter filter4user = new UserFilter(req);
                updateSearchCache(filter4user, selectedItem);
                break;
              
            case GoToHP:
                return HttpResponses.redirectTo(Jenkins.getActiveInstance().getRootUrl() + "security-inspector");
              
            default:
                throw new IOException("Action " + action + " is not supported");
        }

        // Redirect to the search report page
        String request = b.toString();
        return HttpResponses.redirectTo(request);
    }

    public List<Item> doAutoCompleteJob(@QueryParameter String value) {
        List<Item> c = new LinkedList<Item>();
        List<Item> items = Jenkins.getInstance().getAllItems();
        for (Item item : items) {
            if (item.toString().toLowerCase().startsWith(value.toLowerCase())) {
                c.add(item);
            }
        }
        return c;
    }

    public void doGoHome(StaplerRequest req, StaplerResponse rsp) throws IOException, UnsupportedEncodingException, ServletException, Descriptor.FormException {
        Jenkins.getActiveInstance().checkPermission(Jenkins.ADMINISTER);
        GoHome action = GoHome.fromRequest(req);
        switch (action) {
            case GoToJF:
                rsp.sendRedirect("job-filter");
                break;
            case GoToSF:
                rsp.sendRedirect("slave-filter");
                break;
            case GoToUF:
                rsp.sendRedirect("user-filter");
                break;
                default:
                throw new IOException("Action " + action + " is not supported");
        }           
    }
    
    /**
     * Get Jobs/Slaves/Users from context
     */
    public Set<TopLevelItem> getRequestedJobs() throws HttpResponses.HttpResponseException {
        UserContext context = contextMap.get(getSessionId());
        View sourceView = getSourceView();
        Set<TopLevelItem> res;
        List<TopLevelItem> selectedJobs = context.getJobFilter().doFilter(Jenkins.getInstance().getAllItems(TopLevelItem.class), sourceView);
        res = new HashSet(selectedJobs.size());
        for (TopLevelItem item : selectedJobs) {
            if (item != null) {
                res.add(item);
            }
        }
        return res;
    }
    
    public Set<Computer> getRequestedSlaves() throws HttpResponses.HttpResponseException {
        UserContext context = contextMap.get(getSessionId());
        Set<Computer> res;
        List<Computer> selectedSlaves = context.getSlaveFilter().doFilter();
        res = new HashSet<Computer>(selectedSlaves.size());
        for (Computer item : selectedSlaves) {
            if (item != null && item instanceof Computer) {
                res.add((Computer) item);
            }
        }
        return res;
    }

    public Set<User> getRequestedUsers() throws HttpResponses.HttpResponseException {
        UserContext context = contextMap.get(getSessionId());
        Set<User> res;
        List<User> selectedUsers = context.getUserFilter().doFilter();
        res = new HashSet<User>(selectedUsers.size());
        for (User item : selectedUsers) {
            if (item != null && item instanceof User) {
                res.add((User) item);
            }
        }
        return res;
    }
    
    /**
     * Get selected user/job from context
     */
    public User getRequestedUser() throws HttpResponses.HttpResponseException {
        UserContext context = contextMap.get(getSessionId());
        String userId = context.getItem();
        User user = User.get(userId, false, null);
        if (user == null) {
            throw HttpResponses.error(404, "User " + userId + " does not exists");
        }
        return user;
    }

    public Item getRequestedJob() throws HttpResponses.HttpResponseException {
        UserContext context = contextMap.get(getSessionId());
        String jobName = context.getItem();
        Item job = Jenkins.getInstance().getItemByFullName(jobName, Item.class);
        if (job == null) {
            throw HttpResponses.error(404, "Job " + jobName + " does not exists");
        }
        return job;
    }

    /**
     * Buttons:
     * - Submit Jobs/Slaves/Users reports
     * - Go to Home Page
     */
    enum UserSubmit {

        Submit4jobs,
        Submit4slaves,
        Submit4user,
        GoToHP;

        static UserSubmit fromRequest(StaplerRequest req) throws IOException {
            Map map = req.getParameterMap();
            for (UserSubmit val : UserSubmit.values()) {
                if (map.containsKey(val.toString())) {
                    return val;
                }
            }
            throw new IOException("Cannot find an action in the reqest");
        }
    }
    
    /**
     * Buttons:
     * Go to page with filters
     */
    enum GoHome {

        GoToJF,
        GoToSF,
        GoToUF;

        static GoHome fromRequest(StaplerRequest req) throws IOException {
            Map map = req.getParameterMap();
            for (GoHome val : GoHome.values()) {
                if (map.containsKey(val.toString())) {
                    return val;
                }
            }
            throw new IOException("Cannot find an action in the reqest");
        }
    }
    
    /**
     * Gets identifier of the current session.
     * @return Unique id of the current session.
     */
    public static String getSessionId() {
        return Stapler.getCurrentRequest().getSession().getId(); 
    }
    
    public boolean hasConfiguredFilters() {
        return contextMap.containsKey(getSessionId());
    }
    
    /**
     * Cleans internal cache of JSON Objects for the session.
     * @todo Cleanup approach, replace for URL-based parameterization
     * @return Current Session Id
     */
    public String cleanCache() {
        final String sessionId = getSessionId();
        contextMap.flush(sessionId);      
        return sessionId;
    }
    
    public void updateSearchCache(JobFilter jobFilter, String item) {
        cleanCache();
        // Put Context to the map
        contextMap.put(getSessionId(), new UserContext(jobFilter, item));
    }
    
    public void updateSearchCache(SlaveFilter slaveFilter, String item) {
        cleanCache();
        // Put Context to the map
        contextMap.put(getSessionId(), new UserContext(slaveFilter, item));
    }
    
    public void updateSearchCache(UserFilter userFilter, String item) {
        cleanCache();
        // Put Context to the map
        contextMap.put(getSessionId(), new UserContext(userFilter, item));
    }
}
