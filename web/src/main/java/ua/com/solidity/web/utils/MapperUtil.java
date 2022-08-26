package ua.com.solidity.web.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;

public final class MapperUtil {
    private MapperUtil() {
    }

    private static ModelMapper modelMapper;

    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source
                .stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }

    public static void setModelMapper(ModelMapper modelMapper) {
        MapperUtil.modelMapper = modelMapper;
    }
}
