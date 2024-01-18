package ua.com.valexa.downloaderismc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ua.com.valexa.common.dto.StepRequestDto;
import ua.com.valexa.downloaderismc.service.govua.GovuaDownloader;

@SpringBootTest
class DownloaderIsmcApplicationTests {


    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GovuaDownloader govuaDownloader;

    @Test
    void contextLoads() throws JsonProcessingException {



    }

//    @Test
//    void contextLoads() {
//        try {
//            String json = "{\"id\":123}";
//            StepRequestDto dto = objectMapper.readValue(json, StepRequestDto.class);
//            System.out.println("Deserialized DTO: " + dto.getId());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
