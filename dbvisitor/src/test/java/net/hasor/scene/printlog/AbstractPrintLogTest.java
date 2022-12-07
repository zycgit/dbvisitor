package net.hasor.scene.printlog;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerFactory.class })
@PowerMockIgnore({ "javax.script.*", "javax.management.*", "org.w3c.dom.*", "org.apache.log4j.*", "org.xml.sax.*", "javax.xml.*" })
public class AbstractPrintLogTest {
    protected List<Object> printLog = new ArrayList<>();

    @Before
    public void setupLogger() {
        Logger logger = PowerMockito.mock(Logger.class);
        PowerMockito.when(logger.isDebugEnabled()).thenReturn(true);
        PowerMockito.doAnswer(i -> printLog.add("DEBUG:" + i.getArguments()[0])).when(logger).debug(Mockito.anyString());
        PowerMockito.doAnswer(i -> printLog.add("INFO:" + i.getArguments()[0])).when(logger).info(Mockito.anyString());
        PowerMockito.doAnswer(i -> printLog.add("TRACE:" + i.getArguments()[0])).when(logger).trace(Mockito.anyString());
        PowerMockito.doAnswer(i -> printLog.add("WARN:" + i.getArguments()[0])).when(logger).warn(Mockito.anyString());
        PowerMockito.doAnswer(i -> printLog.add("ERROR:" + i.getArguments()[0])).when(logger).error(Mockito.anyString());

        PowerMockito.mockStatic(LoggerFactory.class);
        PowerMockito.when(LoggerFactory.getLogger(Mockito.anyString())).thenAnswer(inv -> logger);
        PowerMockito.when(LoggerFactory.getLogger((Class) Mockito.any())).thenAnswer(inv -> logger);
    }
}
