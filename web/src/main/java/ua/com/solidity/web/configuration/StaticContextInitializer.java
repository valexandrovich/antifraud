package ua.com.solidity.web.configuration;


import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ua.com.solidity.web.utils.MapperUtil;

@Component
@RequiredArgsConstructor
public class StaticContextInitializer {

    private final ModelMapper modelMapper;

    @PostConstruct
    public void init() {
        MapperUtil.setModelMapper(modelMapper);
    }
}
