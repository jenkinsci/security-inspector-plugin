/*
 * The MIT License
 *
 * Copyright (c) 2016 Oleg Nenashev.
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
package org.jenkinsci.plugins.securityinspector.impl.users;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import hudson.model.TopLevelItem;
import hudson.model.User;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import static org.jenkinsci.plugins.securityinspector.SecurityInspectorAction.getSessionId;
import org.jenkinsci.plugins.securityinspector.UserContext;
import org.jenkinsci.plugins.securityinspector.UserContextCache;
import org.jenkinsci.plugins.securityinspector.model.ReportBuilder;
import org.jenkinsci.plugins.securityinspector.util.JenkinsHelper;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponses;

/**
 * Base class for building reports for particular users.
 * @author Oleg Nenashev
 */
public abstract class UserReportBuilder extends ReportBuilder {

    @Override
    public final Type getType() {
        return Type.USER;
    }
    
    @Nonnull
    public Collection<User> getPossibleUsers() {
        SortedSet<User> sortedUser = new TreeSet<>(getComparatorUser());
        sortedUser.addAll(User.getAll());
        return sortedUser;
    }
    
    public List<AbstractFolder> getAllFolders() {
        return JenkinsHelper.getInstanceOrFail().getAllItems(AbstractFolder.class);
    }
    
    @Nonnull
    public String getDisplayName(@CheckForNull TopLevelItem item) {
        return item != null ? item.getFullDisplayName() : "";
    }
    
    /**
     * Retrieves display name of a user.
     * @param user User
     * @return User full name. Empty string if the user is {@code null}.
     */
    @Nonnull
    public String getDisplayName(@CheckForNull User user) {
        return user != null ? user.getFullName() + " (" + user.getId() + ")" : "";
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
    
    /**
     * Get selected user from context
     *
     * @return user if exists. Otherwise an error will be returned
     */
    @Nonnull
    @Restricted(NoExternalUse.class)
    public User getRequestedUser() throws HttpResponses.HttpResponseException {
        final UserContext context = UserContextCache.getInstance().get(getSessionId());
        if (context == null) {
            // TODO: 
            throw HttpResponses.error(404, "Context hae not been found");
        }
        String userId = context.getItem();
        User user = User.get(userId, false, null);
        if (user == null) {
            throw HttpResponses.error(404, "User " + userId + " does not exist");
        }
        return user;
    }
}
