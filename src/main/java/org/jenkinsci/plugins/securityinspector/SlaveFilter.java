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
import hudson.model.Computer;
import hudson.model.Descriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;

public class SlaveFilter {
    
    /**
     * Include regex string.
     */
    private String includeRegex4Slave;
    
    /**
     * Compiled include pattern from the includeRegex string.
     */
    private transient Pattern includePattern4Slave;

    /**
     * Filter by enabled/disabled status of jobs.
     * Null for no filter, true for enabled-only, false for disabled-only.
     */
    private Boolean statusFilter4Slave;
    
    /**
     * Constructs empty filter.
     */
    public SlaveFilter() {
        this.statusFilter4Slave = null;
        this.includeRegex4Slave = null;        
    }
    
      /**
     * Constructs filter from StaplerRequest.
     * This constructor is just a modified copy of ListView's configure method.
     * @param req Stapler Request
     * @throws hudson.model.Descriptor.FormException
     * @throws IOException
     * @throws ServletException 
     */
    public SlaveFilter(StaplerRequest req) 
            throws Descriptor.FormException, IOException, ServletException {
        if (req.getParameter("useincluderegex4slave") != null) {
            includeRegex4Slave = Util.nullify(req.getParameter("_.includeRegex4Slave"));
            if (includeRegex4Slave == null)
                includePattern4Slave = null;
            else
                includePattern4Slave = Pattern.compile(includeRegex4Slave);
        } else {
            includeRegex4Slave = null;
            includePattern4Slave = null;
        }
    }
    
    public List<Computer> doFilter() {
        SortedSet<String> names;

        synchronized (this) {
            names = new TreeSet<String>();
        }
        
        for (Computer item : Jenkins.getInstance().getComputers()) {
            String itemName = item.getName();
            
            if (includePattern4Slave == null) {
               names.add(itemName); 
            }
            else if (includePattern4Slave.matcher(itemName).matches()) {
                names.add(itemName);
            } 
        }
  
        Boolean localStatusFilter = this.statusFilter4Slave; // capture the value to isolate us from concurrent update
        List<Computer> items = new ArrayList<Computer>(names.size());
        for (String n : names) {
            Computer item = Jenkins.getInstance().getComputer(n);
            // Add if no status filter or filter matches enabled/disabled status:
            if(item!=null && (localStatusFilter == null)) {
                items.add(item);
            }
        }
        
        return items;
    }
  
   
    public Pattern getIncludePattern() {
        return includePattern4Slave;
    }

    public String getIncludeRegex() {
        return includeRegex4Slave;
    }

    public Boolean getStatusFilter() {
        return statusFilter4Slave;
    }
}
