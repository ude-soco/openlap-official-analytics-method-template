import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openlap.dataset.OpenLAPDataColumn;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.template.AnalyticsMethod;
import com.openlap.exceptions.AnalyticsMethodInitializationException;
import com.openlap.dataset.OpenLAPColumnConfigData;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dataset.OpenLAPPortConfig;
import com.openlap.dataset.OpenLAPPortMapping;
import com.openlap.dynamicparam.OpenLAPDynamicParam;
import com.openlap.dynamicparam.OpenLAPDynamicParamDataType;
import com.openlap.dynamicparam.OpenLAPDynamicParamFactory;
import com.openlap.dynamicparam.OpenLAPDynamicParamType;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.OpenLAPDynamicParamException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Test
    public void readmeStyleAnalyticsMethodInitializesAndExecutes()
            throws AnalyticsMethodInitializationException, OpenLAPDataColumnException {
        ReadmeStyleItemCount method = new ReadmeStyleItemCount();

        method.initialize(readmeStyleDataSet(), readmeStyleConfig());
        OpenLAPDataSet output = method.execute();

        Assert.assertEquals(Arrays.asList("apple", "banana"), output.getColumns().get("item_name").getData());
        Assert.assertEquals(Arrays.asList(2, 1), output.getColumns().get("item_count").getData());
        Assert.assertEquals("normal", method.getType());
    }

    @Test
    public void readmeStyleAnalyticsMethodUsesAdditionalParams()
            throws AnalyticsMethodInitializationException, OpenLAPDataColumnException {
        ReadmeStyleItemCount method = new ReadmeStyleItemCount();
        Map<String, String> additionalParams = new HashMap<String, String>();
        additionalParams.put("return_count", "1");

        method.initialize(readmeStyleDataSet(), readmeStyleConfig(), additionalParams);
        OpenLAPDataSet output = method.execute();

        Assert.assertEquals(Arrays.asList("apple"), output.getColumns().get("item_name").getData());
        Assert.assertEquals(Arrays.asList(2), output.getColumns().get("item_count").getData());
    }

    @Test
    public void readmeStyleAnalyticsMethodHasOptionalPmml() {
        ReadmeStyleItemCount method = new ReadmeStyleItemCount();

        Assert.assertFalse(method.hasPMML());
        Assert.assertNull(method.getPMMLInputStream());
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.getFactory().configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        return objectMapper;
    }

    private InputStream resource(String path) {
        InputStream stream = this.getClass().getResourceAsStream(path);
        Assert.assertNotNull("Missing test resource: " + path, stream);
        return stream;
    }

    private static OpenLAPDataSet readmeStyleDataSet() throws OpenLAPDataColumnException {
        OpenLAPDataSet dataSet = new OpenLAPDataSet();
        dataSet.addOpenLAPDataColumn(column("incoming_items", OpenLAPColumnDataType.Text, true));
        dataSet.getColumns().get("incoming_items").getData().add("apple");
        dataSet.getColumns().get("incoming_items").getData().add("banana");
        dataSet.getColumns().get("incoming_items").getData().add("apple");
        return dataSet;
    }

    private static OpenLAPPortConfig readmeStyleConfig() {
        OpenLAPPortConfig config = new OpenLAPPortConfig();
        config.getMapping()
                .add(new OpenLAPPortMapping(
                        configData("incoming_items", OpenLAPColumnDataType.Text, true),
                        configData("items", OpenLAPColumnDataType.Text, true)));
        return config;
    }

    private static OpenLAPDataColumn column(String id, OpenLAPColumnDataType type, boolean required)
            throws OpenLAPDataColumnException {
        return OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(id, type, required, null, null);
    }

    private static OpenLAPColumnConfigData configData(
            String id, OpenLAPColumnDataType type, boolean required) {
        return new OpenLAPColumnConfigData(id, type, required, null, null);
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

    private static class ReadmeStyleItemCount extends AnalyticsMethod {
        ReadmeStyleItemCount() {
            setInput(new OpenLAPDataSet());
            setOutput(new OpenLAPDataSet());
            setParams(new OpenLAPDynamicParams());
            setType("normal");

            try {
                getInput().addOpenLAPDataColumn(column("items", OpenLAPColumnDataType.Text, true));
                getOutput().addOpenLAPDataColumn(column("item_name", OpenLAPColumnDataType.Text, false));
                getOutput().addOpenLAPDataColumn(column("item_count", OpenLAPColumnDataType.Numeric, false));
                getParams().addOpenLAPDynamicParam(
                        OpenLAPDynamicParamFactory.createOpenLAPDataColumnOfType(
                                "return_count",
                                OpenLAPDynamicParamType.Textbox,
                                OpenLAPDynamicParamDataType.INTEGER,
                                "Number of items to return",
                                "Maximum number of counted items to return",
                                10,
                                "",
                                false));
            } catch (OpenLAPDataColumnException exception) {
                throw new IllegalStateException("Could not initialize README-style columns", exception);
            } catch (OpenLAPDynamicParamException exception) {
                throw new IllegalStateException("Could not initialize README-style params", exception);
            }
        }

        @Override
        protected void implementationExecution() {
            LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
            List<?> items = getInput().getColumns().get("items").getData();
            for (Object item : items) {
                String itemName = String.valueOf(item);
                Integer currentCount = counts.get(itemName);
                counts.put(itemName, currentCount == null ? 1 : currentCount + 1);
            }

            int returnCount = (Integer) getParams().getParams().get("return_count").getValue();
            Set<Map.Entry<String, Integer>> entries = counts.entrySet();
            for (Map.Entry<String, Integer> entry : entries) {
                if (returnCount == 0) {
                    break;
                }
                getOutput().getColumns().get("item_name").getData().add(entry.getKey());
                getOutput().getColumns().get("item_count").getData().add(entry.getValue());
                returnCount--;
            }
        }

        @Override
        public Boolean hasPMML() {
            return false;
        }

        @Override
        public InputStream getPMMLInputStream() {
            return null;
        }

        @Override
        public String getAnalyticsMethodName() {
            return "Count Items";
        }

        @Override
        public String getAnalyticsMethodDescription() {
            return "Counts items in a text column.";
        }

        @Override
        public String getAnalyticsMethodCreator() {
            return "OpenLAP";
        }
    }
}
