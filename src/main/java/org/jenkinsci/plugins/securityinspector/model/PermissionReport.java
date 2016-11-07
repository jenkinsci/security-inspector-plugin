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

package org.jenkinsci.plugins.securityinspector.model;

import hudson.security.Permission;
import hudson.security.PermissionGroup;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class PermissionReport<TRow, TEntryReport>
        extends SecurityInspectorReport<TRow, PermissionGroup, Permission, TEntryReport> {

  // TODO: WTF? Implicit overrides in implementations
  public final void generateReport(@Nonnull Set<TRow> rows, @Nonnull Set<PermissionGroup> groups) {
    Set<Permission> permissions = new HashSet<>();
    for (PermissionGroup group : groups) {
      permissions.addAll(getItemsOfGroup(group));
    }
    generateReport(rows, permissions, groups);
  }

  @Override
  public final PermissionGroup getGroupOfItem(Permission item) {
    return item.group;
  }

  @Override
  public final Collection<Permission> getItemsOfGroup(PermissionGroup group) {
    LinkedList<Permission> res = new LinkedList<>();
    for (Permission p : group.getPermissions()) {
      if (p.getEnabled()) {
        res.add(p);
      }
    }
    return res;
  }

  @Override
  public final String getGroupTitle(PermissionGroup group) {
    return group.title.toString();
  }

  @Override
  public final String getColumnTitle(Permission item) {
    return item.name;
  }
}
