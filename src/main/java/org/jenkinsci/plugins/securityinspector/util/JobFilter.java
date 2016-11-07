/*
 * The MIT License
 *
 * Copyright 2014-2016 Ksenia Nenasheva <ks.nenasheva@gmail.com>
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

package org.jenkinsci.plugins.securityinspector.util;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.AllView;
import hudson.model.Descriptor;
import static hudson.model.Descriptor.findByDescribableClassName;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

public class JobFilter {

  /**
   * Jobs filters.
   */
  @Nonnull
  private List<ViewJobFilter> jobFilters;

  /**
   * Include regex string.
   */
  @CheckForNull
  private String includeRegex;

  /**
   * Compiled include pattern from the includeRegex string.
   */
  @CheckForNull
  private transient Pattern includePattern;

  /**
   * Filter by enabled/disabled status of jobs. {@code null} for no filter, true for
   * enabled-only, false for disabled-only.
   */
  @CheckForNull
  private Boolean statusFilter;

  /**
   * Constructs empty filter.
   */
  public JobFilter() {
    this.statusFilter = null;
    this.jobFilters = new LinkedList<>();
    this.includeRegex = null;
  }

  /**
   * Constructs filter from a StaplerRequest. 
   * This constructor is just a modified copy of ListView's configure method.
   *
   * @param req Stapler Request
   * @throws Descriptor.FormException Missing or invalid field in the form
   * @throws ServletException Cannot retrieve submitted form in Stapler
   */
  @Restricted(NoExternalUse.class)
  public JobFilter(@Nonnull StaplerRequest req)
          throws Descriptor.FormException, ServletException {
    if (req.getParameter("useincluderegex") != null) {
      includeRegex = Util.nullify(req.getParameter("_.includeRegex"));
      if (includeRegex == null) {
        includePattern = null;
      } else {
        includePattern = Pattern.compile(includeRegex);
      }
    } else {
      includeRegex = null;
      includePattern = null;
    }

    List<ViewJobFilter> items = new ArrayList<>();
    Object formData = req.getSubmittedForm().get("jobFilters");
    Collection<? extends Descriptor<ViewJobFilter>> descriptors = ViewJobFilter.all();

    if (formData != null) {
      for (Object o : JSONArray.fromObject(formData)) {
        JSONObject jo = (JSONObject) o;
        String kind = jo.getString("$class");
        Descriptor<ViewJobFilter> d = findByDescribableClassName(descriptors, kind);
        if (d != null) {
          items.add(d.newInstance(req, jo));
        }
      }
    }

    String filter = Util.fixEmpty(req.getParameter("statusFilter"));
    statusFilter = filter != null ? "1".equals(filter) : null;
    jobFilters = items;
  }

  /**
   * Filters jobs by the specified filter
   * Due to the glitch in {@link View#getAllItems()} we always process all jobs independently from the view contents.
   * @param view View, for which we retrieve the data
   * @return List of the jobs matching the specified filters
   */
  @Nonnull
  @Restricted(NoExternalUse.class)
  public List<TopLevelItem> doFilter(@Nonnull AllView view) {
    final SortedSet<String> names = new TreeSet<>();

    // TODO: Switch to View.getAllItems() once it behaves according to the spec
    final List<TopLevelItem> allItems = JenkinsHelper.getInstanceOrFail().getAllItems(TopLevelItem.class);
    final Jenkins jenkins = JenkinsHelper.getInstanceOrFail();   

    if (includePattern != null) {
      for (Item item : allItems) {
        String itemName = item.getFullName();
        if (includePattern.matcher(itemName).matches()) {
          names.add(itemName);
        }
      }
    } else {
      for (Item item : allItems) {
        String itemName = item.getFullName();
        names.add(itemName);
      }
    }

    Boolean localStatusFilter = this.statusFilter; // capture the value to isolate us from concurrent update
    List<TopLevelItem> items = new ArrayList<>();
    for (String n : names) {
      TopLevelItem item = jenkins.getItemByFullName(n, TopLevelItem.class);
      // Add if no status filter or filter matches enabled/disabled status:
      if (item != null && (localStatusFilter == null
              || !(item instanceof AbstractProject)
              || ((AbstractProject) item).isDisabled() ^ localStatusFilter)) {
        items.add(item);
      }
    }

    // Check the filters
    Iterable<ViewJobFilter> localJobFilters = getJobFilters();
    for (ViewJobFilter jobFilter : localJobFilters) {
      items = jobFilter.filter(items, allItems, view);
    }

    return items;
  }

  @Nonnull
  public List<ViewJobFilter> getJobFilters() {
    return jobFilters;
  }

  @CheckForNull
  public Pattern getIncludePattern() {
    return includePattern;
  }

  @CheckForNull
  public String getIncludeRegex() {
    return includeRegex;
  }

  @CheckForNull
  public Boolean getStatusFilter() {
    return statusFilter;
  }
}
