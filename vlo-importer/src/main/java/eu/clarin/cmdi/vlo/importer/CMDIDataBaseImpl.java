/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 * @param <T> Type of document store
 */
public abstract class CMDIDataBaseImpl<T> implements DocFieldContainer {

    protected static final Logger LOG = LoggerFactory.getLogger(CMDIData.class);
    protected static final String METADATA_TYPE = "Metadata";
    protected static final String DATA_RESOURCE_TYPE = "Resource";
    protected static final String SEARCH_SERVICE_TYPE = "SearchService";
    // * Definition of the string denoting the landing page type. */
    protected static final String LANDING_PAGE_TYPE = "LandingPage";
    // * Definition of the string denoting the search page type. */
    protected static final String SEARCH_PAGE_TYPE = "SearchPage";
    protected final FieldNameService fieldNameService;
    /**
     * The unique identifier of the cmdi file.
     */
    protected String id;
    // Lists for different types of resources.
    protected final List<Resource> metaDataResources = new ArrayList<>();
    protected final List<Resource> dataResources = new ArrayList<>();
    protected final List<Resource> searchResources = new ArrayList<>();
    protected final List<Resource> landingPageResources = new ArrayList<>();
    protected final List<Resource> searchPageResources = new ArrayList<>();

    public CMDIDataBaseImpl(FieldNameService fieldNameService) {
        this.fieldNameService = fieldNameService;
    }

    public abstract T getDocument();

    /**
     * Sets a field in the doc to a certain value. Well, at least calls another
     * (private) method that actually does this.
     *
     * @param valueSet
     * @param caseInsensitive
     */
    public abstract void addDocField(ValueSet valueSet, boolean caseInsensitive);

    public abstract void replaceDocField(ValueSet valueSet, boolean caseInsensitive);

    public abstract void addDocField(String fieldName, String value, boolean caseInsensitive);

    public abstract void replaceDocField(String name, String value, boolean caseInsensitive);

    public abstract void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive);

    @Override
    public abstract Collection<Object> getDocField(String name);

    public List<Resource> getDataResources() {
        return dataResources;
    }

    public List<Resource> getMetadataResources() {
        return metaDataResources;
    }

    /**
     * Returns list of all search interfaces (preferably CQL interfaces)
     *
     * @return list of search interface resources
     */
    public List<Resource> getSearchResources() {
        return searchResources;
    }

    /**
     * Return the list of landing page resources.
     *
     * @return list of landing page resources
     */
    public List<Resource> getLandingPageResources() {
        return landingPageResources;
    }

    /**
     * Return the list of search page resources.
     *
     * @return the list
     */
    public List<Resource> getSearchPageResources() {
        return searchPageResources;
    }

    /**
     * Checks if any resources (metadata, landingpage, etc.) are available
     *
     * @return Returns true if at least one resource is available
     */
    public boolean hasResources() {
        return !(getDataResources().isEmpty() && getMetadataResources().isEmpty() && getSearchResources().isEmpty() && getSearchPageResources().isEmpty() && getLandingPageResources().isEmpty());
    }

    /**
     * Add a meta data resource to the list of resources of that type.
     *
     * Whenever the type is not one of a type supported by the CMDI
     * specification, a warning is logged.
     *
     * @param resource meta data resource
     * @param type type of the resource
     * @param mimeType mime type associated with the resource
     */
    public void addResource(String resource, String type, String mimeType) {
        if (METADATA_TYPE.equals(type)) {
            metaDataResources.add(new Resource(resource, type, mimeType));
        } else if (DATA_RESOURCE_TYPE.equals(type)) {
            dataResources.add(new Resource(resource, type, mimeType));
        } else if (SEARCH_SERVICE_TYPE.equals(type)) {
            searchResources.add(new Resource(resource, type, mimeType));
        } else if (LANDING_PAGE_TYPE.equals(type)) {
            // omit multiple LandingPages
            if (landingPageResources.isEmpty()) {
                landingPageResources.add(new Resource(resource, type, mimeType));
            } else {
                LOG.warn("Ignoring surplus landingpage: {}", resource);
            }
        } else if (SEARCH_PAGE_TYPE.equals(type)) {
            searchPageResources.add(new Resource(resource, type, mimeType));
        } else {
            LOG.warn("Ignoring unsupported resource type " + type + ", name=" + resource);
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    protected int availabilityToLvl(String availabilty) {
        if (availabilty == null) {
            return 0;
        }
        switch (availabilty) {
            case FacetConstants.AVAILABILITY_LEVEL_PUB:
                return 1;
            case FacetConstants.AVAILABILITY_LEVEL_ACA:
                return 2;
            case FacetConstants.AVAILABILITY_LEVEL_RES:
                return 3;
            default:
                return -1; // other tags
        }
    }

}
