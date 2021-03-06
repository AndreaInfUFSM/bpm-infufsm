/**
 * Copyright (C) 2009 BonitaSoft S.A.
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
package org.bonitasoft.console.client.model.identity;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.console.client.BonitaUUID;
import org.bonitasoft.console.client.SimpleFilter;
import org.bonitasoft.console.client.common.data.AsyncHandler;
import org.bonitasoft.console.client.identity.UserMetadataItem;
import org.bonitasoft.console.client.model.DefaultFilteredDataSourceImpl;
import org.bonitasoft.console.client.model.MessageDataSource;
import org.bonitasoft.console.client.model.SimpleSelection;

/**
 * @author Nicolas Chabanoles
 * 
 */
public class UserMetadataDataSourceImpl extends DefaultFilteredDataSourceImpl<BonitaUUID, UserMetadataItem, SimpleFilter> implements UserMetadataDataSource {

  public UserMetadataDataSourceImpl(MessageDataSource aMessageDataSource) {
    super(new UserMetadataItemData(), new SimpleSelection<BonitaUUID>(), aMessageDataSource);
    setItemFilter(new SimpleFilter(0, 20));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.bonitasoft.console.client.model.identity.UserMetadataDataSource#getAllItems
   * (org.bonitasoft.console.client.common.data.AsyncHandler)
   */
  @SuppressWarnings("unchecked")
  public void getAllItems(final AsyncHandler<List<UserMetadataItem>> anAsyncHandler) {

    ((UserMetadataItemData)myRPCItemData).getAllItems(new AsyncHandler<List<UserMetadataItem>>() {
      public void handleFailure(Throwable aT) {
        myKnownItems.clear();
        if (anAsyncHandler != null) {
          anAsyncHandler.handleFailure(aT);
        }
      }

      public void handleSuccess(List<UserMetadataItem> result) {
        myKnownItems.clear();
        updateItems(result);
        // Store the total size.
        mySize = result.size();
        if (anAsyncHandler != null) {
          anAsyncHandler.handleSuccess(new ArrayList<UserMetadataItem>(myKnownItems.values()));
        }
      };
    });

  }

}
