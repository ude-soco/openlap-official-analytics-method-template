import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.template.AnalyticsMethod;
import com.openlap.exceptions.AnalyticsMethodInitializationException;
import com.openlap.dataset.OpenLAPColumnConfigData;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.dynamicparam.OpenLAPDynamicParam;
import com.openlap.dynamicparam.OpenLAPDynamicParamDataType;
import com.openlap.dynamicparam.OpenLAPDynamicParamFactory;
import com.openlap.dynamicparam.OpenLAPDynamicParamType;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDynamicParamException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for AnalyticsMethodFRamework
 */
public class AnalyticsMethodFrameworkTests {


    AnalyticsMethod testMethod1;
    OpenLAPDataSet inputDataSet;
    OpenLAPPortConfig configuration1;

    ObjectMapper mapper;

    @Before
    public void beforeTests() throws IOException {
        mapper = objectMapper();

        testMethod1 = new AnalyticsMethodsTestImplementation();
        // Initialize mockup DataSet for inputDataSet to the analytics method
        inputDataSet = mapper.readValue(resource("DataSetSample.json"), OpenLAPDataSet.class);
        // Initialize mockup OpenLAPPortConfig
        configuration1 = mapper.
                readValue(resource("ConfigurationSample.json"),
                        OpenLAPPortConfig.class);
    }

    // Test initialization
    @Test
    public void testInitialization() throws AnalyticsMethodInitializationException {
        // Make Input
        //
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList("bananito", "abc" ));
        testMethod1.initialize(inputDataSet, configuration1);
        Assert.assertEquals(expected, testMethod1.getInput().getColumns().get("inputColumn1").getData());
    }

    // Test getInputs and getOutputs
    @Test
    public void testGetInputsAndOutputs() throws IOException {
        //Make object for input from json
        List<OpenLAPColumnConfigData> inputExpected = mapper.readValue(this.getClass().
                getResourceAsStream("InputConfigurationDataSample.json"),
                mapper.getTypeFactory().constructCollectionType(List.class, OpenLAPColumnConfigData.class) );
        List<OpenLAPColumnConfigData> outputExpected = mapper.readValue(this.getClass().
                getResourceAsStream("OutputConfigurationDataSample.json"),
                mapper.getTypeFactory().constructCollectionType(List.class, OpenLAPColumnConfigData.class) );
        //Make object for output from json

        Assert.assertArrayEquals(inputExpected.toArray(), testMethod1.getInputPorts().toArray());

        Assert.assertArrayEquals(outputExpected.toArray(),testMethod1.getOutputPorts().toArray());

    }

    // Test execution
    @Test
    public void testExecution() throws IOException, AnalyticsMethodInitializationException {
        //Make a dataset from json with the expected result
        OpenLAPDataSet expectedOutPutDataset = mapper.readValue(
                resource("DataSetOutputSample.json"), OpenLAPDataSet.class);
        testMethod1.initialize(inputDataSet, configuration1);

        //Execute the method
        //Assert that they are equal
        Assert.assertEquals(expectedOutPutDataset,testMethod1.execute());
    }

    //Test PMML
    @Test
    public void testPMMLLoading()throws Exception{
        Assert.assertTrue(testMethod1.hasPMML());
        Assert.assertNotNull(testMethod1.getPMMLInputStream());
    }

    @Test
    public void initializeWithNullParamsUsesInputMapping() throws AnalyticsMethodInitializationException {
        Assert.assertNull(testMethod1.getParams());

        testMethod1.initialize(inputDataSet, configuration1);

        Assert.assertEquals(
                Arrays.asList("bananito", "abc"),
                testMethod1.getInput().getColumns().get("inputColumn1").getData());
    }

    @Test
    public void initializeWithNullParamsAndAdditionalParamsUsesInputMapping()
            throws AnalyticsMethodInitializationException {
        Assert.assertNull(testMethod1.getParams());

        Map<String, String> additionalParams = new HashMap<String, String>();
        additionalParams.put("ignored", "value");
        testMethod1.initialize(inputDataSet, configuration1, additionalParams);

        Assert.assertEquals(
                Arrays.asList("bananito", "abc"),
                testMethod1.getInput().getColumns().get("inputColumn1").getData());
    }

    @Test
    public void initializeWithNullAdditionalParamsUsesDefaults()
            throws AnalyticsMethodInitializationException {
        AnalyticsMethod method = new ParameterizedAnalyticsMethod();

        method.initialize(inputDataSet, configuration1, null);

        OpenLAPDynamicParam<?> limit = method.getParams().getParams().get("limit");
        Assert.assertEquals(10, limit.getValue());
    }

    @Test
    public void initializeWithEmptyAdditionalParamsUsesDefaults()
            throws AnalyticsMethodInitializationException {
        AnalyticsMethod method = new ParameterizedAnalyticsMethod();

        method.initialize(inputDataSet, configuration1, new HashMap<String, String>());

        OpenLAPDynamicParam<?> limit = method.getParams().getParams().get("limit");
        Assert.assertEquals(10, limit.getValue());
    }

    @Test
    public void initializeWithInvalidNumericAdditionalParamFailsClearly() {
        AnalyticsMethod method = new ParameterizedAnalyticsMethod();
        Map<String, String> additionalParams = new HashMap<String, String>();
        additionalParams.put("limit", "not-a-number");

        Assert.assertThrows(
                NumberFormatException.class,
                () -> method.initialize(inputDataSet, configuration1, additionalParams));
    }

    @Test
    public void hasPMMLFalseAllowsNullPMMLInputStream() {
        AnalyticsMethod method = new NoPmmlAnalyticsMethod();

        Assert.assertFalse(method.hasPMML());
        Assert.assertNull(method.getPMMLInputStream());
    }

    @Test
    public void legacyFixtureEnumValuesDeserialize() throws IOException {
        OpenLAPDataSet dataSet = mapper.readValue(resource("DataSetSample.json"), OpenLAPDataSet.class);
        List<OpenLAPColumnConfigData> outputConfig = mapper.readValue(
                resource("OutputConfigurationDataSample.json"),
                mapper.getTypeFactory().constructCollectionType(List.class, OpenLAPColumnConfigData.class));

        Assert.assertEquals(
                OpenLAPColumnDataType.Text,
                dataSet.getColumns().get("incomingColumn").getConfigurationData().getType());
        Assert.assertEquals(OpenLAPColumnDataType.Numeric, outputConfig.get(0).getType());
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.getFactory().configure(JsonParser.Feature.ALLOW_COMMENTS, true);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(OpenLAPColumnDataType.class, new LegacyColumnDataTypeDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    private InputStream resource(String path) {
        InputStream stream = this.getClass().getResourceAsStream(path);
        Assert.assertNotNull("Missing test resource: " + path, stream);
        return stream;
    }

    private static class LegacyColumnDataTypeDeserializer extends JsonDeserializer<OpenLAPColumnDataType> {
        @Override
        public OpenLAPColumnDataType deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            String value = parser.getValueAsString();
            if ("STRING".equals(value)) {
                return OpenLAPColumnDataType.Text;
            }
            if ("INTEGER".equals(value)
                    || "FLOAT".equals(value)
                    || "LONG".equals(value)
                    || "SHORT".equals(value)
                    || "BYTE".equals(value)) {
                return OpenLAPColumnDataType.Numeric;
            }
            if ("BOOLEAN".equals(value)) {
                return OpenLAPColumnDataType.TrueFalse;
            }
            return OpenLAPColumnDataType.valueOf(value);
        }
    }

    private static class ParameterizedAnalyticsMethod extends AnalyticsMethodsTestImplementation {
        ParameterizedAnalyticsMethod() {
            OpenLAPDynamicParams params = new OpenLAPDynamicParams();
            try {
                params.addOpenLAPDynamicParam(
                        OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType(
                                "limit",
                                OpenLAPDynamicParamType.Textbox,
                                OpenLAPDynamicParamDataType.INTEGER,
                                "Limit",
                                "Maximum number of rows",
                                10,
                                "",
                                false));
            } catch (OpenLAPDynamicParamException exception) {
                throw new IllegalStateException("Could not create test dynamic parameter", exception);
            }
            setParams(params);
        }
    }

    private static class NoPmmlAnalyticsMethod extends AnalyticsMethodsTestImplementation {
        @Override
        public Boolean hasPMML() {
            return false;
        }

        @Override
        public InputStream getPMMLInputStream() {
            return null;
        }
    }
}
