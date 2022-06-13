package ua.com.solidity.web.service.validator;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.db.repositories.TagTypeRepository;
import ua.com.solidity.web.response.secondary.ManualTagStatus;

@Component
@RequiredArgsConstructor
public class ManualTagValidator {

    private final TagTypeRepository tagTypeRepository;
    public List<ManualTagStatus> validate(List<ManualTag> tags) {
        List<ManualTagStatus> statusList = new ArrayList<>();

        for (ManualTag tag : tags) {
            if (tagTypeRepository.findByCode(tag.getMkId()).isEmpty()
            && !StringUtils.isBlank(tag.getMkId()))
                statusList.add(new ManualTagStatus(tag.getId(), 0, "невідома мітка"));
            if (StringUtils.isBlank(tag.getMkId()))
                statusList.add(new ManualTagStatus(tag.getId(), 0, "мітка не має бути пустою"));
            if (!valid(tag.getMkEventDate(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),1, ManualDataRegex.DATE.getMessage()));
            if (!valid(tag.getMkStart(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),2, ManualDataRegex.DATE.getMessage()));
            if (!valid(tag.getMkExpire(), ManualDataRegex.DATE.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),3, ManualDataRegex.DATE.getMessage()));
            if (!valid(tag.getMkNumberValue(), ManualDataRegex.NUMBER.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),4, ManualDataRegex.NUMBER.getMessage()));
        }
        return statusList;
    }

    private static boolean valid(String value, String regex) {
        if (value == null || regex == null) return true;
        return value.matches(regex);
    }
}
