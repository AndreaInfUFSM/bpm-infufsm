/**
 * Copyright (C) 2009  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.ow2.bonita.light;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.ow2.bonita.facade.def.majorElement.NamedElement;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;

public interface LightProcessDefinition extends Serializable, NamedElement {

  public enum ProcessType {
    PROCESS, EVENT_SUB_PROCESS
  }

  ProcessDefinitionUUID getUUID();
  String getVersion();
  ProcessState getState();
    
  Date getDeployedDate();
  Date getUndeployedDate();
  String getDeployedBy();
  String getUndeployedBy();

  /**
   * Gets the set of category names the process definition is in.
   * @return the set of categories. The Set may be empty.
   */
  Set<String> getCategoryNames();

  /**
   * Gets the type of the process.
   * @return the type of the process
   */
  ProcessType getType();
  
  Date getMigrationDate();
}
