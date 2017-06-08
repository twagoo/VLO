/*
 * Copyright (C) 2015 CLARIN
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

import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import static eu.clarin.cmdi.vlo.importer.LanguageCodePostProcessor.CODE_PREFIX;
import static eu.clarin.cmdi.vlo.importer.LanguageCodePostProcessor.LANG_NAME_PREFIX;
import java.util.Collections;
import java.util.List;

/**
 * Post processor that converts a languageCode value (as generated by the
 * {@link LanguageCodePostProcessor}) into a languageName value as it should be
 * display in the front end application
 *
 * @author Twan Goosen <twan.goosen@mpi.nl>
 */
class LanguageNamePostProcessor implements PostProcessor {

    @Override
    public List<String> process(String value, CMDIData cmdiData) {
        if (value.startsWith(LANG_NAME_PREFIX)) {
            return returnName(value);
        } else if (value.startsWith(CODE_PREFIX)) {
            return returnNameForCode(value);
        } else {
            // invalid value (name nor code)!!
            return Collections.emptyList();
        }
    }

    @Override
    public boolean doesProcessNoValue() {
        return false;
    }

    /**
     *
     * @param value original value string with <em>code</em> prefix
     * @return language name for the code according to {@link LanguageCodeUtils#getLanguageNameForLanguageCode(java.lang.String)
     * }
     */
    private List<String> returnNameForCode(String value) {
        final LanguageCodeUtils languageCodeUtils = MetadataImporter.languageCodeUtils;
        final String code = value.substring(CODE_PREFIX.length());
        final String languageName = languageCodeUtils.getLanguageNameForLanguageCode(code.toUpperCase());
        return Collections.singletonList(languageName);
    }

    /**
     *
     * @param value original value string with <em>name</em> prefix
     * @return verbatim language string
     */
    private List<String> returnName(String value) {
        return Collections.singletonList(value.substring(LANG_NAME_PREFIX.length()));
    }

}
