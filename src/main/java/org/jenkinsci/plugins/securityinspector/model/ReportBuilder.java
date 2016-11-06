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
package org.jenkinsci.plugins.securityinspector.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Defines a report builder for the Security Inspector UI.
 * @author Oleg Nenashev
 */
public abstract class ReportBuilder implements ExtensionPoint {
    
    @Nonnull
    public abstract Type getType();
    @Nonnull
    public abstract String getIcon();
    @Nonnull
    public abstract String getIndex();
    @Nonnull
    public abstract String getDisplayName();
    @Nonnull
    public abstract String getDescription();
    
    @Nonnull
    public static ExtensionList<ReportBuilder> all() {
        return ExtensionList.lookup(ReportBuilder.class);
    }
    
    @Nonnull
    public static List<ReportBuilder> all(@Nonnull Type type) {
        final ExtensionList<ReportBuilder> all = all();
        final ArrayList<ReportBuilder> res = new ArrayList<>();
        for (ReportBuilder rb : all) {
            if (rb.getType() == type) {
                res.add(rb);
            }
        }
        return res;
    }
    
    public static enum Type {
        ITEM,
        USER,
        COMPUTER
    }
}
