# OpenLAP Analytics Method Template

This repository provides the small Java library contract used to implement OpenLAP analytics methods. It is a Maven
Java library, not a Spring Boot application.

## Requirements

- Java 8-compatible source and bytecode.
- Maven via the included `./mvnw` wrapper.
- `openlap-official-data-exchange-formats` version `1.2.1`.

The data-exchange library serializes column types with the current enum names `Text`, `Numeric`, and `TrueFalse`.
For backward compatibility it also accepts legacy input names such as `STRING`, `INTEGER`, and `BOOLEAN` when
deserializing JSON fixtures or client payloads. New examples and generated payloads should prefer the current enum names.

## Analytics Method Contract

An analytics method extends `com.openlap.template.AnalyticsMethod`.

Implementations are expected to:

- Set `input`, `output`, optional `params`, and `type` in the constructor.
- Use `OpenLAPDataColumnFactory` for input and output columns.
- Use `OpenLAPDynamicParams` only when the method needs runtime parameters.
- Receive incoming data through `initialize(OpenLAPDataSet, OpenLAPPortConfig)`.
- Receive incoming data and user parameters through `initialize(OpenLAPDataSet, OpenLAPPortConfig, Map<String, String>)`.
- Put custom analytics logic in `implementationExecution()`.
- Populate `getOutput()` before `execute()` returns.
- Implement `hasPMML()` and `getPMMLInputStream()`; return `false` and `null` when PMML is not used.

The usual lifecycle is:

1. Construct the analytics method.
2. Call `initialize(...)` to validate and map the input dataset into the method input ports.
3. Call `execute()`.
4. Read the returned `OpenLAPDataSet`.

## Maven Dependency

This template currently depends on:

```xml
<dependency>
    <groupId>com.github.ude-soco</groupId>
    <artifactId>openlap-official-data-exchange-formats</artifactId>
    <version>1.2.1</version>
</dependency>
```

The JitPack repository must be available:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Example Analytics Method

The following example counts item occurrences. It uses Java 8-compatible syntax and fails clearly if constructor setup
cannot create columns or dynamic parameters.

```java
package com.openlap.AnalyticsMethods.Prototypes;

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
        setType("normal");

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
            String itemName = String.valueOf(item);
            Integer currentCount = counts.get(itemName);
            counts.put(itemName, currentCount == null ? 1 : currentCount + 1);
        }

        OpenLAPDynamicParam<?> returnCountParam = getParams().getParams().get("return_count");
        int returnCount = ((Number) returnCountParam.getValue()).intValue();

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
```

## Input Mapping

`initialize(...)` uses `OpenLAPPortConfig` to map source dataset columns into the method input columns. The
`outputPort` in each mapping refers to a column in the incoming dataset. The `inputPort` refers to the analytics
method input column.

```json
{
  "mapping": [
    {
      "outputPort": {
        "type": "Text",
        "id": "incoming_items",
        "required": true
      },
      "inputPort": {
        "type": "Text",
        "id": "items",
        "required": true
      }
    }
  ]
}
```

Older JSON using `"STRING"` for text or `"INTEGER"` for numeric values is still accepted by
`openlap-official-data-exchange-formats` `1.2.1`, but new JSON should use `"Text"`, `"Numeric"`, and `"TrueFalse"`.

## PMML

PMML is optional. If an analytics method does not use PMML, implement:

```java
@Override
public Boolean hasPMML() {
    return false;
}

@Override
public InputStream getPMMLInputStream() {
    return null;
}
```

If `hasPMML()` returns `true`, `getPMMLInputStream()` should return an input stream for the PMML file packaged with the
analytics method JAR.

## Development

Run the tests with:

```bash
./mvnw clean test
```

Inspect dependencies with:

```bash
./mvnw dependency:tree
```

The project intentionally keeps Java 8 source/target compatibility for now.
