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
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.securityinspector.util.JenkinsHelper;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

// TODO: Split to helper classes

public class SecurityInspectorHelper {

  /*package*/ SecurityInspectorHelper() {
  }

  @Nonnull
  public Collection<User> getPossibleUsers() {
    SortedSet<User> sortedUser = new TreeSet<User>(getComparatorUser());
    sortedUser.addAll(User.getAll());
    return sortedUser;
  }

  @Nonnull
  public List<Item> getPossibleJobs() {
    return JenkinsHelper.getInstanceOrFail().getAllItems();
  }

  /**
   * Retrieves display name of a user
   * @param user User
   * @return  User full name. Empty string if the user is {@code null}.
   */
  @Nonnull
  public String getDisplayName(@CheckForNull User user) {
    return user != null ? user.getFullName() + " (" + user.getId() + ")" : "";
  }

  ///TODO why '*'?
  /**
   * Retrieves display name of an item.
   * @param item Item
   * @return  User full name. Stub symbol if the item is {@code null}.
   */
  @Nonnull
  public String getJobName(@CheckForNull Item item) {
    return item != null ? item.getFullName() : "*";
  }

  @Nonnull
  public Comparator<User> getComparatorUser() {
    return new Comparator<User>() {
      @Override
      public int compare(User o1, User o2) {
        return o1.getId().compareTo(o2.getId());
      }
    };
  }

  @Nonnull
  public Comparator<Item> getComparatorItem() {
    return new Comparator<Item>() {
      @Override
      public int compare(Item o1, Item o2) {
        return o1.getFullName().compareTo(o2.getFullName());
      }
    };
  }
}
