package org.example.openlap;

import com.openlap.dataset.OpenLAPColumnDataType;
import com.openlap.dataset.OpenLAPDataColumnFactory;
import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.dynamicparam.OpenLAPDynamicParam;
import com.openlap.dynamicparam.OpenLAPDynamicParamDataType;
import com.openlap.dynamicparam.OpenLAPDynamicParamFactory;
import com.openlap.dynamicparam.OpenLAPDynamicParamType;
import com.openlap.dynamicparam.OpenLAPDynamicParams;
import com.openlap.exceptions.OpenLAPDataColumnException;
import com.openlap.exceptions.OpenLAPDynamicParamException;
import com.openlap.template.AnalyticsMethod;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemCount extends AnalyticsMethod {

    public ItemCount() {
        setInput(new OpenLAPDataSet());
        setOutput(new OpenLAPDataSet());
        setParams(new OpenLAPDynamicParams());
        setType("descriptive");

        try {
            getInput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(
                            "items",
                            OpenLAPColumnDataType.Text,
                            true,
                            "Items List",
                            "List of items to count"));

            getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(
                            "item_name",
                            OpenLAPColumnDataType.Text,
                            false,
                            "Item Names",
                            "Counted item names"));

            getOutput().addOpenLAPDataColumn(
                    OpenLAPDataColumnFactory.createOpenLAPDataColumnOfType(
                            "item_count",
                            OpenLAPColumnDataType.Numeric,
                            false,
                            "Item Count",
                            "Number of times each item occurred"));

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
            throw new IllegalStateException("Could not initialize ItemCount columns", exception);
        } catch (OpenLAPDynamicParamException exception) {
            throw new IllegalStateException("Could not initialize ItemCount parameters", exception);
        }
    }

    @Override
    protected void implementationExecution() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();
        List<?> items = getInput().getColumns().get("items").getData();

        for (Object item : items) {
            if (item == null || item.toString().trim().isEmpty()) {
                continue;
            }
            String itemName = item.toString();
            Integer currentCount = counts.get(itemName);
            counts.put(itemName, currentCount == null ? 1 : currentCount + 1);
        }

        OpenLAPDynamicParam<?> returnCountParam = getParams().getParams().get("return_count");
        int returnCount = ((Number) returnCountParam.getValue()).intValue();
        if (returnCount < 1) {
            throw new IllegalArgumentException("Choose at least one item to return.");
        }

        getOutput().getColumns().get("item_name").getData().clear();
        getOutput().getColumns().get("item_count").getData().clear();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
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
        return "Item Count";
    }

    @Override
    public String getAnalyticsMethodDescription() {
        return "Counts item occurrences in a text column.";
    }

    @Override
    public String getAnalyticsMethodCreator() {
        return "Developer Name";
    }
}
