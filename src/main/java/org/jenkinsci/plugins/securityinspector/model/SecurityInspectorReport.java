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
package org.jenkinsci.plugins.securityinspector.model;

import java.util.Set;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.apache.commons.collections.map.MultiKeyMap;

/**
 * Basic class for reports being generated by the plugin. This class implements
 * the following report format:
 * <ul>
 * <li> The report is being returned as a table
 * <li> Columns are grouped into several groups
 * <li> Rows are not grouped
 * </ul>
 * This format gives a 3-dimensional report, all element types can be specified
 * by generics.
 *
 * @author Ksenia Nenasheva
 * @param <TRow> Class of the Rows in the report
 * @param <TColumnGroup> Class of the column group definitions
 * @param <TColumnItem> Class of item reports within a group
 * @param <TEntryReport> Class of the stored entries
 */
public abstract class SecurityInspectorReport<TRow, TColumnGroup, TColumnItem, TEntryReport> {

    @Nonnull
    private final MultiKeyMap entries;
    @Nonnull
    private final Set<TColumnGroup> groups;
    @Nonnull
    private final Set<TRow> rows;
    @Nonnull
    private final Set<TColumnItem> columns;

    /*package*/
    SecurityInspectorReport() {
        this.entries = new MultiKeyMap();
        this.groups = new HashSet<>();
        this.rows = new TreeSet<>(getComparator());
        this.columns = new HashSet<>();
    }

    @Nonnull
    public MultiKeyMap getEntries() {
        return entries;
    }

    @Nonnull
    public Set<TColumnGroup> getGroups() {
        return groups;
    }

    @Nonnull
    public Set<TRow> getRows() {
        return rows;
    }

    @Nonnull
    public Set<TColumnItem> getColumns() {
        return columns;
    }

    public final void generateReport(@Nonnull Set<TRow> rows, @Nonnull Set<TColumnItem> columns, @Nonnull Set<TColumnGroup> groups) {
        this.groups.addAll(groups);
        this.rows.addAll(rows);
        this.columns.addAll(columns);

        for (TRow row : rows) {
            for (TColumnItem column : columns) {
                entries.put(row, column, getEntryReport(row, column));
            }
        }
    }

    @Nonnull
    public Comparator<TRow> getComparator() {
        return new Comparator<TRow>() {
            @Override
            public int compare(TRow o1, TRow o2) {
                return getRowTitle(o1).compareTo(getRowTitle(o2));
            }
        };
    }

    /**
     * Retrieve a group for an item.
     *
     * @param item Item to be analyzed
     * @return Assigned group. Never {@code null}
     */
    @Nonnull
    public abstract TColumnGroup getGroupOfItem(@Nonnull TColumnItem item);

    /**
     * Retrieves items for a group.
     *
     * @param group Group
     * @return Collection of items. May be empty, but never {@code null}
     */
    @Nonnull
    public abstract Collection<TColumnItem> getItemsOfGroup(@Nonnull TColumnGroup group);

    /**
     * Retrieves an entry for the specified row and column values.
     *
     * @param row Row
     * @param item Column
     * @return Entry for the report table
     */
    @Nonnull
    protected abstract TEntryReport getEntryReport(@Nonnull TRow row, @Nonnull TColumnItem item);

    /**
     * Returns display name for the row grouping cell.
     *
     * @return Localizable text for the column header
     */
    @Nonnull
    public abstract String getRowColumnHeader();

    /**
     * Retrieves a title for the specified row.
     *
     * @param row Row
     * @return Localizable text title
     */
    @Nonnull
    public abstract String getRowTitle(@Nonnull TRow row);

    /**
     * Retrieves a title for the specified group.
     *
     * @param group Group
     * @return Localizable text title
     */
    @Nonnull
    public abstract String getGroupTitle(@Nonnull TColumnGroup group);

    /**
     * Retrieves a title for the specified column.
     *
     * @param column Column
     * @return Localizable text title
     */
    @Nonnull
    public abstract String getColumnTitle(@Nonnull TColumnItem column);

    /**
     * Verifies that the generated entry is correct.
     *
     * @param row Row
     * @param column Column
     * @param reportEntry Generated report entry
     * @return {@code true} if the check passed
     */
    public abstract boolean isEntryReportOk(@Nonnull TRow row, @Nonnull TColumnItem column, @Nonnull TEntryReport reportEntry);
}
