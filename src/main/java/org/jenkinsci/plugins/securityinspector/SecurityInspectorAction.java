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

import hudson.Extension;
import hudson.model.ManagementLink;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.securityinspector.model.ReportBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.Stapler;

@Extension
public class SecurityInspectorAction extends ManagementLink {

    public SecurityInspectorAction() {
    }

    @Override
    public String getIconFileName() {
        return "secure.gif";
    }

    @Override
    public String getDisplayName() {
        return "Security Inspector";
    }

    @Override
    public String getDescription() {
        return "Inspect permissions configured by Jenkins security settings";
    }

    @Override
    public String getUrlName() {
        return "security-inspector";
    }

    public String getCategoryName() {
        return "SECURITY";
    }

    @CheckForNull
    @Restricted(NoExternalUse.class)
    public ReportBuilder getDynamic(@Nonnull String buiderName) {
        for (ReportBuilder bldr : ReportBuilder.all()) {
            if (bldr.getIndex().equals(buiderName)) {
                return bldr;
            }
        }
        return null;
    }

    @Nonnull
    @Restricted(NoExternalUse.class)
    public List<ReportBuilder> getReportBuilders(@Nonnull String type) {
        //TODO: Illegal argument handling
        return ReportBuilder.all(ReportBuilder.Type.valueOf(type));
    }

    //TODO: Handle IllegalStateException ?
    /**
     * Gets identifier of the current session.
     *
     * @return Unique id of the current session.
     * @exception IllegalStateException if this method is called on an
     * invalidated session
     */
    @Nonnull
    public static String getSessionId() throws IllegalStateException {
        return Stapler.getCurrentRequest().getSession().getId();
    }

    public boolean hasConfiguredFilters() {
        return UserContextCache.getInstance().containsKey(getSessionId());
    }
}
