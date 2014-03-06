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
package eu.clarin.cmdi.vlo.wicket.model;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.apache.wicket.model.IModel;

/**
 * Wraps a collection model to always return a list (passthrough if model object
 * is already a list, creates a new array list instead if needed)
 *
 * @author twagoo
 * @param <T> type of collection
 */
public class CollectionListModel<T> implements IModel<List<T>> {

    private final IModel<Collection<T>> collectionModel;

    public CollectionListModel(IModel<Collection<T>> collectionModel) {
        this.collectionModel = collectionModel;
    }

    @Override
    public List<T> getObject() {
        final Collection object = collectionModel.getObject();
        if (object instanceof List) {
            return (List<T>) object;
        }
        return Lists.newArrayList(object);
    }

    @Override
    public void setObject(List<T> object) {
        collectionModel.setObject(object);
    }

    @Override
    public void detach() {
        collectionModel.detach();
    }

}