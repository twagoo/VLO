package eu.clarin.cmdi.vlo.config;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.slf4j.LoggerFactory;

/**
 * VLO configuration<br><br>
 * 
 * The annotated members in this class are the parameters by means of which
 * you can configure a VLO application like for example the VLO importer or
 * the VLO web application.
 * 
 * A member is annotated by prepending @element. When the VloConfig class is
 * reflected into the Simple framework, the framework will assign the values
 * it finds in the VloConfig.xml file to the members in the VloConfig class. 
 * 
 * By invoking the get method defined in the VloConfig class, a VLO application 
 * can query the value of a parameter. If you just instantiate an instance of 
 * the VloConfig class, the members will be assigned values from the packaged 
 * the VloConfig.xml file. 
 * 
 * Alternatively, the readConfig methods will let you specify an xml file of 
 * your choice. 
 * 
 * Whenever you like to add a parameter the VLO configuration, add a member 
 * with the appropriate name and type, and prepend an at aign to the 
 * declaration, like this:
 * 
 * {@literal @element}<br> {@literal parameterMember}<br><br>
 * 
 * The xml file used should in this case contain a definition like
 * 
 * {@literal<parameterMember>} "the value of the
 * parameter"{@literal </parameterMember>}<br><br>
 *
 * If you want to add a type of member that is not included in VloConfig class 
 * yet, or if you are looking for more information on the framework, please refer
 * to <url> <br><br> 
 * 
 * The parameters are stored statically. This means that after you have create 
 * a VloConfig object, you can reference a parameter outside the scope in which
 * the object was originally created. So after a VloConfig object has been 
 * created, get() in 
 *
 * WebAppConfig.get().getSomeParameter();<br><br>
 *
 * will return the configuration, and getSomeParameter() will return a
 * specific parameter in it. 
 * 
 * Through the get and set methods, the application is indifferent to the origin
 * of a parameter: you can get and set the value of a parameter without having
 * to worry about how the parameter was defined originally. 
 *
 * By invoking the read method, and by querying the context, the web
 * application, on initialization, determines which definition to use. 
 * 
 * Also, the get and set methods allow for a modification of the original value
 * of the parameter. For example, if the format of a parameter changes, this 
 * change can be handled in the get and set methods, instead of having to 
 * modify every reference to the parameter in the application. 
 *
 * Note on the explanation of the meaning of the parameters. Because the
 * meaning of a parameter is not local to this class, or even not local to the
 * configuration package, they are described in the general VLO
 * documentation.<br><br>
 *
 * @author keeloo
 */
@Root
public class VloConfig extends ConfigFromFile {

    /** 
     * Create a reference through which a VloConfig class object can send
     * messages to the logging framework that has been associated with the 
     * application.
     */
    private final static org.slf4j.Logger LOG =
            LoggerFactory.getLogger(VloConfig.class);

    /**
     * Because the VloConfig class uses the Simple framework via the
     * ConfigFilePersister class, implement the logging interface in 
     * that class.
     */
    private class VloConfigLogger implements ConfigFilePersister.Logger {

        @Override
        public void log(Object data) {
            
            /**
             * Send a message to the logging framework by using the 
             * LOG reference.
             */
            LOG.error(data.toString());
        }
    }
  
    // create a reference to the ConfigFilePersister's logging interface
    private static VloConfigLogger logger;

    /**
     * Constructor method
     */
    public VloConfig() {
        // create the ConfigFilePersister's logging interface
        logger = new VloConfigLogger();

        /**
         * Initialize the ConfigFilePersister's reference to the interface 
         */
        ConfigFilePersister.setLogger(VloConfig.logger);
    }
    
    /**
     * Create a static reference to the class itself to collect the members
     * denoting parameters. Because these members themselves are static, the 
     * reference will point the a fixed set of parameters. Please note that the
     * reference needs to be a protected reference because of access from one
     * of the extending VloWebApp class.
     */
    protected static VloConfig config = null;

    /**
     * Read the configuration from an XML file. 
     *
     * Please invoke this method if you want a configuration different from
     * the one that is represented in the packages XML file. 
     * 
     * @param fileName the name of the file to read the configuration from
     *
     * @return the configuration 
     */
    public static VloConfig readConfig(String fileName) {
        if (config == null) {
            // the configuration is not there yet; create it now
            config = new VloConfig();
        }

        // get the XML file configuration from the file by invoking ...
        config = (VloConfig) read(fileName, config);

        return config;
    }

    /**
     * Read the configuration from an XML file. 
     * 
     * Invole this method instead of readConfig() if you want to change
     * the parameters because the application is run in a context different
     * from the usual one. Here, modifications of the parameters inspired on
     * the difference in context can be made.
     * 
     * A web application can serve as a tipical example of a difference in 
     * context: the application itself runs in a web server container, while the
     * tests associated with the web application will be run outside this 
     * container.
     *     
     * @param fileName the name of the file to read the configuration from
     *
     * @return the configuration
     */
    public static VloConfig readTestConfig(String fileName) {
        if (config == null) {
            // the configuration is not there yet; create it now
            config = new VloConfig();
        }

        // get the XML file configuration from the file by invoking ...
        config = (VloConfig) read(fileName, config);
        
        // modify the parameters here

        return config;
    }
    
    /**
     * Return the reference to the configuration
     * 
     * @return the configuration
     */
    public static VloConfig get (){
        return config;
    }

    /**
     * VLO application parameter members<br><br>
     *
     * Initialize the annotated members in a proper way. This will allow them to
     * be linearized to corresponding elements in an XML file.
     * 
     * Please refer to the general VLO documentation for a description of the
     * member parameters.
     */
    
    @Element // directive for Simple
    private boolean deleteAllFirst = false;
    
    @Element
    private boolean printMapping = false;
    
    @ElementList // directive for Simple
    private List<DataRoot> dataRoots;
    
    public List<DataRoot> getDataRoots() {
        return dataRoots;
    }
    
    @Element
    private String vloHomeLink = "";
    
    @Element
    private String solrUrl = "";
    
    @Element
    private String profileSchemaUrl = "";

    @Element
    private String componentRegistryRESTURL = "";
    
    @Element
    private String handleServerUrl = "";
    
    @Element
    private String imdiBrowserUrl = "";
    
    /**
     * Note: the national project mapping itself is not part of the web
     * application configuration.
     */
    @Element
    private String nationalProjectMapping = "";
    
    /**
     * An array of facetFields<br><br>
     *
     * In case of an array of elements, the number of elements in the array
     * needs to be communicated to the Simple framework. The following would be
     * a correct description of an array of three facet fields<br><br>
     *
     * {@literal <facetFields length="3">}<br> 
     * {@literal    <facetField>}<br>
     * {@literal       fieldOne}<br> 
     * {@literal    </facetField>}<br>
     * {@literal    <facetField>}<br> 
     * {@literal       fieldTwo}<br>
     * {@literal    </facetField>}<br> 
     * {@literal    <facetField>}<br>
     * {@literal       fieldThree}<br> 
     * {@literal    </facetField>}<br>
     * {@literal </facetFields>}<br><br>
     *
     * To let Simple now it has to interpret the facetFields element as an
     * array, use the ElementArray directive. Use the directive to let Simple
     * know that the elements inside 'facetFields' are named 'facetField'.
     */
    @ElementArray(entry = "facetField")
    private String[] facetFields = {"", "", ""};
    
    @Element
    private String countryComponentUrl = "";
    
    @Element
    private String language2LetterCodeComponentUrl = "";
    
    @Element
    private String language3LetterCodeComponentUrl = "";
    
    @Element
    private String silToISO639CodesUrl = "";
    
    @Element
    private String FederatedContentSearchUrl = " ";

    /**
     * Get and set methods for web application parameter members<br><br>
     *
     * By using a get or set method, you can apply an operation to a parameter
     * here without the need to make changes in different parts of the
     * application.
     */
    
    /**
     * Set the value of the dataRoots parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param dataRoots the value
     */
    public void setDataRoots(List<DataRoot> dataRoots) {
        this.dataRoots = dataRoots;
    }

    /**
     * Set the value of the deleteAllFirst parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public void setDeleteAllFirst(boolean deleteAllFirst) {
        this.deleteAllFirst = deleteAllFirst;
    }

    /**
     * Get the value of the deleteAllFirst parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public boolean isDeleteAllFirst() {
        return deleteAllFirst;
    }

    /**
     * Set the value of the printMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param printMapping the value
     */
    public void setPrintMapping(boolean printMapping) {
        this.printMapping = printMapping;
    }

    /**
     * Get the value of the printMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public boolean isPrintMapping() {
        return printMapping;
    }
    
    /**
     * Get the value of the VloHomeLink parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getVloHomeLink() {
        return vloHomeLink;
    }

    /**
     * Set the value of the VloHomeLink parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param vloHomeLink the value
     */
    public void setVloHomeLink(String link) {
        this.vloHomeLink = link;
    }

    /**
     * Get the value of the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getSolrUrl() {
        return solrUrl;
    }

    /**
     * Set the value of the SolrUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param url the parameter
     */
    public void setSolrUrl(String url) {
        this.solrUrl = url;
    }

    /**
     * Get the value of the ProfileSchemaUrl by profileId parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @return the value
     */
    public String getComponentRegistryProfileSchema(String id) {
        return profileSchemaUrl.replace("{PROFILE_ID}", id);
    }

    /**
     * Set the value of the ProfileSchemaUrl parameter<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     * Note: the profileId needs to be expanded.
     *
     * @param profileId the value
     */
    public void setProfileSchemaUrl(String url) {
        this.profileSchemaUrl = url;
    }

    /**
     * Get the value of the ComponentRegisteryRESTURL parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getComponentRegistryRESTURL() {
        return componentRegistryRESTURL;
    }

    /**
     * Set the value of the ComponentRegisteryRESTURL parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param componentRegistryRESTURL the value
     */
    public void setComponentRegistryRESTURL(String url) {
        this.componentRegistryRESTURL = url;
    }

    /**
     * Get the value of the HandleServerUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getHandleServerUrl() {
        return handleServerUrl;
    }

    /**
     * Set the value of the HandleServerUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param handleServerUrl the value
     */
    public void setHandleServerUrl(String url) {
        this.handleServerUrl = url;
    }

    /**
     * Set the value of the IMDIBrowserUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param imdiBrowserUrl the value
     */
    public void setIMDIBrowserUrl(String url) {
        this.imdiBrowserUrl = url;
    }

    /**
     * Get the value of the ProfileSchemaUrl parameter combined with a handle<br><br>
     *
     * For a description of the schema, refer to the general VLO documentation.
     *
     * @return the value 
     */
    public String getIMDIBrowserUrl(String handle) {
        String result;
        try {
            result = imdiBrowserUrl + URLEncoder.encode(handle, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = imdiBrowserUrl + handle;
        }
        return result;
    }

    /**
     * Get the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public void setFederatedContentSearchUrl(String url) {
        this.FederatedContentSearchUrl = url;
    }

    /**
     * Set the value of the FederatedContentSearchUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param FederatedContentSearchUrl the value
     */
    public String getFederatedContentSearchUrl() {
        return FederatedContentSearchUrl;
    }

    /**
     * Set the value of the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param nationalProjectMapping the value
     */
    public void setNationalProjectMapping(String mapping) {
        this.nationalProjectMapping = mapping;
    }

    /**
     * Get the value of the NationalProjectMapping parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getNationalProjectMapping() {
        return nationalProjectMapping;
    }

    /**
     * Get the value of the FacetFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String[] getFacetFields() {
        return facetFields;
    }

    /**
     * Set the value of the FacetFields parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param facetFields the value, a list of facet fields
     */
    public void setFacetFields(String[] fields) {
        this.facetFields = fields;
    }

    /**
     * Get the value of the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getCountryComponentUrl() {
        return countryComponentUrl;
    }

    /**
     * Set the value of the CountryComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param countryComponentUrl the value
     */
    public void setCountryComponentUrl(String url) {
        this.countryComponentUrl = url;
    }

    /**
     * Get the value of the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getLanguage2LetterCodeComponentUrl() {
        return language2LetterCodeComponentUrl;
    }

    /**
     * Set the value of the Language2LetterCodeComponentUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param language2LetterCodeComponentUrl the value
     */
    public void setLanguage2LetterCodeComponentUrl(String url) {
        this.language2LetterCodeComponentUrl = url;
    }

    /**
     * Get the value of the Language3LetterCodeComponentUrl parameter<br><br> 
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getLanguage3LetterCodeComponentUrl() {
        return language3LetterCodeComponentUrl;
    }

    /**
     * Set the value of the Language3LetterCodeComponentUrl parameter<br><br> 
     * 
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param language3LetterCodeComponentUrl the value
     */
    public void setLanguage3LetterCodeComponentUrl(String url) {
        this.language3LetterCodeComponentUrl = url;
    }

    /**
     * Get the value of the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @return the value
     */
    public String getSilToISO639CodesUrl() {
        return silToISO639CodesUrl;
    }

    /**
     * Set the value of the SilToISO639CodesUrl parameter<br><br>
     *
     * For a description of the parameter, refer to the general VLO
     * documentation.
     *
     * @param silToISO639CodesUrl the value
     */
    public void setSilToISO639CodesUrl(String url) {
        this.silToISO639CodesUrl = url;
    }
}