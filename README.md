# OpenLAP Analytics Method Template

## INTRODUCTION

The following video gives the introduction to the OpenLAP followed by the tutorial to add new Analytics Method to the
OpenLAP.

<p align="center">
	<a href="http://www.youtube.com/watch?feature=player_embedded&v=9PdU8pQkvLU" target="_blank">
		<span><strong>Video Tutorial to add new Analytics Methods to OpenLAP</strong></span>
		<br>
		<img src="http://img.youtube.com/vi/9PdU8pQkvLU/0.jpg" alt="OpenLAP Introduction and New Analytics Method"/>
	</a>
</p>

## Fundamental Concepts

The main idea behind analytics methods is to receive the incoming data in the OpenLAP-DataSet format, apply the analysis
to this data and return the analyzed data in the OpenLAP-DataSet format. To implement a new analytics method, the
developer must extend the abstract `AnalyticsMethod` class available in the OpenLAP-AnalyticsMethodsFramework project.

### Methods of the `AnalyticsMethod` abstract class

The `AnalyticsMethod` abstract class has a series of methods that allows new classes that extend it to be used by the
OpenLAP.

#### Implemented Methods

* The `initialize()` method takes an `OpenLAPDataSet` and an `OpenLAPPortConfig` as parameters. The `AnalyticsMethod`
  will use this as its input `OpenLAPDataSet` with the incoming data if the `OpenLAPPortConfig` is valid.
* The `execute()` method returns the output `OpenLAPDataSet` after executing the `implementationExecution()` method and
  performing the analysis.
* The `getInputPorts()` and `getOutputPorts()` methods allow other classes to obtain the columns metadata as
  `OpenLAPColumnConfigData` class of the input and output `OpenLAPDataSet`.

#### Abstract Methods

* The `implementationExecution()` method is where the developer will implement the logic to interpret the incoming data
  from input `OpenLAPDataSet`, analyze it and write it to the output `OpenLAPDataSet`. This method is called by the
  `execute()` method described above to execute this analytics method. The important point here is that the analyzed
  data should be written to the output `OpenLAPDataSet` before this method ends.
* The `hasPMML()` method returns a Boolean value indicating the desire of the developer to
  use [Predictive Model Markup Language (PMML)](http://dmg.org/pmml/v4-2-1/GeneralStructure.html) in the analytics
  method. The PMML is mainly used while performing a predictive analysis. The OpenLAP provides the mechanism to validate
  the PMML XML during upload.
* The `getPMMLInputStream()`method should return an input stream to the PMML file available in the JAR bundle of the
  analytics method If the `hasPMML()` method returns `true`.

## Step by step guide to implement a new Analytics Method

The following steps must be followed by the developer to implement a new Analytics Method for the OpenLAP:

1. Setting up the development environment

2. Creating project and importing the dependencies into it.

3. Create a class that extends the `AnalyticsMethod`.

4. Define the input and output `OpenLAPDataSet`.

5. Implement the abstract methods.

6. Pack the binaries into a JAR bundle.

7. Upload the JAR bundle using the OpenLAP administration panel along with the configuration.

These steps are explained in more details with concrete examples in the following sections.

### Step 1. Setting up the development environment

To create a new analytics method, it is essential to install the following software:

* Java Development Kit (JDK) 7+: Ensure that you have Java Development Kit version 7 or above installed on your system (
  Amazon corretto 11 maximum in current OpenLap version).
* Any Integrated Development Environment (IDE) for Java development, such
  as, [Intellij IDEA](https://www.jetbrains.com/idea/download), [NetBeans](https://netbeans.org/downloads/), [Eclipse](https://eclipse.org/downloads/),
  etc.

In the upcoming steps, IntelliJ IDEA is used to develop a sample analytics method using Maven.

### Step 2. Creating project and importing the dependencies into it.

* Create a new project. `File -> New -> Project`
* Select `Maven` from the left and click `Next`.
* Enter the GroupId, ArtifactId and Version etc. To facilitate the retrieval of a recently implemented analytics method
  from the fat JAR file,
  it is essential to set its GroupId correctly. Otherwise, identifying the newly added analytics method class becomes
  challenging due to the presence
  of numerous class files from the dependencies within the fat JAR file. The GroupId needs to be set as "
  com.openlap.AnalyticsMethods.Prototypes"
  without the quotes.
* Add JitPack repository to the `pom.xml` file.

Maven:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

* Add dependency of the OpenLAP-AnalyticsMethodsFramework project to the ‘pom.xml’ file. The latest version of the
  dependency xml can be retrieved from
  the  [![](https://jitpack.io/v/OpenLearningAnalyticsPlatform/OpenLAP-AnalyticsMethodsFramework.svg)]([https://jitpack.io/#OpenLearningAnalyticsPlatform/OpenLAP-AnalyticsMethodsFramework](https://jitpack.io/#OpenlapDependencies/open-lap-analytics-method-template-master/)).

Maven:

```xml

<dependency>
    <groupId>com.github.OpenlapDependencies</groupId>
    <artifactId>open-lap-analytics-method-template-master</artifactId>
    <version>version1</version>
</dependency>
```

### Step 3. Create a class that extends the abstract AnalyticsMethod class.

Within the project, create a class that extends the AnalyticsMethod, following the example provided below. In this
example, the class is called ItemCount. Ensure the class resides in a package called "
com.openlap.AnalyticsMethods.Prototypes".

```java
package com.openlap.AnalyticsMethods.Prototypes;

import com.openlap.template.AnalyticsMethod;

import java.io.InputStream;

public class ItemCount extends AnalyticsMethod {
	@Override
	public String getAnalyticsMethodName() {
        ...
	}

	@Override
	public String getAnalyticsMethodDescription() {
        ...
	}

	@Override
	public String getAnalyticsMethodCreator() {
        ...
	}

	@Override
	protected void implementationExecution() {
        ...
	}

	@Override
	public InputStream getPMMLInputStream() {
        ...
	}

	@Override
	public Boolean hasPMML() {
        ...
	}
}
```

### Step 4. Define the input and output `OpenLAPDataSet`.

Define the input and output OpenLAP-DataSets within the constructor of the class "ItemCount" following the example
provided below. The input, output, params and type should be set in the constructor. The params is used to obtain
additional user input from the user interface at runtime. The type indicates whether the analytics method is of normal
or machine learning type. Inputs defined here will be the inputs expected by the class. The outputs will be on the hand
the value returned by the class in the impelementationExecution method.  (In case a new visualisation method is also to
be created by the user, it should be noted that the outputs defined here will be received in the visualization method by
for example defining inputs of the same column data type in its initializeDataSetConfiguration() method. For instance,
the current given example indicates that the output has two columns with a type text and numeric. Consequently,
visualisation methods chosen to visualize the output of this analytics method, need to expect two input columns of data
type text and numeric.)

```java
public ItemCount() {
	this.setInput(new OpenLAPDataSet());
	this.setOutput(new OpenLAPDataSet());
	this.setParams(new OpenLAPDynamicParams());
	this.setType("normal");

	try {
		this.getInput().addOpenLAPDataColumn(
				OpenLAPDataColumnFactory
						.createOpenLAPDataColumnOfType(
								"items",
								OpenLAPColumnDataType.Text,
								true,
								"Items List",
								"List of items to count")
		);
		this.getOutput().addOpenLAPDataColumn(
				OpenLAPDataColumnFactory
						.createOpenLAPDataColumnOfType(
								"item_name",
								OpenLAPColumnDataType.Text,
								true,
								"Item Names",
								"List of top 10 most occuring items in the list")
		);
		this.getOutput().addOpenLAPDataColumn(
				OpenLAPDataColumnFactory
						.createOpenLAPDataColumnOfType(
								"item_count",
								OpenLAPColumnDataType.Numeric,
								true,
								"Item Count",
								"Number of time each item occured in the list")
		);
	} catch (OpenLAPDataColumnException e) {
		e.printStackTrace();
	}

	try {
		this.getParams().addOpenLAPDynamicParam(
				OpenLAPDynamicParamFactory
						.createOpenLAPDataColumnOfType(
								"return_count",
								OpenLAPDynamicParamType.Textbox,
								OpenLAPDynamicParamDataType.INTEGER,
								"Number of items to return (N)",
								"Specify the number of items" +
										" that need to be returned. e.g. 10 will return top " +
										"10 items. -1 will return all items.",
								10,
								"",
								true));
	} catch (OpenLAPDynamicParamException var2) {
		var2.printStackTrace();
	}
}
```

### Step 5. Implement the abstract methods.

Implement the abstract methods of the extended AnalyticsMethod class, as discussed earlier. The example below
illustrates a sample implementation of the analytics method. The implementation given in the method
implementationExecution takes a list of string items as input, counts the occurrences of each item in the list, and
returns the top 10 most frequently occurring items. The developer needs to replace this code with their own business
logic.

```java

@Override
protected void implementationExecution() {
	try {
		var itemNameAndCount = new LinkedHashMap<String, Integer>();

		int returnCount = (Integer) ((OpenLAPDynamicParam)
				this.getParams()
						.getParams()
						.get("return_count")).getValue();


		//Iiterate over each item of the column
		var items = ((OpenLAPDataColumn)
				this.getInput().getColumns().get("items")).getData();

		for (Object item : items) {
			if (itemNameAndCount.containsKey(item))
				itemNameAndCount
						.put((String) item, itemNameAndCount.get((String) item) + 1);
			else
				itemNameAndCount.put((String) item, 1);
		}

		Set<Map.Entry<String, Integer>> itemsSet = itemNameAndCount.entrySet();

		if (itemsSet.size() < returnCount || returnCount == -1)
			returnCount = itemsSet.size();

		//Finding the item with the highest count,
		// adding it to the output OpenLAPDataSet
		// and removing it from the itemsSet.
		while (returnCount > 0) {
			var itemsIterator = itemsSet.iterator();

			var topEntry = itemsIterator.next();

			while (itemsIterator.hasNext()) {
				Map.Entry<String, Integer> currentEntry = itemsIterator.next();

				if (currentEntry.getValue() > topEntry.getValue())
					topEntry = currentEntry;
			}

			getOutput().getColumns().get("item_name").getData()
					.add(topEntry.getKey());

			getOutput().getColumns().get("item_count").getData()
					.add(topEntry.getValue());

			itemsSet.remove(topEntry);
			returnCount--;
		}
	} catch (Exception e) {
		System.out.println("Current analytics method taken from the jar file threw an exception:");
		System.out.println(e.getMessage() + "; at line:" + e.getStackTrace()[0].getLineNumber());
	}
}

@Override
public String getAnalyticsMethodName() {
	return "Count N";
}

@Override
public String getAnalyticsMethodDescription() {
	return "Count, sort and return N.";
}

@Override
public String getAnalyticsMethodCreator() {
	return "Developer Name";
}

@Override
public InputStream getPMMLInputStream() {
	return null;
}

@Override
public Boolean hasPMML() {
	return false;
}
```

#### Step 6. Pack the project into a fat jar file.

The project needs to be packed as a fat jar file. To instruct Maven to generate a Fat JAR from the project, one needs to
incorporate a Fat JAR build configuration into the project's POM file. In the pom.xml file the below given configuration
needs to be added:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.1.1</version>

            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
            </configuration>

            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Following this, run the following command in the terminal of IntelliJ: "mvn clean package". Maven will then generate a
Fat JAR in the "target" directory. The fat jar file will have the following format "
my-project-name-jar-with-dependencies.jar".

#### Step 7. Upload the JAR file using the OpenLAP administration panel.

Submit the JAR file to OpenLAP by uploading it through the administration panel. 
