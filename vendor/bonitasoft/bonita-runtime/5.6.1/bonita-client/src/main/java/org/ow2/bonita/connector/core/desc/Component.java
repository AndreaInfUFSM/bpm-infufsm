/**
 * Copyright (C) 2009  Bull S. A. S.
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
 * 
 * Modified by Matthieu Chaffotte - BonitaSoft S.A.
 **/
package org.ow2.bonita.connector.core.desc;

/**
 *
 * @author Matthieu Chaffotte
 *
 */
public abstract class Component {

  private String labelId;

//used by XStream
  protected Component() {}
  
  public Component(String labelId) {
    super();
    this.labelId = labelId;
  }

  public String getLabelId() {
    return labelId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Component) {
      Component c = (Component) obj;
      return (this.getLabelId().equals(c.getLabelId()));
    } else {
      return false;
    }
  }
}
