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
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.AllView;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ManagementLink;
import hudson.model.Node;
import hudson.model.TopLevelItem;
import hudson.model.User;
import hudson.model.View;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.QueryParameter;

@Extension
public class SecurityInspectorAction extends ManagementLink {

    private final SecurityInspectorHelper helper = new SecurityInspectorHelper();

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
        Set<Job> items = getRequestedJobs();
        User user = getRequestedUser();
        JobReport report;

        // Impersonate to check the permission
        Authentication auth = user.impersonate();
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
        //Set<User> users = new HashSet<User>(User.getAll());
        Set<User> users = getRequestedUsers();
        Item job = getRequestedJob();

        UserReport report = UserReport.createReport(users, job);
        return report;
    }

    public SecurityInspectorReport getReportSlave() {
        //Set<Slave> items = new HashSet<Slave>(Jenkins.getInstance().getAllItems(Slave.class));

        Set<Computer> computers = getRequestedSlaves();
        //Slave slave = Computer::getNode(computers);
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
        Authentication auth = user.impersonate();
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

    public void doUserSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, UnsupportedEncodingException, ServletException, Descriptor.FormException {
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        String selectedUser = req.getParameter("selectedUser");
        StringBuilder b = new StringBuilder();

        UserSubmit action = UserSubmit.fromRequest(req);

        switch (action) {
            case Submit4jobs:
                b.append("search_report_user_4_job?user=").append(selectedUser);
                View sourceView = getSourceView();
                JobFilter filters = new JobFilter(req, sourceView);
                List<TopLevelItem> selectedJobs = filters.doFilter(Jenkins.getInstance().getItems(), sourceView);
                for (TopLevelItem item : selectedJobs) {
                    b.append("&job=").append(item.getName());
                }
                break;
            case Submit4slaves:
                b.append("search_report_user_4_slave?user=").append(selectedUser);
                SlaveFilter filter4slave = new SlaveFilter(req);
                List<Computer> selectedSlaves = filter4slave.doFilter();
                for (Computer item : selectedSlaves) {
                    b.append("&slave=").append(item.getName());
                }
                break;
            default:
                throw new IOException("Action " + action + " is not supported");
        }

        // Redirect to the search report page
        String request = b.toString();
        rsp.sendRedirect(request);
    }

    public void doJobSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, UnsupportedEncodingException, ServletException, Descriptor.FormException {
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
        String selectedJobs = req.getParameter("selectedJobs");
        String valid = req.getParameter("_.includeRegex4User");
        try {
            Pattern.compile(valid);
            StringBuilder b = new StringBuilder("search_report_job?job=" + selectedJobs);

            UserFilter filter4user = new UserFilter(req);
            List<User> selectedUsers = filter4user.doFilter();

            for (User item : selectedUsers) {
                b.append("&user=").append(item.getDisplayName());
            }

            String request = b.toString();
            rsp.sendRedirect(request);
        } catch (PatternSyntaxException exception) {
            String error = exception.getDescription();
            String backURL = "user-filter";
            rsp.sendRedirect("error");
        }
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
        Hudson.getInstance().checkPermission(Hudson.ADMINISTER);
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

    public Set<Job> getRequestedJobs() throws HttpResponses.HttpResponseException {
        String[] jobNames = Stapler.getCurrentRequest().getParameterValues("job");
        Set<Job> res;
        if (jobNames == null) {
            List<AbstractProject> items = Jenkins.getInstance().getAllItems(AbstractProject.class);
            res = new HashSet<Job>(items.size());
            for (AbstractProject item : items) {
                if (item != null && item instanceof TopLevelItem) {
                    res.add(item);
                }
            }
        } else {
            res = new HashSet<Job>(jobNames.length);
            for (String jobName : jobNames) {
                TopLevelItem item = Jenkins.getInstance().getItem(jobName);
                if (item != null && item instanceof Job) {
                    res.add((Job) item);
                }
            }
        }
        return res;
    }

    public User getRequestedUser() throws HttpResponses.HttpResponseException {
        String userId = Stapler.getCurrentRequest().getParameter("user");
        if (userId == null) {
            throw HttpResponses.error(404, "'user' has not been specified");
        }
        User user = User.get(userId, false, null);
        if (user == null) {
            throw HttpResponses.error(404, "User " + userId + " does not exists");
        }
        return user;
    }

    public Item getRequestedJob() throws HttpResponses.HttpResponseException {
        String jobName = Stapler.getCurrentRequest().getParameter("job");
        if (jobName == null) {
            throw HttpResponses.error(404, "'job' has not been specified");
        }
        Item job = Jenkins.getInstance().getItem(jobName);
        if (job == null) {
            throw HttpResponses.error(404, "Job " + jobName + " does not exists");
        }
        return job;
    }

    public Set<Computer> getRequestedSlaves() throws HttpResponses.HttpResponseException {
        String[] slaveNames = Stapler.getCurrentRequest().getParameterValues("slave");
        Set<Computer> res;
        if (slaveNames == null) {
            Computer[] items = Jenkins.getInstance().getComputers();
            res = new HashSet<Computer>(items.length);
            for (Computer item : items) {
                res.add((Computer) item);
            }
        } else {
            res = new HashSet<Computer>(slaveNames.length);
            for (String slaveName : slaveNames) {
                Computer item = Jenkins.getInstance().getComputer(slaveName);
                if (item != null && item instanceof Computer) {
                    res.add((Computer) item);
                }
            }
        }
        return res;
    }

    public Set<User> getRequestedUsers() throws HttpResponses.HttpResponseException {
        String[] userNames = Stapler.getCurrentRequest().getParameterValues("user");
        Set<User> res;
        if (userNames == null) {
            Collection<User> items = User.getAll();
            res = new HashSet<User>(items.size());
            for (User item : items) {
                res.add((User) item);
            }
        } else {
            res = new HashSet<User>(userNames.length);
            for (String userName : userNames) {
                User item = User.get(userName, false, null);
                if (item != null && item instanceof User) {
                    res.add((User) item);
                }
            }
        }
        return res;
    }

    enum UserSubmit {

        Submit4jobs,
        Submit4slaves;

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
}
