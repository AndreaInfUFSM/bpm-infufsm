/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.perf.approvalwf.hook;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.bonita.definition.Hook;
import org.ow2.bonita.facade.QueryAPIAccessor;
import org.ow2.bonita.facade.runtime.ActivityInstance;

/**
 * @author Pierre Vigneras
 */
public class Reject implements Hook {

  private static final Logger LOG = Logger.getLogger(Reject.class.getName());

  public void execute(QueryAPIAccessor accessor, ActivityInstance activityInstance) throws Exception {
    if (LOG.isLoggable(Level.FINE)) {
      LOG.fine("Rejected!");
    }
  }
}
