/*
 * The MIT License
 *
 * Copyright 2014 Ksenia Nenasheva <ks.nenasheva@gmail.com>
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

import java.util.Set;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.apache.commons.collections.map.MultiKeyMap;

public abstract class SecurityInspectorReport<TRow, TColumnGroup, TColumnItem, TEntryReport> {

    private MultiKeyMap entries;
    private Set<TColumnGroup> groups;
    private Set<TRow> rows;
    private Set<TColumnItem> columns;
    
    /*package*/ 
    SecurityInspectorReport() {
    }

    public MultiKeyMap getEntries() {
        return entries;
    }

    public Set<TColumnGroup> getGroups() {
        return groups;
    }

    public Set<TRow> getRows() {
        return rows;
    }

    public Set<TColumnItem> getColumns() {
        return columns;
    }
    
    public final void generateReport(Set<TRow> rows, Set<TColumnItem> columns, Set<TColumnGroup> groups) {
        this.entries = new MultiKeyMap();
        this.groups = new HashSet<TColumnGroup>(groups);
        SortedSet<TRow> sortedRow = new TreeSet<TRow>(getComparator());
        sortedRow.addAll(rows);
        this.rows = sortedRow;
        this.columns = columns;
        
        for (TRow row : rows) {
            for (TColumnItem column : columns) {
                entries.put(row, column, getEntryReport(row, column));
            }
        }
    }
    
    public Comparator<TRow> getComparator() {
        return new Comparator<TRow>() {
            @Override
            public int compare(TRow o1, TRow o2) {
                return getRowTitle(o1).compareTo(getRowTitle(o2));
            }
        };
    }
    
    public abstract TColumnGroup getGroupOfItem(TColumnItem item);
    public abstract Collection<TColumnItem> getItemsOfGroup(TColumnGroup group);
    protected abstract TEntryReport getEntryReport(TRow row, TColumnItem item);
    
    // Layout management
    public abstract String getRowColumnHeader();
    public abstract @Nonnull String getRowTitle(TRow row);
    public abstract String getGroupTitle(TColumnGroup group);
    public abstract String getColumnTitle(TColumnItem item);
    public abstract boolean isEntryReportOk(TRow row, TColumnItem item, TEntryReport report);
}
