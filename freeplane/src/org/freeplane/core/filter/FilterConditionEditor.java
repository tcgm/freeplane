/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.ref.WeakReference;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.IElementaryConditionController;
import org.freeplane.core.filter.util.ExtendedComboBoxModel;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.core.resources.ResourceBundles;

/**
 * @author Dimitry Polivaev
 * 23.05.2009
 */
public class FilterConditionEditor extends Box{
	private class ElementaryConditionChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final Object property = filteredPropertiesModel.getSelectedItem();
				final NamedObject selectedItem = (NamedObject) elementaryConditions.getSelectedItem();
				final IElementaryConditionController conditionController = filterController.getConditionFactory()
				    .getConditionController(property);
				final boolean canSelectValues = conditionController.canSelectValues(property, selectedItem);
				values.setEnabled(canSelectValues);
				caseInsensitive.setEnabled(canSelectValues
				        && conditionController.isCaseDependent(property, selectedItem));
			}
		}
	}

	private class FilteredPropertyChangeListener implements ItemListener {
		public void itemStateChanged(final ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				final Object selectedProperty = filteredPropertiesComponent.getSelectedItem();
				final IElementaryConditionController conditionController = filterController.getConditionFactory()
				    .getConditionController(selectedProperty);
				final ComboBoxModel simpleConditionComboBoxModel = conditionController
				    .getConditionsForProperty(selectedProperty);
				elementaryConditions.setModel(simpleConditionComboBoxModel);
				elementaryConditions.setEnabled(simpleConditionComboBoxModel.getSize() > 0);
				final NamedObject selectedCondition = (NamedObject) simpleConditionComboBoxModel.getSelectedItem();
				values.setEditable(conditionController.canEditValues(selectedProperty, selectedCondition));
				final boolean canSelectValues = conditionController
				    .canSelectValues(selectedProperty, selectedCondition);
				values.setEnabled(canSelectValues);
				values.setModel(conditionController.getValuesForProperty(selectedProperty));
				final ComboBoxEditor valueEditor = conditionController.getValueEditor();
				values.setEditor(valueEditor != null ? valueEditor : new BasicComboBoxEditor());
				if (values.getModel().getSize() > 0) {
					values.setSelectedIndex(0);
				}
				else {
					values.setSelectedIndex(-1);
				}
				caseInsensitive.setEnabled(canSelectValues
				        && conditionController.isCaseDependent(selectedProperty, selectedCondition));
				return;
			}
		}
	}
	final private JCheckBox caseInsensitive;
	final private JComboBox elementaryConditions;
	final private JComboBox values;
	final private JComboBox filteredPropertiesComponent;
	final private ExtendedComboBoxModel filteredPropertiesModel;
	final private FilterController filterController;

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public FilterConditionEditor(FilterController filterController) {
	    super(BoxLayout.X_AXIS);
	    this.filterController = filterController;
		setBorder(new EmptyBorder(5, 0, 5, 0));
		filteredPropertiesComponent = new JComboBox();
		filteredPropertiesModel = new ExtendedComboBoxModel();
		filteredPropertiesComponent.setModel(filteredPropertiesModel);
		filteredPropertiesComponent.addItemListener(new FilteredPropertyChangeListener());
		add(Box.createHorizontalGlue());
		add(filteredPropertiesComponent);
		filteredPropertiesComponent.setRenderer(filterController.getConditionRenderer());
		elementaryConditions = new JComboBox();
		elementaryConditions.addItemListener(new ElementaryConditionChangeListener());
		add(Box.createHorizontalGlue());
		add(elementaryConditions);
		elementaryConditions.setRenderer(filterController.getConditionRenderer());
		values = new JComboBox();
		add(Box.createHorizontalGlue());
		add(values);
		values.setRenderer(filterController.getConditionRenderer());
		values.setEditable(true);
		caseInsensitive = new JCheckBox();
		add(Box.createHorizontalGlue());
		add(caseInsensitive);
		caseInsensitive.setText(ResourceBundles.getText("filter_ignore_case"));
		mapChanged(filterController.getController().getMap());
   }
	private WeakReference<MapModel> lastMap;
	/**
	 */
	public void mapChanged(final MapModel newMap) {
		if (newMap != null) {
			if(lastMap != null && lastMap.get() == newMap){
				return;
			}
			filteredPropertiesModel.removeAllElements();
			final Iterator<IElementaryConditionController> conditionIterator = filterController.getConditionFactory()
			    .conditionIterator();
			while (conditionIterator.hasNext()) {
				final IElementaryConditionController next = conditionIterator.next();
				filteredPropertiesModel.addExtensionList(next.getFilteredProperties());
				filteredPropertiesModel.setSelectedItem(filteredPropertiesModel.getElementAt(0));
			}
		}
		else {
			values.setSelectedIndex(-1);
			filteredPropertiesComponent.setSelectedIndex(0);
			filteredPropertiesModel.setExtensionList(null);
		}
		lastMap = new WeakReference<MapModel>(newMap);
	}
	public ICondition getCondition() {
	    ICondition newCond;
	    final Object value = values.getSelectedItem();
	    final NamedObject simpleCond = (NamedObject) elementaryConditions.getSelectedItem();
	    final boolean ignoreCase = caseInsensitive.isSelected();
	    final Object selectedItem = filteredPropertiesComponent.getSelectedItem();
	    newCond = filterController.getConditionFactory().createCondition(selectedItem, simpleCond, value,
	        ignoreCase);
		if (values.isEditable()) {
			final Object item = values.getSelectedItem();
			if (item != null && !item.equals("")) {
				values.removeItem(item);
				values.insertItemAt(item, 0);
				values.setSelectedIndex(0);
				if (values.getItemCount() >= 10) {
					values.removeItemAt(9);
				}
			}
		}
	    return newCond;
    }
	public String getSearchTerm() {
	    return filteredPropertiesComponent.getSelectedItem().toString();
    }
	public void focusInputField() {
		if(values.isEnabled()){
			values.requestFocus();
			Component editorComponent = values.getEditor().getEditorComponent();
			if(editorComponent instanceof JTextComponent){
				((JTextComponent)editorComponent).selectAll();
			}
			return;
		}
    }
}
