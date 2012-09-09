/**
 * Copyright (C) 2010 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.client.identity;

import org.bonitasoft.console.client.BonitaUUID;
import org.bonitasoft.console.client.ItemFilter;
import org.bonitasoft.console.client.SimpleFilter;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class GroupFilter extends SimpleFilter {

    private static final long serialVersionUID = 408664694721413196L;
    protected BonitaUUID myParentGroupUUID;

    /**
     * Default constructor.
     */
    private GroupFilter() {
        // Mandatory for serialization.
    }

    public GroupFilter(final int aStartingIndex, final int aPageSize) {
        super(aStartingIndex, aPageSize, false);
    }

    public BonitaUUID getParentGroupUUID() {
        return myParentGroupUUID;
    }

    public void setParentGroupUUID(BonitaUUID aGroupUUID) {
        myParentGroupUUID = aGroupUUID;
    }

    @Override
    public ItemFilter createFilter() {
        GroupFilter theResult = new GroupFilter();
        theResult.updateFilter(this);
        return theResult;
    }

    public void updateFilter(GroupFilter aFilter) {
        super.updateFilter(aFilter);
        myParentGroupUUID = aFilter.getParentGroupUUID();
    }

}
