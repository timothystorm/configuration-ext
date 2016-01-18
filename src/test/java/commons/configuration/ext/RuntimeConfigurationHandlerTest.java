package commons.configuration.ext;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RuntimeConfigurationHandlerTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    RuntimeConfigurationHandler _handler;
    Configuration               _config;

    @Before
    public void setUp() throws Exception {
        _config = createMock(Configuration.class);
        _handler = new RuntimeConfigurationHandler();
    }

    @Test
    public void load() throws Exception {
        // record expectations
        _config.addProperty("empty_key", "");
        _config.addProperty("txt_key", "txt_value");
        _config.addProperty("cdata_key", "<cdata><value/></cdata>");
        _config.addProperty("global_key", "global_value");
        replay(_config);

        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), xml().getBytes());
        _handler.load(new FileReader(tmp), _config);

        verify(_config);
    }

    @Test
    public void save() throws Exception {
        expect(_config.getKeys()).andReturn(
                Arrays.asList(new String[] { "empty_key", "txt_key", "cdata_key", "global_key" }).iterator());
        expect(_config.getProperty("empty_key")).andReturn("");
        expect(_config.getProperty("txt_key")).andReturn("good");
        expect(_config.getProperty("cdata_key")).andReturn("<cdata><value/></cdata>");
        expect(_config.getProperty("global_key")).andReturn("global_key");
        replay(_config);

        File tmp = tmpFolder.newFile();
        _handler.save(new FileWriter(tmp), _config);

        // verify config behavior
        verify(_config);
    }
    
    /**
     * @see #recordLoadExpectations()
     */
    String xml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        xml.append(
                "<configuration xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" xs:schemaLocation=\"http://commons.apache.org/schema/runtime-configuration-1.0.0 runtime-configuration-1.0.0.xsd\" xmlns=\"http://commons.apache.org/schema/runtime-configuration-1.0.0\">");
        {
            xml.append("<context>");
            {
                xml.append("<hosts env=\"0\">");
                {
                    xml.append("<host>localhost</host>");
                }
                xml.append("</hosts>");
            }
            xml.append("</context>");
        }

        { // empty property
            xml.append("<property key=\"empty_key\">");
            {
                xml.append("<value env=\"0\" />");
            }
            xml.append("</property>");
        }

        { // text property
            xml.append("<property key=\"txt_key\">");
            {
                xml.append("<value env=\"0\">");
                xml.append("txt_value");
                xml.append("</value>");
            }
            xml.append("</property>");
        }

        {// cdata property
            xml.append("<property key=\"cdata_key\">");
            {
                xml.append("<value env=\"0\">");
                xml.append("<![CDATA[<cdata><value/></cdata>]]>");
                xml.append("</value>");
            }
            xml.append("</property>");
        }

        {// global property
            xml.append("<property key=\"global_key\">");
            {
                xml.append("<value env=\"*\">");
                xml.append("global_value");
                xml.append("</value>");
            }
            xml.append("</property>");
        }

        xml.append("</configuration>");

        return xml.toString();
    }
}
