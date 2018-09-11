package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.FacetConstants;
import java.util.Collection;

import org.apache.solr.common.SolrInputDocument;

import eu.clarin.cmdi.vlo.FieldKey;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import org.apache.solr.common.SolrInputField;

/**
 * Represents a document of CMDI data.
 */
public class CMDIDataSolrImpl extends CMDIDataBaseImpl<SolrInputDocument> {

    /**
     * The associated solr document (not send to the solr server yet)
     */
    private SolrInputDocument doc;

    public CMDIDataSolrImpl(FieldNameService fieldNameService) {
        super(fieldNameService);
    }

    @Override
    public SolrInputDocument getDocument() {
        return doc;
    }

    /**
     * Sets a field in the doc to a certain value. Well, at least calls another
     * (private) method that actually does this.
     *
     * @param valueSet
     * @param caseInsensitive
     */
    @Override
    public void addDocField(ValueSet valueSet, boolean caseInsensitive) {
        final String fieldName = valueSet.getTargetFacetName();
        final String value = valueSet.getValue();
        if (fieldNameService.getFieldName(FieldKey.ID).equals(fieldName)) {
            setId(value.trim());
        } else {
            addDocField(fieldName, value, caseInsensitive);
        }
    }

    @Override
    public void replaceDocField(ValueSet valueSet, boolean caseInsensitive) {
        replaceDocField(valueSet.getTargetFacetName(), valueSet.getValue(), caseInsensitive);
    }

    @Override
    public void addDocField(String fieldName, Object value, boolean caseInsensitive) {
        handleDocField(fieldName, value, caseInsensitive);
    }

    @Override
    public void replaceDocField(String name, Object value, boolean caseInsensitive) {
        if (this.doc != null) {
            this.doc.removeField(name);
        }
        this.addDocField(name, value, caseInsensitive);
    }

    @Override
    public void addDocFieldIfNull(ValueSet valueSet, boolean caseInsensitive) {
        if (this.getDocField(valueSet.getTargetFacetName()) == null) {
            this.addDocField(valueSet, caseInsensitive);
        }
    }

    @Override
    public void removeField(String name) {
        this.doc.removeField(name);
    }

    @Override
    public boolean hasField(String name) {
        return this.doc.containsKey(name);
    }

    @Override
    public Collection<Object> getFieldValues(String name) {
        return this.doc.getFieldValues(name);
    }

    /**
     * Sets a field in the doc to a certain value. Before adding checks for
     * duplicates.
     *
     * @param name
     * @param value
     * @param caseInsensitive
     */
    private void handleDocField(String name, Object value, boolean caseInsensitive) {
        if (doc == null) {
            doc = new SolrInputDocument();
        }
        if (value instanceof String && !((String) value).trim().isEmpty()) {
            if (caseInsensitive) {
                value = ((String) value).toLowerCase();
            }
            Collection<Object> fieldValues = doc.getFieldValues(name);
            if (fieldValues == null || !fieldValues.contains(value)) {
                // if availability facet reduce tag to most restrictive
                if (name.equals(fieldNameService.getFieldName(FieldKey.AVAILABILITY))
                        || name.equals(fieldNameService.getFieldName(FieldKey.LICENSE_TYPE))) {
                    reduceAvailability(name, value.toString()); //TODO: move this out of this class!!
                } else {
                    doc.addField(name, value);
                }
            } // ignore double values don't add them
        }
    }

    @Override
    public Collection<Object> getDocField(String name) {
        if (doc == null) {
            return null;
        } else {
            return doc.getFieldValues(name);
        }
    }

    /**
     * In case that Availability facet has more then one value use the most
     * restrictive tag from PUB, ACA and RES. TODO: Move this to post processor
     *
     * @param field field to reduce availability values in
     * @param value value to insert (add or replace)
     */
    private void reduceAvailability(String field, String value) {
        Collection<Object> currentValues = doc.getFieldValues(field);
        // the first value
        if (currentValues == null) {
            doc.addField(field, value);
            return;
        }
        int lvlNew = availabilityToLvl(value);
        if (lvlNew == -1) {
            // other tags, add them, uniqueness has already being checked
            doc.addField(field, value);
            return;
        }
        // current level of availability
        int lvlCur = -1;
        for (Object val : currentValues) {
            int rhs = availabilityToLvl(val.toString());
            if (lvlCur < rhs) {
                lvlCur = rhs;
            }
        }
        // if new values is more restrictive replace the old with new
        if (lvlNew > lvlCur) {
            SolrInputField fOld = doc.get(field);
            SolrInputField fNew = new SolrInputField(field);
            fNew.addValue(value); // new, more restrictive value
            for (Object val : fOld.getValues()) {
                // copy other tags
                if (availabilityToLvl(val.toString()) == -1) {
                    fNew.addValue(val);
                }
            }
            doc.replace(field, fOld, fNew);
        }
    }

    private int availabilityToLvl(String availabilty) {
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
