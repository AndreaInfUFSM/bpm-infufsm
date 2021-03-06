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
package org.bonitasoft.forms.client.view.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bonitasoft.forms.client.model.FormFieldAvailableValue;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget displaying a group of radio buttons (only one radio button can be selected inside a group)
 * 
 * @author Anthony Birembaut
 */
public class RadioButtonGroupWidget extends Composite implements HasClickHandlers, ClickHandler, HasValueChangeHandlers<Boolean>, ValueChangeHandler<Boolean> {

    /**
     * the flow panel used to display the widget
     */
    protected FlowPanel flowPanel;
    
    /**
     * The set of radio buttons in the group
     */
    protected Set<RadioButton> radioButtons = new HashSet<RadioButton>();
    
    /**
     * items style
     */
    protected String itemsStyle;
    
    /**
     * The radio buttons group Id
     */
    protected String radioButtonGroupId;

    /**
     * click handlers registered for the widget
     */
    protected List<ClickHandler> clickHandlers;

    /**
     * value change handlers registered for the widget
     */
    protected List<ValueChangeHandler<Boolean>> valueChangeHandlers;
    
    /**
     * indicates if HTML is allowed as value of the widget
     */
    protected boolean allowHTML;
    
    /**
     * Constructor
     * 
     * @param radioButtonGroupId Id of the radio button group
     * @param availableValues available values of the group
     * @param initialValue initial value
     * @param itemsStyle the css classes of each radio button
     *            
     */
    public RadioButtonGroupWidget(final String radioButtonGroupId, final List<FormFieldAvailableValue> availableValues, final String initialValue, final String itemsStyle) {
        this(radioButtonGroupId, availableValues, initialValue, itemsStyle, false);
    }
    
    /**
     * Constructor
     * 
     * @param radioButtonGroupId Id of the radio button group
     * @param availableValues available values of the group
     * @param initialValue initial value
     * @param itemsStyle the css classes of each radio button
     * @param allowHTML allow HTML in the radio buttons labels
     *            
     */
    public RadioButtonGroupWidget(final String radioButtonGroupId, final List<FormFieldAvailableValue> availableValues, final String initialValue, final String itemsStyle, final boolean allowHTML) {

        flowPanel = new FlowPanel();
        
        this.radioButtonGroupId = radioButtonGroupId;
        
        this.itemsStyle = itemsStyle;
        
        this.allowHTML = allowHTML;
        
        for (final FormFieldAvailableValue availableValue : availableValues) {
            final RadioButton radioButton = new RadioButton(radioButtonGroupId, availableValue.getLabel(), allowHTML);
            radioButton.addClickHandler(this);
            radioButton.addValueChangeHandler(this);
            radioButton.setFormValue(availableValue.getValue());
            if (initialValue != null && initialValue.equals(availableValue.getValue()))
            {
                radioButton.setValue(true);
            }
            radioButton.setStyleName("bonita_form_radio");
            if (itemsStyle != null && itemsStyle.length() > 0) {
                radioButton.addStyleName(itemsStyle);
            }
            radioButtons.add(radioButton);
            flowPanel.add(radioButton);
        }

        initWidget(flowPanel);
    }
    
    /**
     * @return the String value of the slected radio button of the group (null if no radio button is selected)
     */
    public String getValue(){
        
        String value = null;
        
        final Iterator<Widget> iterator = flowPanel.iterator();
        while (iterator.hasNext()) {
            final RadioButton radioButton = (RadioButton) iterator.next();
            if (radioButton.getValue()) {
                value = radioButton.getFormValue();
                break;
            }
        }
        
        return value;
    }
    
    /**
     * Set the value of the widget
     * @param value
     */
    public void setValue(final String value, boolean fireEvents) {
        if ((getValue() != null && getValue().equals(value)) || (value != null && value.equals(getValue()))) {
            fireEvents = false;
        }
        for (final RadioButton radioButton : radioButtons) {
            if (value != null && value.equals(radioButton.getFormValue())) {
                radioButton.setValue(true);
            } else {
                radioButton.setValue(false);
            }
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, true);
        }
    }
    
    /**
     * Set the wigdet available values
     * @param availableValues
     */
    public void setAvailableValues(final List<FormFieldAvailableValue> availableValues, final boolean fireEvents) {
        radioButtons.clear();
        flowPanel.clear();
        clickHandlers.clear();
        valueChangeHandlers.clear();
        for (final FormFieldAvailableValue availableValue : availableValues) {
            final RadioButton radioButton = new RadioButton(radioButtonGroupId, availableValue.getLabel(), allowHTML);
            radioButton.addClickHandler(this);
            radioButton.addValueChangeHandler(this);
            radioButton.setFormValue(availableValue.getValue());
            radioButton.setStyleName("bonita_form_radio");
            if (itemsStyle != null && itemsStyle.length() > 0) {
                radioButton.addStyleName(itemsStyle);
            }
            radioButtons.add(radioButton);
            flowPanel.add(radioButton);
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, true);
        }
    }
    
    /**
     * Enable or disable the radiobuttons group
     * @param isEnabled
     */
    public void setEnabled(final boolean isEnabled) {
        for (final RadioButton radioButton : radioButtons) {
            radioButton.setEnabled(isEnabled);
        }
    }

    /**
     * {@inheritDoc}
     */
    public HandlerRegistration addClickHandler(final ClickHandler clickHandler) {
        if (clickHandlers == null) {
            clickHandlers = new ArrayList<ClickHandler>();
        }
        clickHandlers.add(clickHandler);
        return new EventHandlerRegistration(clickHandler);
    }
    
    /**
     * {@inheritDoc}
     */
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Boolean> valueChangeHandler) {
        if (valueChangeHandlers == null) {
            valueChangeHandlers = new ArrayList<ValueChangeHandler<Boolean>>();
        }
        valueChangeHandlers.add(valueChangeHandler);
        return new EventHandlerRegistration(valueChangeHandler);
    }

    /**
     * {@inheritDoc}
     */
    public void onClick(final ClickEvent clickEvent) {
        for (final ClickHandler clickHandler : clickHandlers) {
            clickHandler.onClick(clickEvent);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void onValueChange(final ValueChangeEvent<Boolean> valueChangeEvent) {
        for (final ValueChangeHandler<Boolean> valueChangeHandler : valueChangeHandlers) {
            valueChangeHandler.onValueChange(valueChangeEvent);
        }
    }
    
    /**
     * Custom Handler registration
     */
    protected class EventHandlerRegistration implements HandlerRegistration {

        protected EventHandler eventHandler;
        
        public EventHandlerRegistration(final EventHandler eventHandler) {
            this.eventHandler = eventHandler;
        }
        
        public void removeHandler() {
            if (eventHandler instanceof ClickHandler) {
                clickHandlers.remove(eventHandler);
            } else if (eventHandler instanceof ValueChangeHandler<?>) {
                valueChangeHandlers.remove(eventHandler);
            }
        }
    }
}
