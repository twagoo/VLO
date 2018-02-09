package eu.clarin.cmdi.vlo.importer.mapping;

public class TargetFacet {
    private FacetConfiguration facetConfiguration;

    private boolean overrideExistingValues;
    private boolean removeSourceValue;
    private String value;

    public TargetFacet(FacetConfiguration facetConfiguration, String value) {
        this.facetConfiguration = facetConfiguration;
        this.overrideExistingValues = false;
        this.removeSourceValue = true;
        this.value = value;
    }

    public TargetFacet(TargetFacet targetFacet) {
        this.facetConfiguration = targetFacet.facetConfiguration;
        this.overrideExistingValues = targetFacet.overrideExistingValues;
        this.removeSourceValue = targetFacet.removeSourceValue;
        this.value = targetFacet.value;
    }

    public TargetFacet(FacetConfiguration facetConfiguration, String overrideExistingValues, String removeSourceValue) {
        this.facetConfiguration = facetConfiguration;
        setOverrideExistingValues(overrideExistingValues);
        setRemoveSourceValue(removeSourceValue);

    }

    public FacetConfiguration getFacetConfiguration() {
        return facetConfiguration;
    }

    public void setFacetConfiguration(FacetConfiguration facetConfiguration) {
        this.facetConfiguration = facetConfiguration;
    }

    public void setOverrideExistingValues(String overrideExistingValues) {
        this.overrideExistingValues = "true".equalsIgnoreCase(overrideExistingValues); // should always be false if not set explicitly true
    }

    public boolean getOverrideExistingValues() {
        return this.overrideExistingValues;
    }

    public void setRemoveSourceValue(String removeSourceValue) {
        this.removeSourceValue = "true".equalsIgnoreCase(removeSourceValue); // should always be true if not set explicitly false
    }

    public boolean getRemoveSourceValue() {
        return this.removeSourceValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TargetFacet clone() {
        return new TargetFacet(this);
    }
}
