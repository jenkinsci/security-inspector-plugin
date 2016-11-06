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
package org.jenkinsci.plugins.securityinspector.impl.users.permissionsForComputers;

import hudson.Extension;
import org.jenkinsci.plugins.securityinspector.model.ReportBuilder;

/**
 * Builds a report for {@link PermissionsForUsersReport}.
 * @author Oleg Nenashev
 */
@Extension
public class BuilderImpl extends ReportBuilder {

    @Override
    public Type getType() {
        return Type.COMPUTER;
    }

    @Override
    public String getIcon() {
        return "fingerprint.png";
    }

    @Override
    public String getIndex() {
        return "slave-filter";
    }

    @Override
    public String getDisplayName() {
        return "Single user, multiple nodes";
    }

    @Override
    public String getDescription() {
        return "Display node permissions for the specified user";
    }
    
}
