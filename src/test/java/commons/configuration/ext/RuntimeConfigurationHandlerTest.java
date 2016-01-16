package commons.configuration.ext;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RuntimeConfigurationHandlerTest {
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();
    
    RuntimeConfigurationHandler _handler;
    Configuration _config;
    
    @Before
    public void setUp() throws Exception{
        _handler = new RuntimeConfigurationHandler((_config = createMock(Configuration.class)));
    }
    
    @Test
    public void readFrom() throws Exception{
        recordOmegaExpctations();
        replay(_config);
        
        File tmp = tmpFolder.newFile();
        Files.write(tmp.toPath(), omegaXml().getBytes());
        _handler.parse(new FileReader(tmp));
        
        verify(_config);
    }
    
    /**
     * @see #omegaXml()
     */
    void recordOmegaExpctations() {
        _config.addProperty("empty_key", "");
        _config.addProperty("txt_key", "good");
        _config.addProperty("cdata_key", "<custom><xml/></custom>");
    }
    
    /**
     * @see #recordOmegaExpctations()
     */
    String omegaXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<configuration>");
        {
            xml.append("<context>");
            {
                xml.append("<hosts env=\"0\">");
                { // localhost
                    xml.append("<host>localhost</host>");
                    xml.append("<host>127.0.0.1</host>");
                    xml.append("<host>::0</host>");
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
                xml.append("good");
                xml.append("</value>");
            }
            xml.append("</property>");
        }

        {// cdata property
            xml.append("<property key=\"cdata_key\">");
            {
                xml.append("<value env=\"0\">");
                xml.append("<![CDATA[<custom><xml/></custom>]]>");
                xml.append("</value>");
            }
            xml.append("</property>");
        }

        xml.append("</configuration>");

        return xml.toString();
    }
}
