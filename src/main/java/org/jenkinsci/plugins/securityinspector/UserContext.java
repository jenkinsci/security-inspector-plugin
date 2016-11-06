/*
 * The MIT License
 *
 * Copyright 2015-2016 Ksenia Nenasheva <ks.nenasheva@gmail.com>
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

import org.jenkinsci.plugins.securityinspector.util.JobFilter;
import org.jenkinsci.plugins.securityinspector.util.ComputerFilter;
import org.jenkinsci.plugins.securityinspector.util.UserFilter;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(NoExternalUse.class)
public class UserContext {

  @CheckForNull
  private final JobFilter jobFilter;
  @CheckForNull
  private final ComputerFilter slaveFilter;
  @CheckForNull
  private final UserFilter userFilter;
  @Nonnull
  private final String item;

  public UserContext(@Nonnull JobFilter jobFilter, @Nonnull String item) {
    this.jobFilter = jobFilter;
    this.item = item;

    this.slaveFilter = null;
    this.userFilter = null;
  }

  public UserContext(@Nonnull ComputerFilter slaveFilter, @Nonnull String item) {
    this.slaveFilter = slaveFilter;
    this.item = item;

    this.jobFilter = null;
    this.userFilter = null;
  }

  public UserContext(@Nonnull UserFilter userFilter, @Nonnull String item) {
    this.userFilter = userFilter;
    this.item = item;

    this.jobFilter = null;
    this.slaveFilter = null;
  }

  @CheckForNull
  public JobFilter getJobFilter() {
    return jobFilter;
  }

  @CheckForNull
  public ComputerFilter getSlaveFilter() {
    return slaveFilter;
  }

  @CheckForNull
  public UserFilter getUserFilter() {
    return userFilter;
  }

  @Nonnull
  public String getItem() {
    return item;
  }
}
