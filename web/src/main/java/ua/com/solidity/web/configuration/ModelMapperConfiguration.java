package ua.com.solidity.web.configuration;

import lombok.RequiredArgsConstructor;
import org.modelmapper.Condition;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.com.solidity.db.entities.TagType;
import ua.com.solidity.db.entities.YAddress;
import ua.com.solidity.db.entities.YAltPerson;
import ua.com.solidity.db.entities.YEmail;
import ua.com.solidity.db.entities.YINN;
import ua.com.solidity.db.entities.YPassport;
import ua.com.solidity.db.entities.YPhone;
import ua.com.solidity.db.entities.YTag;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.web.dto.olap.YAddressDto;
import ua.com.solidity.web.dto.olap.YAltPersonDto;
import ua.com.solidity.web.dto.olap.YEmailDto;
import ua.com.solidity.web.dto.olap.YINNDto;
import ua.com.solidity.web.dto.olap.YPassportDto;
import ua.com.solidity.web.dto.olap.YPhoneDto;
import ua.com.solidity.web.dto.olap.YTagDto;

@Configuration
@RequiredArgsConstructor
public class ModelMapperConfiguration {

    private final TagTypeRepository tagTypeRepository;

    private final Converter<String, String> toUpperCase =
            context -> context.getSource() == null ? null : context.getSource().toUpperCase();
    private final Condition<YTagDto, YTag> tagTypeIdNotNull = context -> {
        TagType tagType = context.getSource().getTagType();
        return tagType != null && tagType.getId() != null && tagType.getId() > 0;
    };


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.createTypeMap(YPassportDto.class, YPassport.class, TypeMapName.YPASSPORT_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YPassport::setId))
                .addMappings(mapper -> mapper.using(toUpperCase).map(YPassportDto::getSeries, YPassport::setSeries))
                .addMappings(mapper -> mapper.using(toUpperCase).map(YPassportDto::getAuthority, YPassport::setAuthority))
                .addMappings(mapper -> mapper.using(toUpperCase).map(YPassportDto::getType, YPassport::setType))
                .addMappings(mapper -> mapper.skip(YPassport::setImportSources));

        modelMapper.createTypeMap(YINNDto.class, YINN.class, TypeMapName.YINN_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YINN::setId))
                .addMappings(mapper -> mapper.skip(YINN::setImportSources));

        modelMapper.createTypeMap(YAddressDto.class, YAddress.class, TypeMapName.YADDRESS_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YAddress::setId))
                .addMappings(mapper -> mapper.skip(YAddress::setImportSources));

        modelMapper.createTypeMap(YAltPersonDto.class, YAltPerson.class, TypeMapName.YALT_PERSON_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YAltPerson::setId))
                .addMappings(mapper -> mapper.skip(YAltPerson::setImportSources));

        modelMapper.createTypeMap(YEmailDto.class, YEmail.class, TypeMapName.YEMAIL_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YEmail::setId))
                .addMappings(mapper -> mapper.skip(YEmail::setImportSources));

        modelMapper.createTypeMap(YPhoneDto.class, YPhone.class, TypeMapName.YPHONE_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YPhone::setId))
                .addMappings(mapper -> mapper.skip(YPhone::setImportSources));

        modelMapper.createTypeMap(YTagDto.class, YTag.class, TypeMapName.YTAG_TO_UPPER_CASE)
                .addMappings(mapper -> mapper.skip(YTag::setId))
//                .addMappings(
//                        mapper -> mapper.when(tagTypeIdNotNull)
//                        .map(src -> {
//                                 Long id = src.getTagType().getId();
//                                 return tagTypeRepository.findById(id)
//                                         .orElseThrow(() -> new EntityNotFoundException(TagType.class, id));
//                             },
//                             YTag::setTagType))
                .addMappings(mapper -> mapper.skip(YTag::setImportSources));

        return modelMapper;
    }
}
