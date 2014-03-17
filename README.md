# beanval2json

Converts [beanvalidation-annotations](http://beanvalidation.org) to JSON to be used for client-side validation.

This class:

    package de.adorsys.beanval2json.test.model;
    
    public class Person implements Serializable {

        @Pattern(regexp = "[a-zA-Z]+", message = "Please enter only letters")
        private String name;
	
        @Min(value = 500)
        @Max(value = 5000)
        private BigDecimal salary;
	
    }
    
will be converted to:

    {
      "de.adorsys.beanval2json.test.model.Person.name": {
        "pattern": {
          "regexp": "[a-zA-Z]+",
          "message": "Please enter only letters"
        }
      },
      "de.adorsys.beanval2json.test.model.Person.salary": {
        "min": {
          "value": "500"
        },
        "max": {
          "value": "5000"
        }
      }
    }

A working example can be found in the [beanval2json-generator-test module](beanval2json-generator-test).

## Setup

Use the `de.adorsys.beanval2json.BeanvalAnnotationProcessor` e.g. via Maven:

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.1</version>
                <configuration>
                    <source>1.7</source>
                    <target>1.7</target>
                    <encoding>UTF-8</encoding>
                    <!-- disable annotation-processing - we will use the bsc-processor-plugin -->
                    <compilerArgument>-proc:none</compilerArgument>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.bsc.maven</groupId>
                <artifactId>maven-processor-plugin</artifactId>
                <version>2.2.4</version>
                <executions>
                    <execution>
                        <id>process</id>
                        <goals>
                            <goal>process</goal>
                        </goals>
                        <phase>generate-sources</phase>
                        <configuration>
                            <processors>
                                <processor>de.adorsys.beanval2json.BeanvalAnnotationProcessor</processor>
                            </processors>
                            <outputDirectory>${project.build.directory}/${project.build.finalName}/js</outputDirectory>
                            <options>
                                <mappingFile>mapping.properties</mappingFile>
                                <messagesFile>messages.properties</messagesFile>
                                <ignoreFile>ignore.txt</ignoreFile>
                             </options>
                        </configuration>
                    </execution>
                </executions>
                <dependencies>
                    <dependency>
                        <groupId>de.adorsys.beanval2json</groupId>
                        <artifactId>beanval2json-generator</artifactId>
                        <!-- no release by now -->
                        <version>1.0.0-SNAPSHOT</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>  

## Options

* **mappingFile:** Path to a properties-file with full-qualified property-names as key. The annotation-processor uses the full qualified names 
  as key in the generated JSON-Object. The crux of the matter is how to map the values to the input-fields. One way could be to use e.g. 
  the input-ids and overwrite the mapping in this property-file.
* **messagesFile:** Path to a properties-file with error-messages. If the messages defined in the annotations start and end with a curley bracket 
  like {javax.validation.constraints.NotNull.message} the processor will use the defined message from this file.
* **ignoreFile:** Path to a line-separted file with Classes or Properties which should not be converted to JSON.

## Validation

Beanval2Json converts beanvalidation values to JSON to be used for client-side validation. This project does no validation.
Most projects have their validation-plugins anyway which could use the generated JSON. One integration example with HTML5 form validation
can be found in the [test-project](beanval2json-generator-test/src/main/webapp/js/html5-validation.js)
