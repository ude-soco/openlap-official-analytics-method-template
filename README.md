# OpenLAP Analytics Method Template

Use this Java library to build an analytics method for OpenLAP. A plugin JAR
contains your calculation and defines its inputs, outputs and settings. An
optional manifest tells OpenLAP where to recommend the method and how to
interpret its columns.

## Build your first plugin

Use JDK 17 or 21. From this repository, build and inspect the **Item Count** example.

```sh
./mvnw -f examples/item-count/pom.xml clean package
jar tf examples/item-count/target/item-count-1.0.0.jar
```

The JAR contains `org/example/openlap/ItemCount.class` and
`META-INF/openlap/analytics.json`.

- **Local test**. Copy the JAR to the backend's `jars/analytics_techniques/`
  directory and restart the backend. If the backend uses `DATA_DIR`, use the
  `jars/analytics_techniques/` directory inside it.
- **Managed OpenLAP**. An administrator can start a new installation under
  **Admin Dashboard → Analytics Methods → Add analytics methods**.

In a Basic Indicator, select a dataset, choose **Item Count**, and map
**Items List** to a text value such as the activity name. You can set
**Number of items to return** in the method settings.

## Create a method

1. Copy the example project to your own directory and edit its Maven
   coordinates. Match the API dependency versions to your OpenLAP backend.
   Maven's `provided` scope lets OpenLAP supply the shared Java classes at
   runtime. [Example POM](examples/item-count/pom.xml)
2. Extend `com.openlap.template.AnalyticsMethod` in a public class with a public
   no-argument constructor. Define the columns and any parameters in the
   constructor, then write the calculation in `implementationExecution()`.
   [Item Count source](examples/item-count/src/main/java/org/example/openlap/ItemCount.java)
3. Update the optional manifest to match your class and its column IDs. Keep
   it at `src/main/resources/META-INF/openlap/analytics.json`.
   [Example manifest](examples/item-count/src/main/resources/META-INF/openlap/analytics.json)
4. Run `mvn clean package` from your project. The plugin JAR is in `target/`.

| Where | What to define |
| --- | --- |
| Java method | Method name and description, calculation, input and output column IDs, titles, descriptions, types and required flags. Define parameter labels, defaults and choices here too. |
| Optional manifest | Which indicator editors to recommend the method in, what each input represents, and how to display important outputs. |
| OpenLAP | Available xAPI fields, learner privacy, validation of mappings and combining indicator results. |

### Java method

Use `OpenLAPDataColumnFactory` to define columns as `Text`, `Numeric` or
`TrueFalse`. Keep column IDs stable so users can reopen saved mappings.
Initialize `OpenLAPDynamicParams`, using an empty collection when the method
has no settings. OpenLAP calls `initialize(...)` to map input values and apply
settings. It then calls `execute()`, which returns the output dataset filled
by your method.

In a mapping, `outputPort` refers to a source column and `inputPort` refers to
your method's input column. In the example, the source column containing
activity names is mapped to the method's `items` input.

For a method that uses PMML, return `true` from `hasPMML()` and supply an input
stream for the packaged PMML file from `getPMMLInputStream()`. The Item Count
example returns `false` and `null`.

### Optional manifest

Set `schemaVersion` to `1` and add one entry under `methods` for each method you
want to describe. `className` is the full Java class name, including its
package. Match the input and output IDs to those in the Java constructor.

| Field | What it does |
| --- | --- |
| `recommendedFor` | Lists `BASIC`, `MULTI_LEVEL`, or both. These labels help people find a method. The Java input definitions govern which columns can be mapped. |
| `inputRoles` | Describes an input as `EVENTS`, `CATEGORY`, `TIMESTAMP`, `SCORE`, or `OUTCOME`. OpenLAP uses the roles to check which fields could supply the required data. |
| `outputHints` | Use `primary: true` for a main measure. Use `format: "PERCENTAGE"` for numeric percentages on the 0–100 scale. A `role` can identify `X`, `Y`, `LABEL` or `CLUSTER` columns. `labelFromInput` takes the input ID whose selected source label should appear on the output. |
| `outputHints.<output ID>.diagnostic` | Describes a count of `INCLUDED`, `EXCLUDED`, or `UNGROUPED` rows. Use `aggregation: "ONCE"` for a whole-result count repeated on each row or `"SUM"` for counts that should be added together. Set `summaryOnly: true` to display the count in a summary. An optional `input` names the associated method input. |

In the example, the manifest recommends Item Count in Basic Indicator, gives
`items` the category role, and marks `item_count` as the main measure. Older
plugins can continue using their Java definitions alone.

## Develop the template library

From this repository, run its tests with `./mvnw clean test`. Use
`./mvnw dependency:tree` to inspect the library's dependencies. The library
targets Java 8 bytecode.
