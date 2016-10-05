# Configuration Extensions

Extensions to the [commons-configuration](https://commons.apache.org/proper/commons-configuration/) library

## Credits
Darren Bruxvoort, for sparking this idea.

## Usage

### RuntimeConfiguration

In many large scale environments there are several different levels of testing 
as well as production.  At each of these levels there needs to be a unique set
of configurations.  RuntimeConfiguration is an additional configuration that makes
leveled configuration possible.  You use it like any other commons-configuration
component.  The only difference is the resource file, which is structured xml data.

```
<?xml version="1.0" encoding="UTF-8"?>
<conf:configuration 
  xmlns:conf="http://commons.apache.org/schema/runtime-configuration-1.0.0" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xsi:schemaLocation="http://commons.apache.org/schema/runtime-configuration-1.0.0 runtime-configuration-1.0.0">
  <conf:context>
    <conf:hosts env="DEV">
      <conf:host>localhost</conf:host>
      <conf:host>127.0.0.1</conf:host>
    <conf:hosts>
    <conf:hosts env="QA">
      <conf:host>fully.qualified.domain.name</conf:host>
      <conf:host>10.10.10.100</conf:host>
    </conf:hosts>
  </conf:context>
      
  <conf:property key="database_url">
    <conf:value env="DEV">jdbc:mysql://localhost/inventory</conf:value>
    <conf:value env="QA">jdbc:mysql://10.10.8.200/inventory</conf:value>
  </conf:property>
  <conf:property key="api_key">
  	<conf:value env="*">1234567890abcdefg0987654321</conf:value>
  </conf:proprty>
</conf:configuration>
```

Then you use the configuration as usual.

```
RuntimeConfiguration config = new RuntimeConfiguration("/path/to/config.xml");
String url = config.getString("database_url");
```

## Spring Utilities
Utilities are provided for Spring dependency injection in conjunction with commons
configuration.

### ConfigurationFactory
Aids in the creation of a spring injected Configurations.

```
<bean id="AppConfig" class="commons.configuration.ext.spring.ConfigurationFactory">
  <property name="configurations">
   <list>
     <bean class="org.apache.commons.configuration.SystemConfiguration" />
     <bean class="commons.configuration.ext.RuntimeConfiguration">
       <constructor-arg value="path/to/config/file" />
     </bean>
   </list>
  </property>
</bean>

<bean class="a.b.c.MyClass">
  <property name="configuration" ref="AppConfig" />
</bean>
```

### ConfigurationPlaceholderConfigurer
A configuration resource configurer that resolves placeholders in bean property
values of context definitions. It pulls values from a configuration into bean 
definitions.  The default placeholder syntax follows the JSF style: #{...}

```
<!-- Prepare configuration -->
<bean id="AppConfig" class="commons.configuration.ext.spring.ConfigurationFactory">
  <property name="configurations">
    <list>
      <bean class="commons.configuration.ext.RuntimeConfiguration">
        <constructor-arg value="classpath:application-config.xml" type="java.io.File" />
      </bean>
    </list>
  </property>
</bean>

<!-- Prepare configuration placeholder -->
<bean class="commons.configuration.ext.spring.ConfigurationPlaceholderConfigurer">
  <property name="configuration" ref="AppConfig" />
</bean>

<!-- Use configuration placeholder(s) -->
<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
  <property name="driverClassName" value="#{driver}"/>
  <property name="url" value="jdbc:#{dbname}" />
</bean>
```

## License

Apache Licence 2.0
