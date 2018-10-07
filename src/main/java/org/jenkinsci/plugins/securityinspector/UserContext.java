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

import hudson.model.Computer;
import hudson.model.TopLevelItem;
import hudson.model.User;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(Beta.class)
public class UserContext {

    @CheckForNull
    private final List<TopLevelItem> selectedJobs;
    @CheckForNull
    private final List<Computer> selectedSlaves;
    @CheckForNull
    private final List<User> selectedUsers;
    @Nonnull
    private final String item;

    public UserContext(List<TopLevelItem> selectedJobs, 
            List<Computer> selectedSlaves,
            List<User> selectedUsers,
            @Nonnull String item) {
        this.selectedJobs = selectedJobs;
        this.selectedSlaves = selectedSlaves;
        this.selectedUsers = selectedUsers;
        this.item = item;
    }

    @CheckForNull
    public List<TopLevelItem> getJobs() {
        return selectedJobs;
    }

    @CheckForNull
    public List<Computer> getSlaves() {
        return selectedSlaves;
    }

    @CheckForNull
    public List<User> getUsers() {
        return selectedUsers;
    }

    @Nonnull
    public String getItem() {
        return item;
    }
}
