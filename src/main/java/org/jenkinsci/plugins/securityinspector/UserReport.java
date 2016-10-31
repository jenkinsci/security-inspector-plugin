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

import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.User;
import hudson.model.View;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import java.util.HashSet;
import java.util.Set;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UsernameNotFoundException;

public class UserReport extends PermissionReport<User, Boolean> {

  Item job4report;

  private UserReport(Item job) {
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

  public final void generateReport(Set<User> rows) {
    Set<PermissionGroup> groups = new HashSet<PermissionGroup>(PermissionGroup.getAll());
    groups.remove(PermissionGroup.get(Permission.class));
    groups.remove(PermissionGroup.get(Hudson.class));
    groups.remove(PermissionGroup.get(Computer.class));
    groups.remove(PermissionGroup.get(View.class));

    super.generateReport(rows, groups);
  }

  public static UserReport createReport(Set<User> rows, Item job) {
    UserReport report = new UserReport(job);
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
