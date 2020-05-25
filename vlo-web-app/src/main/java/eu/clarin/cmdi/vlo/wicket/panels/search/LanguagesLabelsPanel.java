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

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.wicket.components.RecordPageLink;
import eu.clarin.cmdi.vlo.wicket.model.LanguageLabelModel;
import org.apache.solr.common.SolrDocument;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import eu.clarin.cmdi.vlo.pojo.ExpansionState;
import org.apache.wicket.markup.html.WebMarkupContainer;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Tariq
 */
public class LanguagesLabelsPanel extends Panel {

    @SpringBean
    private FieldNameService fieldNameService;

    private static final int MAX_LABEL_LENGTH = 12;
    private static final int MAX_LANGUAGES = 5;

    private final IModel<ExpansionState> expansionStateModel;
    private final IModel<List<String>> languagesLabelsModel;
    private IModel<List<String>> languagesListModel;
    private final IModel<SolrDocument> documentModel;

    private final Link showMoreLink;

    public LanguagesLabelsPanel(String id, IModel<SolrDocument> documentModel, IModel<ExpansionState> expansionStateModel) {
        super(id, documentModel);
        this.documentModel = documentModel;
        this.expansionStateModel = expansionStateModel;

        languagesLabelsModel = new LanguageLabelModel(documentModel, fieldNameService.getFieldName(FieldKey.LANGUAGE_CODE));
        languagesListModel = () -> {
            final List<String> fullList = languagesLabelsModel.getObject();
            int numOfItems = ((expansionStateModel.getObject() == ExpansionState.COLLAPSED) ? MAX_LANGUAGES : fullList.size());
            return fullList.subList(0, Math.min(numOfItems, fullList.size()));
        };

        add(createListView("languagesList"));

        showMoreLink = new IndicatingAjaxFallbackLink<Void>("showMore") {
            @Override
            public void onClick(Optional<AjaxRequestTarget> t) {
                // toggle the expansion state
                if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                    expansionStateModel.setObject(ExpansionState.EXPANDED);
                } else {
                    expansionStateModel.setObject(ExpansionState.COLLAPSED);
                }

                t.ifPresent(target -> {
                    target.add(LanguagesLabelsPanel.this);
                });

            }
        };

        showMoreLink.add(new WebMarkupContainer("state").add(
                new AttributeModifier("class", new IModel<>() {

                    @Override
                    public String getObject() {
                        if (expansionStateModel.getObject() == ExpansionState.COLLAPSED) {
                            return "fa fa-plus-square-o";
                        } else {
                            return "fa fa-minus-square-o";
                        }
                    }
                })));
        add(showMoreLink);
        setOutputMarkupId(true);
    }

    private ListView createListView(String id) {
        return new ListView<String>(id, languagesListModel) {
            @Override
            protected void populateItem(ListItem<String> item) {
                final Link languageLink = new RecordPageLink("languageLink", documentModel);
                String label = item.getModel().getObject();
                if (label.length() > MAX_LABEL_LENGTH) {
                    label = label.substring(0, MAX_LABEL_LENGTH) + "..";
                }
                item.add(languageLink
                        .add(new Label("languageName", label))
                        .add(new AttributeModifier("title", item.getModel()))
                );
            }
        };
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        showMoreLink.setVisible(languagesLabelsModel.getObject().size() > MAX_LANGUAGES);

    }

    @Override
    public void detachModels() {
        super.detachModels();
        expansionStateModel.detach();
    }

}
