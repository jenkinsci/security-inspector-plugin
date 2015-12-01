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

import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;

public class UserFilter {
    
    /**
     * Include regex string.
     */
    private String includeRegex4User;
    
    /**
     * Compiled include pattern from the includeRegex string.
     */
    private transient Pattern includePattern4User;

    /**
     * Filter by enabled/disabled status of jobs.
     * Null for no filter, true for enabled-only, false for disabled-only.
     */
    private Boolean statusFilter4User;
    
    /**
     * Constructs empty filter.
     */
    public UserFilter() {
        this.statusFilter4User = null;
        this.includeRegex4User = null;        
    }
    
      /**
     * Constructs filter from StaplerRequest.
     * This constructor is just a modified copy of ListView's configure method.
     * @param req Stapler Request
     * @throws hudson.model.Descriptor.FormException
     * @throws IOException
     * @throws ServletException 
     */
    public UserFilter(StaplerRequest req) 
            throws Descriptor.FormException, IOException, ServletException {
        if (req.getParameter("useincluderegex4user") != null) {
            includeRegex4User = Util.nullify(req.getParameter("_.includeRegex4User"));
            if (includeRegex4User == null)
                includePattern4User = null;
            else
                try {
                    includePattern4User = Pattern.compile(includeRegex4User);
                } catch (PatternSyntaxException exception) {
                    FormValidation.error(exception.getDescription());
        }
        } else {
            includeRegex4User = null;
            includePattern4User = null;
        }
    }
    
    public List<User> doFilter() {
        SortedSet<String> names = new TreeSet<String>();
    
        for (User user : User.getAll()) {
            String userId = user.getId();
            if (includePattern4User == null){
              names.add(userId);
            }
            else if (includePattern4User.matcher(userId).matches()) {
                names.add(userId);
            } 
        }
  
        Boolean localStatusFilter = this.statusFilter4User; // capture the value to isolate us from concurrent update
        List<User> items = new ArrayList<User>(names.size());
        for (String n : names) {
            User item = User.get(n, false, null);
            // Add if no status filter or filter matches enabled/disabled status:
            if(item!=null && (localStatusFilter == null)) {
                items.add(item);
            }
        }      
        return items;
    }
  
   
    public Pattern getIncludePattern() {
        return includePattern4User;
    }

    public String getIncludeRegex() {
        return includeRegex4User;
    }

    public Boolean getStatusFilter() {
        return statusFilter4User;
    }
}
