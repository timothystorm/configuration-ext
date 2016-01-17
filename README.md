# Configuration Extensions

Extensions to the [commons-configuration](https://commons.apache.org/proper/commons-configuration/) library

## Credits
Darren Bruxvoort for sparking this idea.

## Usage

EnvConfiguration.java

In many large scale environments there are several different levels of testing 
and production.  At each of these test levels there needs to be a unique set
of configurations.  EnvConfiguration is an additional configuration that makes
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
EnvConfiguration config = new EnvConfiguration("/path/to/config.xml");
String url = config.getString("database_url");
```
## History

Initial deployment

## License

Apache Licence 2.0
