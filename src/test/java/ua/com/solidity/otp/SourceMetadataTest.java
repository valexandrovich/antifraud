package ua.com.solidity.otp;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ua.com.solidity.otp.model.DataSource;
import ua.com.solidity.otp.model.DataSourceColumnMapping;
import ua.com.solidity.otp.model.DataSourceLogItem;
import ua.com.solidity.otp.service.DataSourceService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SourceMetadataTest {

    @Autowired
    public DataSourceService dataSourceService;

    @Test
    public void testDataSourceService() {
        DataSourceColumnMapping dsc1 = new DataSourceColumnMapping(1, "source_column1", "target_column1", DataSourceColumnMapping.DataType.TEXT, DataSourceColumnMapping.DataType.TEXT);
        DataSourceColumnMapping dsc2 = new DataSourceColumnMapping(2, "source_column2", "target_column2", DataSourceColumnMapping.DataType.INTEGER, DataSourceColumnMapping.DataType.INTEGER);
        DataSource ds = new DataSource();
        DataSourceLogItem dsli = new DataSourceLogItem(null, ds, LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS), true, Arrays.asList(dsc1, dsc2));
        ds.setColumnMapping(Arrays.asList(dsc1, dsc2));
        ds.setDataSourceLogItems(Arrays.asList(dsli));
        ds.setLink("http://www.google.com");
        ds.setName("Test Source");
        ds.setDescription("This is test source for JUnit");
        log.info("Saving DataSource ... ");
        DataSource dsDb = dataSourceService.save(ds);
        assertEquals(ds, dsDb);
        log.info("Finding DataSource ... ");
        DataSource dsFound = dataSourceService.findByUuid(ds.getUuid());
        String dsStr = ds.toString();
        String dsFoundStr = dsFound.toString();
        assertEquals(ds, dsDb);
        assertEquals(dsFound, dsDb);
        assertEquals(dsFound, ds);
    }

}
