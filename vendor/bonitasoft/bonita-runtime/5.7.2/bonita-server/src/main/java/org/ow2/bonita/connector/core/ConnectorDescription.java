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
package org.ow2.bonita.connector.core;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.ow2.bonita.connector.core.desc.Category;
import org.ow2.bonita.connector.core.desc.Component;
import org.ow2.bonita.connector.core.desc.ConnectorDescriptor;
import org.ow2.bonita.connector.core.desc.Getter;
import org.ow2.bonita.connector.core.desc.Page;
import org.ow2.bonita.connector.core.desc.Setter;
import org.ow2.bonita.connector.core.desc.Text;

/**
 * 
 * @author Matthieu Chaffotte
 *
 */
public class ConnectorDescription {

	private static final String LONELY_PAGE_ID = "lonely";
  private Class <? extends Connector> classConnector;
  private Locale currentLocale;
  private ConnectorDescriptor descriptor;
  private ResourceBundle bundle;

  public ConnectorDescription(Class<? extends Connector> classConnector) throws ConnectorException {
    this(classConnector, Locale.getDefault());
  }

  public ConnectorDescription(Class<? extends Connector> classConnector, Locale locale) throws ConnectorException {
    if (classConnector == null) {
      throw new IllegalArgumentException("The connector class cannot be null");
    } else {
      List<ConnectorError> errors = Connector.validateConnector(classConnector);
      if (!errors.isEmpty()) {
        throw new ConnectorException("The connector " + classConnector.getSimpleName() + " contains several errors!",
            classConnector.getName(), classConnector.getName(), errors);
      } else {
        this.classConnector = classConnector;
        this.currentLocale = locale;
        this.descriptor = ConnectorDescriptorAPI.load(classConnector);
      }
    }
  }

  /**
   * 
   * @param locale
   */
  public void setLocale(Locale locale) {
    currentLocale = locale;
  }

  /**
   * Gets the Connector Identification.
   * It checks whether the Connector has an identification
   * and returns either the Connector identification or the
   * Connector class name.
   * @return the connector identification or the Connector class name.
   */
  public String getId() {
  	String id = null;
  	if (descriptor != null) {
  		id = descriptor.getConnectorId();
  	}
  	if (id == null) {
      id = classConnector.getSimpleName();
    }
    return id;
  }

  public String getConnectorLabel() {
    String connector = getResourceBundleValue("ConnectorId");
    if (connector == null) {
      return getId();
    } else {
      return connector;
    }
  }

  public String getConnectorVersion() {
    if (descriptor != null) {
      return descriptor.getVersion();
    } else {
      return "1.0";
    }
  }

  /**
   * Gets the Connector Description.
   * It checks whether the Connector has a description and returns either
   * the Connector Description or null
   * @return the connector description or null
   */
  public String getDescription() {
    return getResourceBundleValue("Description");
  }

  /**
   * Gets the Connector Category Name.
   * It checks whether the Connector has a category and returns either
   * the Connector Category or Other
   * @return the connector category or the default category other
   */
  public String getCategoryName(String categoryId) {
    return getResourceBundleValue(categoryId);
  }

  /**
   * Gets all the Page identifiers.
   * it checks whether the Connector has page names and returns either
   * an array of page names or an empty array.
   * @return an array of page identifiers or an empty array
   */
  public List<String> getPages() {
    List<String> pagesId= new ArrayList<String>();
    if (descriptor == null) {
    	pagesId.add(LONELY_PAGE_ID);
    } else {
    	List<Page> pages = descriptor.getPages();
      if (pages != null) {
        for (Page page : pages) {
          pagesId.add(page.getPageId()); 
        }
      }
    }
    return pagesId;
  }

  /**
   * Gets the page name
   * @param pageId the page identifier
   * @return the page description
   */
  public String getPageName(String pageId) {
    return getResourceBundleValue(pageId);
  }

  /**
   * Gets the page description
   * @param pageId the page identifier
   * @return the page description
   */
  public String getPageDescription(String pageId) {
    return getResourceBundleValue(pageId + ".description");
  }

  /**
   * Gets all the components of the connector inputs in a random order
   * @return the list of all the connector components
   */
  public List<Component> getAllInputs() {
  	List<Component> components = new ArrayList<Component>();
  	if (descriptor != null) {
  		List<Page> pages = descriptor.getPages();
      if (pages != null) {
        for (Page page : pages) {
          components.addAll(page.getWidgets());
        }
      }
  	} else {
			try {
				Connector connector = classConnector.newInstance();
				List<Setter> setters = connector.getSetters();
	  		for (int i = 0; i < setters.size(); i++) {
	  			Setter setter = setters.get(i);
	  			StringBuilder builder = new StringBuilder(setter.getSetterName());
	  			builder.append(i);
	  			Text text = new Text(builder.toString(), setter, 30, 60);
	  			components.add(text);
				}
			} catch (Exception e) {
				return components;
			}
  	}
    return components;
  }

  /**
   * Gets the connector outputs or an empty list if the connector has not got an output.
   * @return the list of connector output identifiers
   */
  public List<String> getOutputNames() {
  	List<String> ids = new ArrayList<String>();
  	if (descriptor != null) {
  		List<Getter> getters = descriptor.getOutputs();
  		if (getters != null) {
  			for (Getter getter : getters) {
  				ids.add(getter.getName());
  			}
  		}
  	} else {
  		try {
				Connector connector = classConnector.newInstance();
				List<Getter> getters = connector.getGetters();
				if (getters != null) {
	  			for (Getter getter : getters) {
	  				ids.add(getter.getName());
	  			}
				}
			} catch (Exception e) {
				return ids;
			}
  	}
  	return ids;
  }

  public Type getOutputType(String outputName) {
    return Connector.getGetterReturnType(classConnector, outputName);
  }

  private Page getPage(String pageId) {
  	if (descriptor != null) {
  		List<Page> pages = descriptor.getPages();
      for (Page page : pages) {
        if (pageId.equals(page.getPageId())) {
          return page;
        }
      }
  	}
    return null;
  }

  /**
   * Gets all components of a connector page. If the page does not exist,
   * an Exception is raised
   * @param pageId the page identifier
   * @return the list of the components of a connector page
   */
  public List<Component> getAllPageInputs(String pageId) {
  	if (pageId.equals(LONELY_PAGE_ID)) {
  		return getAllInputs();
  	} else {
  		Page page = getPage(pageId);
      if (page == null) {
        throw new IllegalArgumentException(pageId + " does not refer to a Connector page Id");
      }
      return page.getWidgets();
  	}
  }

  /**
   * Gets the label value of an input widget. If the label identifier does not refer
   * to any label value, null is returned.
   * @param labelId the labelId
   * @return the i18n label value
   */
  public String getInputLabel(String labelId) {
    String label = getResourceBundleValue(labelId + ".label");
    if (label == null) {
      return labelId;
    } else {
      return label;
    }
  }

  /**
   * Gets the label value of an input widget. If the label identifier does not refer
   * to any label value, null is returned.
   * @param labelId the labelId
   * @return the i18n label value
   */
  public String getInputDescription(String labelId) {
    String label = getResourceBundleValue(labelId + ".description");
    if (label == null) {
      return labelId;
    } else {
      return label;
    }
  }

  /**
   * Gets the connector icon or null if the connector has not got an icon.
   * @return
   */
  public InputStream getIcon() {
  	InputStream is = null;
  	if (descriptor != null) {
  		String icon = descriptor.getIcon();
  		if (icon != null && !"".equals(icon.trim())) {
  			is = classConnector.getResourceAsStream(icon);
  		}
  	}
  	return is;
  }

  /**
   * Gets either the connector icon path or null if any icon is defined
   * @return the connector icon path
   */
  public String getIconPath() {
    String path = null;
    if (descriptor != null) {
      path = descriptor.getIcon();
    }
    return path;
  }

  private ResourceBundle getResourceBundle() {
    if (bundle != null) {
      return bundle;
    } else {
      String language = classConnector.getName();
      try {
        return ResourceBundle.getBundle(language, currentLocale, classConnector.getClassLoader());
      } catch (MissingResourceException e) {
        return null;
      }
    }
  }

  private String getResourceBundleValue(String key) {
    ResourceBundle messages = getResourceBundle();
    if (messages == null) {
      return null;
    }
    try {
      return messages.getString(key);
    } catch(Exception e) {
      return null;
    }
  }

  public Class<? extends Connector> getConnectorClass() {
    return classConnector;
  }

  @Override
  public boolean equals(Object object) {
    boolean equals = false;
    if (object != null && object instanceof ConnectorDescription) {
      ConnectorDescription description = (ConnectorDescription) object;
      if (this.getId().equals(description.getId())) {
        equals = true;
      }
    }
    return equals;
  }

  public List<Category> getCategories() {
    if (descriptor != null) {
      List<Category> categories = descriptor.getCategories();
      if (categories != null) {
        return categories;
      }
    }
    List<Category> categories = new ArrayList<Category>();
    categories.add(ConnectorAPI.other);
    return categories;
  }

  public List<Setter> getInputs() {
    List<Setter> setters = new ArrayList<Setter>();
    if (descriptor != null) {
      setters.addAll(descriptor.getInputs());
    }
    return setters;
  }

  /**
   * @return
   */
  public List<Getter> getOutputs() {
	  List<Getter> getters = new ArrayList<Getter>();
	  if (descriptor != null) {
		  getters.addAll(descriptor.getOutputs());
	  }
	  return getters;
  }

}
