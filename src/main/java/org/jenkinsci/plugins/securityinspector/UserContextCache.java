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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import static org.jenkinsci.plugins.securityinspector.SecurityInspectorAction.getSessionId;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.Beta;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Restricted(Beta.class)
public class UserContextCache {

    //TODO: fix concurrency issues
    final Map<String, UserContext> contextMap = new HashMap<>();

    private static final UserContextCache INSTANCE = new UserContextCache();

    @Nonnull
    public static UserContextCache getInstance() {
        return INSTANCE;
    }

    private UserContextCache() {
        // OTHERS CANNOT INSTANTINATE
    }

    public synchronized boolean containsKey(@Nonnull String sessionId) {
        return contextMap.containsKey(sessionId);
    }

    @CheckForNull
    public synchronized UserContext get(@Nonnull String sessionId) {
        return contextMap.get(sessionId);
    }

    public synchronized void flush(@Nonnull String sessionId) {
        if (contextMap.containsKey(sessionId)) {
            contextMap.remove(sessionId);
        }
    }

    // TODO: Bug, replacement of the session ID
    public synchronized void put(@Nonnull String sessionId, @Nonnull UserContext context) {
        contextMap.put(getSessionId(), context);
    }

    /**
     * Cleans internal cache of JSON Objects for the session.
     *
     * @return Current Session Id
     */
    @Nonnull
    public static String cleanCache() {
        final String sessionId = getSessionId();
        INSTANCE.flush(sessionId);
        return sessionId;
    }

    public static void updateSearchCache(List<TopLevelItem> selectedJobs,
            List<Computer> selectedSlaves,
            List<User> selectedUsers,
            @Nonnull String item) {
        cleanCache();
        // Put Context to the map
        INSTANCE.put(getSessionId(), new UserContext(selectedJobs, selectedSlaves, selectedUsers, item));
    }
}
