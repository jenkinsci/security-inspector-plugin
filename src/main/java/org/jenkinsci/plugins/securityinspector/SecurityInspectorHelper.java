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

import hudson.model.Item;
import hudson.model.User;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

public class SecurityInspectorHelper {

  /*package*/ SecurityInspectorHelper() {
  }

  public Collection<User> getPossibleUsers() {
    SortedSet<User> sortedUser = new TreeSet<User>(getComparatorUser());
    sortedUser.addAll(User.getAll());
    return sortedUser;
  }

  @Nonnull
  @Restricted(NoExternalUse.class)
  public static Jenkins getInstance() throws IllegalStateException {
    Jenkins instance = Jenkins.getInstance();
    if (instance == null) {
      throw new IllegalStateException("Jenkins has not been started, or was already shut down");
    }
    return instance;
  }

  public List<Item> getPossibleJobs() {
    return getInstance().getAllItems();
  }

  public String getDisplayName(User user) {
    return user != null ? user.getFullName() + " (" + user.getId() + ")" : "";
  }

  public String getJobName(Item item) {
    return item != null ? item.getFullName() : "*";
  }

  public Comparator<User> getComparatorUser() {
    return new Comparator<User>() {
      @Override
      public int compare(User o1, User o2) {
        return o1.getId().compareTo(o2.getId());
      }
    };
  }

  public Comparator<Item> getComparatorItem() {
    return new Comparator<Item>() {
      @Override
      public int compare(Item o1, Item o2) {
        return o1.getFullName().compareTo(o2.getFullName());
      }
    };
  }
}
