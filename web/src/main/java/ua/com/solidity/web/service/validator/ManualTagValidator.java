package ua.com.solidity.web.service.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import ua.com.solidity.db.entities.ManualTag;
import ua.com.solidity.web.response.secondary.ManualTagStatus;
import ua.com.solidity.web.service.dynamicfile.Tag;

public class ManualTagValidator {

    public static List<ManualTagStatus> manualTagValidator(List<ManualTag> tags) {
        List<ManualTagStatus> statusList = new ArrayList<>();

        for (ManualTag tag : tags) {
            if (!Arrays.stream(Tag.values()).map(Tag::name).collect(Collectors.toList()).contains(tag.getMkId())
            && !StringUtils.isBlank(tag.getMkId()))
                statusList.add(new ManualTagStatus(tag.getId(), 0, "невідома мітка"));
            if (StringUtils.isBlank(tag.getMkId()))
                statusList.add(new ManualTagStatus(tag.getId(), 0, "мітка не має бути пустою"));
            if (!valid(tag.getMkEventDate(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),1, PhysicalPersonRegex.DATE.getMessage()));
            if (!valid(tag.getMkStart(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),2, PhysicalPersonRegex.DATE.getMessage()));
            if (!valid(tag.getMkExpire(), PhysicalPersonRegex.DATE.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),3, PhysicalPersonRegex.DATE.getMessage()));
            if (!valid(tag.getMkNumberValue(), PhysicalPersonRegex.NUMBER.getRegex()))
                statusList.add(new ManualTagStatus(tag.getId(),4, PhysicalPersonRegex.NUMBER.getMessage()));
        }
        return statusList;
    }

    private static boolean valid(String value, String regex) {
        if (value == null || regex == null) return true;
        return value.matches(regex);
    }
}
