<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="
        http://maven.apache.org/POM/4.0.0
        http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>gov.anl.aps.cdb.test</groupId>
    <artifactId>cdb-unit-tests</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>CdbWebPortal Unit Tests</name>    

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <defaultGoal>test</defaultGoal>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.0.0</version>
                <executions>                    
                    <execution>
                        <configuration>
                            <executable>bash</executable>
                            <arguments>
                                <argument>preparebuild.sh</argument>
                            </arguments>    
                        </configuration>
                        <phase>initialize</phase>
                        <goals>
                            <goal>exec</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>     
            <!-- The compiler plugin enforces Java 1.6 compatibility and controls execution of annotation processors  -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <release>11</release>
                </configuration>
            </plugin>
            <plugin>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M5</version>
            </plugin>          
        </plugins>
    </build>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.jboss.arquillian</groupId>
                <artifactId>arquillian-bom</artifactId>
                <version>1.6.0.Final</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>        
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.jboss.arquillian.junit</groupId>
            <artifactId>arquillian-junit-container</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi</artifactId>
            <version>4.0.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.13.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>2.13.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.0</version>
        </dependency>        
        <dependency>
            <groupId>org.primefaces</groupId>
            <artifactId>primefaces</artifactId>
            <version>8.0</version>
        </dependency>        
        <dependency>
            <groupId>org.primefaces.extensions</groupId>
            <artifactId>primefaces-extensions</artifactId>
            <version>8.0</version>
        </dependency>
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itextpdf</artifactId>
            <version>5.5.13.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.5</version>
        </dependency>
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.16</version>
        </dependency>
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.16</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.16</version>
        </dependency>
        <dependency>
            <groupId>org.eclipse.persistence</groupId>
            <artifactId>org.eclipse.persistence.jpa</artifactId>
            <version>2.6.4</version>
        </dependency>
        
        <dependency>
            <groupId>javax.persistence</groupId>
            <artifactId>persistence-api</artifactId>
            <version>1.0.2</version>
        </dependency>

        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <scope>test</scope>
            <version>2.44.0</version>
        </dependency>
        <dependency>
            <groupId>com.opera</groupId>
            <artifactId>operadriver</artifactId>
            <scope>test</scope>
            <version>1.5</version>
            <exclusions>
                <exclusion>
                    <groupId>org.seleniumhq.selenium</groupId>
                    <artifactId>selenium-remote-driver</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>2.9.9</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-annotations</artifactId>
            <version>2.0.8</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>2.29</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>io.swagger.core.v3</groupId>
            <artifactId>swagger-jaxrs2</artifactId>
            <version>2.0.8</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>com.drewnoakes</groupId>
            <artifactId>metadata-extractor</artifactId>
            <version>2.12.0</version>
            <type>jar</type>
        </dependency>   
        
        <dependency>
            <groupId>com.hazelcast</groupId>
            <artifactId>hazelcast</artifactId>
            <version>4.1.1</version>
        </dependency>
                 
        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
            <version>1.3.2</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>javax.ws.rs</groupId>
            <artifactId>javax.ws.rs-api</artifactId>
            <version>2.1</version>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>fish.payara.extras</groupId>
            <artifactId>payara-embedded-all</artifactId>
            <version>5.2020.7</version>
            <scope>test</scope>
            <type>jar</type>
        </dependency>
        <dependency>
            <groupId>javax</groupId>
            <artifactId>javaee-api</artifactId>
            <version>8.0.1</version>
            <type>jar</type>
        </dependency>
        
        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <version>2.0.1.Final</version>
        </dependency>                        
    </dependencies>
    <profiles>
        <profile>
            <id>arquillian-glassfish-embedded</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencies>
                <dependency>
                    <groupId>fish.payara.arquillian</groupId>
                    <artifactId>payara-container-common</artifactId>
                    <version>2.3.3</version>
                </dependency>
                <dependency>
                    <groupId>fish.payara.arquillian</groupId>
                    <artifactId>arquillian-payara-server-embedded</artifactId>
                    <version>2.3.3</version>
                    <scope>test</scope>
                </dependency>
                <dependency>
                    <groupId>fish.payara.extras</groupId>
                    <artifactId>payara-embedded-all</artifactId>
                    <version>5.2020.7</version>  
                    <scope>provided</scope>
                </dependency>
                
                <dependency>
                    <groupId>fish.payara.extras</groupId>
                    <artifactId>payara-embedded-all</artifactId>
                    <version>5.2020.7</version>
                    <scope>test</scope>
                </dependency>

                
                <dependency>
                    <groupId>org.dbunit</groupId>
                    <artifactId>dbunit</artifactId>
                    <version>2.7.0</version>
                </dependency>
                <dependency>
                    <groupId>org.apache.derby</groupId>
                    <artifactId>derby</artifactId>
                    <version>10.15.2.0</version>
                    <scope>test</scope>
                </dependency>
                <dependency>
                    <groupId>org.apache.derby</groupId>
                    <artifactId>derbyclient</artifactId>
                    <version>10.15.2.0</version>
                    <scope>test</scope>
                </dependency>
            </dependencies>
            <build>
                <testResources>
                    <testResource>
                        <directory>src/test/resources</directory>
                    </testResource>
                    <testResource>
                        <directory>src/test/resources-glassfish-embedded</directory>
                    </testResource>
                </testResources>
                <plugins>
                    <!-- The surefire plugin is configured to pass a system property to GlassFish to retarget the derby log file -->
                    <plugin>
                        <artifactId>maven-surefire-plugin</artifactId>
                        <version>2.22.2</version>
                        <configuration>
                            <systemProperties>
                                <adminHttps>true</adminHttps>
                            </systemProperties>
                            <systemPropertyVariables>
                                <java.util.logging.config.file>
                                    ${project.build.testOutputDirectory}/logging.properties
                                </java.util.logging.config.file>
                                <derby.stream.error.file>
                                    ${project.build.directory}/derby.log
                                </derby.stream.error.file>
                            </systemPropertyVariables>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>        
    </profiles>
</project>
