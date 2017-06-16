/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
package eu.clarin.cmdi.vlo.wicket.panels.search;

import com.google.common.collect.ImmutableList;
import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import eu.clarin.cmdi.vlo.PiwikEventConstants;
import eu.clarin.cmdi.vlo.config.PiwikConfig;
import eu.clarin.cmdi.vlo.pojo.FacetSelection;
import eu.clarin.cmdi.vlo.pojo.FacetSelectionType;
import eu.clarin.cmdi.vlo.pojo.FieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.NameAndCountFieldValuesFilter;
import eu.clarin.cmdi.vlo.pojo.FieldValuesOrder;
import eu.clarin.cmdi.vlo.pojo.QueryFacetsSelection;
import eu.clarin.cmdi.vlo.wicket.AjaxPiwikTrackingBehavior;
import eu.clarin.cmdi.vlo.wicket.components.AjaxIndicatingForm;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueLabel;
import eu.clarin.cmdi.vlo.wicket.components.FieldValueOrderSelector;
import eu.clarin.cmdi.vlo.wicket.model.BridgeModel;
import eu.clarin.cmdi.vlo.wicket.model.BridgeOuterModel;
import eu.clarin.cmdi.vlo.wicket.model.SelectionModel;
import eu.clarin.cmdi.vlo.wicket.provider.FacetFieldValuesProvider;
import eu.clarin.cmdi.vlo.wicket.provider.FieldValueConverterProvider;
import java.util.Collection;
import java.util.Collections;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * A panel that shows all available values for a selected facet. Supports two
 * ordering modes (by name or result count) and dynamic filtering.
 *
 * TODO: group by first letter when sorted by name
 *
 * @author twagoo
 */
public class AllFacetValuesPanel extends GenericPanel<FacetField> {

    @SpringBean
    private FieldValueConverterProvider fieldValueConverterProvider;
    @SpringBean
    private PiwikConfig piwikConfig;

    private final FacetFieldValuesProvider valuesProvider;
    private final WebMarkupContainer valuesContainer;
    private final IModel<FieldValuesFilter> filterModel;
    private final IModel<FacetSelectionType> selectionTypeModeModel;
    private final IModel<QueryFacetsSelection> selectionModel;
    private final IModel<String> fieldNameModel;

    /**
     *
     * @param id component id
     * @param model model for the facet field to show values for
     * @param selectionTypeModeModel model for selection type mode (and/or/...)
     * @param selectionModel selection model
     */
    public AllFacetValuesPanel(String id, IModel<FacetField> model, IModel<FacetSelectionType> selectionTypeModeModel, final IModel<QueryFacetsSelection> selectionModel) {
        this(id, model, selectionTypeModeModel, selectionModel, null);
    }

    /**
     *
     * @param id component id
     * @param model model for the facet field to show values for
     * @param selectionTypeModeModel model for selection type mode (and/or/...)
     * @param selectionModel selection model
     * @param filterModel model that holds a string to filter in (can be null)
     */
    public AllFacetValuesPanel(String id, IModel<FacetField> model, IModel<FacetSelectionType> selectionTypeModeModel, final IModel<QueryFacetsSelection> selectionModel, IModel<FieldValuesFilter> filterModel) {
        super(id, model);

        this.selectionModel = selectionModel;
        this.selectionTypeModeModel = selectionTypeModeModel;
        this.fieldNameModel = new PropertyModel<>(getModel(), "name");

        if (filterModel != null) {
            this.filterModel = filterModel;
        } else {
            this.filterModel = new Model<FieldValuesFilter>(new NameAndCountFieldValuesFilter());
        }

        // create a provider that shows all values and is sorted by name by default
        valuesProvider = new FacetFieldValuesProvider(model, Integer.MAX_VALUE, FieldValueOrderSelector.NAME_SORT, fieldValueConverterProvider) {

            @Override
            protected IModel<FieldValuesFilter> getFilterModel() {
                // filters the values
                return AllFacetValuesPanel.this.filterModel;
            }

        };

        // create a container for the values to allow for AJAX updates
        valuesContainer = new WebMarkupContainer("facetValuesContainer");
        valuesContainer.setOutputMarkupId(true);
        add(valuesContainer);

        // create the view of selected values
        valuesContainer.add(
                new WebMarkupContainer("selectedValuesContainer")
                        .add(createSelectedValuesView("selectedValues"))
                        .add(new Behavior() {
                            @Override
                            public void onConfigure(Component component) {
                                final FacetSelection selectionValues = selectionModel.getObject().getSelectionValues(fieldNameModel.getObject());
                                component.setVisible(selectionValues != null && !selectionValues.isEmpty());
                            }
                        }));

        // create the view of the available values
        final DataView<FacetField.Count> valuesView = createValuesView("facetValue");
        valuesContainer.add(new AllValuesNavigator("navigator", valuesView));
        valuesContainer.add(new AllValuesNavigator("navigator2", valuesView));
        valuesContainer.add(createValuesInfo("valuesInfo", valuesView));
        valuesContainer.add(valuesView);

        // create the form for selection sort option and entering filter string
        final Form optionsForm = createOptionsForm("options");
        optionsForm.setOutputMarkupId(true);
        add(optionsForm);
    }

    private SelectedFacetPanel createSelectedValuesView(String id) {
        final String facet = fieldNameModel.getObject();
        return new SelectedFacetPanel(id, facet, new SelectionModel(facet, selectionModel)) {
            @Override
            protected void onValuesUnselected(Collection<String> valuesRemoved, AjaxRequestTarget target) {
                // Values have been removed, calculate remainder
                selectionModel.getObject().removeFacetValue(facet, valuesRemoved);
                if (target != null) {
                    target.add(valuesContainer);
                }
            }

        }.setRenderForCollapsed(false);
    }

    private DataView<FacetField.Count> createValuesView(final String id) {
        return new DataView<FacetField.Count>(id, valuesProvider, ITEMS_PER_PAGE) {

            @Override
            protected void populateItem(final Item<FacetField.Count> item) {
                item.setDefaultModel(new CompoundPropertyModel<>(item.getModel()));

                final AjaxPiwikTrackingBehavior.FacetValueSelectionTrackingBehaviour selectionTrackingBehavior;
                if (piwikConfig.isEnabled()) {
                    selectionTrackingBehavior = new AjaxPiwikTrackingBehavior.FacetValueSelectionTrackingBehaviour(PiwikEventConstants.PIWIK_EVENT_ACTION_FACET_SELECT, fieldNameModel, new PropertyModel<String>(item.getModel(), "name"));
                } else {
                    selectionTrackingBehavior = null;
                }

                // link to select an individual facet value
                final Link selectLink = new AjaxFallbackLink("facetSelect") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectionModel.getObject().addNewFacetValue(fieldNameModel.getObject(), selectionTypeModeModel.getObject(), Collections.singleton(item.getModelObject().getName()));
                        //detach models to make sure that facet field values get re-evaluated upon rendering
                        AllFacetValuesPanel.this.detachModels();
                        if (target != null) {
                            target.add(valuesContainer);
                            if (selectionTrackingBehavior != null) {
                                target.appendJavaScript(selectionTrackingBehavior.generatePiwikJs(target));
                            }
                        }
                    }
                };
                item.add(selectLink);

                // 'name' field from Count (name of value)
                selectLink.add(new FieldValueLabel("name", fieldNameModel));

                // 'count' field from Count (document count for value)
                item.add(new Label("count"));
            }
        };
    }
    private static final int ITEMS_PER_PAGE = 50;

    private Component createValuesInfo(String id, final DataView<FacetField.Count> view) {
        return new Label(id, new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (view.getItemCount() == 0) {
                    final FacetSelection selectionValues = selectionModel.getObject().getSelectionValues(AllFacetValuesPanel.this.getModelObject().getName());
                    if (selectionValues == null || selectionValues.isEmpty()) {
                        return "No matching values available.";
                    } else {
                        return "No matching values remaining. Click \"Done\" to apply the current selection.";
                    }
                } else if (view.getPageCount() <= 1) {
                    return String.format("Showing %d available values. Select one or more and click \"Done\":", view.getItemCount());
                } else {
                    final long offset = view.getFirstItemOffset();
                    return String.format("Showing items %d - %d of %d matching values. Select one or more and click \"Done\":", offset + 1, offset + view.getViewSize(), view.getItemCount());
                }
            }
        });
    }

    private Form createOptionsForm(String id) {
        final Form options = new AjaxIndicatingForm(id);

        final DropDownChoice<SortParam<FieldValuesOrder>> sortSelect
                = new FieldValueOrderSelector("sort", new PropertyModel<SortParam<FieldValuesOrder>>(valuesProvider, "sort"));
        sortSelect.add(new UpdateOptionsFormBehavior(options));
        options.add(sortSelect);

        final TextField filterField = new TextField<>("filter", new PropertyModel(filterModel, "name"));
        filterField.add(new AjaxFormComponentUpdatingBehavior("keyup") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(valuesContainer);
            }
        });
        options.add(filterField);

        addOccurenceOptions(options);

        return options;
    }

    /**
     * Creates form controls for minimal occurrences options.
     *
     * This requires a 'bridge' construct to combine a checkbox to
     * enable/disable filtering by occurence altogether (which actually sets the
     * min occurence to 0) and a dropdown for the minimal number of occurences,
     * which only gets applied if the checkbox is ticket.
     *
     * The checkbox opens/closes a bridge between the minimal occurences in the
     * filter model and the model that holds the selected option (modulated via
     * the dropdown).
     *
     * @param options options form
     */
    private void addOccurenceOptions(final Form options) {

        // Model that holds the actual number of occurences filtered on
        final IModel<Integer> minOccurenceModel = new PropertyModel<>(filterModel, "minimalOccurence");
        // Model that represents the filter state ('bridge' between filter and selection)
        final IModel<Boolean> bridgeStateModel = Model.of(false);
        // Model that represents the *selected* number of minimal occurences (passes it on if not decoupled)
        final IModel<Integer> minOccurenceSelectModel = new BridgeOuterModel<>(minOccurenceModel, bridgeStateModel, 2);
        // Model that links the actual filter, selection and bridge (object opens and closes it)
        final IModel<Boolean> minOccurenceCheckBoxModel = new BridgeModel<>(minOccurenceModel, minOccurenceSelectModel, bridgeStateModel, 0);

        // checkbox to open and close the 'bridge'
        final CheckBox minOccurenceToggle = new CheckBox("minOccurrencesToggle", minOccurenceCheckBoxModel);
        minOccurenceToggle.add(new UpdateOptionsFormBehavior(options));
        options.add(minOccurenceToggle);

        // Dropdown to select a value (which is applied to the filter if the 'bridge' is open)
        final DropDownChoice<Integer> minOccurence = new DropDownChoice<>("minOccurences", minOccurenceSelectModel, ImmutableList.of(2, 5, 10, 100, 1000));
        minOccurence.add(new UpdateOptionsFormBehavior(options) {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                // on change, apply to inner model ('open bridge')
                if (!minOccurenceCheckBoxModel.getObject()) {
                    minOccurenceCheckBoxModel.setObject(true);
                }
            }

        });
        options.add(minOccurence);
    }

    private class UpdateOptionsFormBehavior extends OnChangeAjaxBehavior {

        private final Form options;

        public UpdateOptionsFormBehavior(Form options) {
            this.options = options;
        }

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
            target.add(options);
            target.add(valuesContainer);
        }

    }

    @Override
    public void detachModels() {
        super.detachModels();
        if (filterModel != null) {
            filterModel.detach();
        }
        if (fieldNameModel != null) {
            fieldNameModel.detach();
        }
        if (selectionModel != null) {
            selectionModel.detach();
        }
        if (selectionTypeModeModel != null) {
            selectionTypeModeModel.detach();
        }
    }

    private static class AllValuesNavigator extends BootstrapAjaxPagingNavigator {

        private final DataView<FacetField.Count> valuesView;

        public AllValuesNavigator(String id, DataView<FacetField.Count> valuesView) {
            super(id, valuesView);
            this.valuesView = valuesView;
        }

        @Override
        protected void onConfigure() {
            super.onConfigure();
            setVisible(valuesView.getPageCount() > 1);
        }
    }

}
