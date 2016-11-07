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
import hudson.model.Computer;
import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Filters {@link Computer}s according to the specified criteria.
 * @author Ksenia Nenasheva
 */
public class ComputerFilter {

  /**
   * Include regex string.
   */
  @CheckForNull
  private final String includeRegex4Slave;

  /**
   * Compiled include pattern from the includeRegex string.
   */
  @CheckForNull
  private final transient Pattern includePattern4Slave;

  /**
   * Constructs empty filter.
   */
  public ComputerFilter() {
    this.includeRegex4Slave = null;
    this.includePattern4Slave = null;
  }

  /**
   * Constructs filter from StaplerRequest. This constructor is just a modified
   * copy of ListView's configure method.
   *
   * @param req Stapler Request
   * @throws Descriptor.FormException Form parameter issue
   */
  @Restricted(NoExternalUse.class)
  public ComputerFilter(@Nonnull StaplerRequest req) throws Descriptor.FormException {
    if (req.getParameter("useincluderegex4slave") != null) {
      includeRegex4Slave = Util.nullify(req.getParameter("_.includeRegex4Slave"));
      if (includeRegex4Slave == null) {
        includePattern4Slave = null;
      } else {
          try {
            includePattern4Slave = Pattern.compile(includeRegex4Slave);
          } catch (PatternSyntaxException ex) {
              throw new Descriptor.FormException("Invalid regular expression", ex, "includeRegex4Slave");
          }
      }
    } else {
      includeRegex4Slave = null;
      includePattern4Slave = null;
    }
  }

  @Nonnull
  @Restricted(NoExternalUse.class)
  public List<Computer> doFilter() {
    final Jenkins jenkins = JenkinsHelper.getInstanceOrFail();
    SortedSet<String> names;

    // TODO: what, sync of the internal method?
    synchronized (this) {
      names = new TreeSet<String>();
    }

    for (Computer item : jenkins.getComputers()) {
      String itemName = item.getName();

      if (includePattern4Slave == null) {
        names.add(itemName);
      } else if (includePattern4Slave.matcher(itemName).matches()) {
        names.add(itemName);
      }
    }

    List<Computer> items = new ArrayList<Computer>(names.size());
    for (String n : names) {
      Computer item = jenkins.getComputer(n);
      // Add if no status filter or filter matches enabled/disabled status:
      if (item != null) {
        items.add(item);
      }
    }

    return items;
  }

  @CheckForNull
  public Pattern getIncludePattern() {
    return includePattern4Slave;
  }

  @CheckForNull
  public String getIncludeRegex() {
    return includeRegex4Slave;
  }
}
