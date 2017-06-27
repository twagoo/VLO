package eu.clarin.cmdi.vlo.importer;

import eu.clarin.cmdi.vlo.config.VloConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LicensePostProcessor extends PostProcessorsWithVocabularyMap {

    public LicensePostProcessor(VloConfig config) {
        super(config);
    }

    @Override
    public List<String> process(String value, CMDIData cmdiData) {
        String normalizedVal = normalize(value);
        return normalizedVal != null ? Arrays.asList(normalizedVal) : new ArrayList<>();
    }

    @Override
    public String getNormalizationMapURL() {
        return getConfig().getLicenseURIMapUrl();
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }
}
