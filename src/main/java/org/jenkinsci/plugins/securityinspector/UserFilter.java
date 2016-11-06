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
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;

public class UserFilter {

  /**
   * Include regex string.
   */
  @CheckForNull
  private final String includeRegex4User;

  /**
   * Compiled include pattern from the includeRegex string.
   */
  @CheckForNull
  private final transient Pattern includePattern4User;

  /**
   * Constructs empty filter.
   */
  public UserFilter() {
    this.includeRegex4User = null;
    this.includePattern4User = null;
  }

  /**
   * Constructs filter from StaplerRequest. This constructor is just a modified
   * copy of ListView's configure method.
   *
   * @param req Stapler Request
   * @throws Descriptor.FormException Form error
   */
  @Restricted(NoExternalUse.class)
  public UserFilter(StaplerRequest req) throws Descriptor.FormException {
    if (req.getParameter("useincluderegex4user") != null) {
      includeRegex4User = Util.nullify(req.getParameter("_.includeRegex4User"));
      if (includeRegex4User == null) {
        includePattern4User = null;
      } else {
        try {
          includePattern4User = Pattern.compile(includeRegex4User);
        } catch (PatternSyntaxException exception) {
          throw new Descriptor.FormException(exception.getDescription(), "includeRegex4User");
        }
      }
    } else {
      includeRegex4User = null;
      includePattern4User = null;
    }
  }

  @Nonnull
  @Restricted(NoExternalUse.class)
  public List<User> doFilter() {
    SortedSet<String> names = new TreeSet<String>();

    for (User user : User.getAll()) {
      String userId = user.getId();
      if (includePattern4User == null) {
        names.add(userId);
      } else if (includePattern4User.matcher(userId).matches()) {
        names.add(userId);
      }
    }

    List<User> items = new ArrayList<User>(names.size());
    for (String n : names) {
      User item = User.get(n, false, null);
      if (item != null) {
        items.add(item);
      }
    }
    return items;
  }

  @CheckForNull
  public Pattern getIncludePattern() {
    return includePattern4User;
  }

  @CheckForNull
  public String getIncludeRegex() {
    return includeRegex4User;
  }
}
