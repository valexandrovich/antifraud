package ua.com.solidity.web.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListWrapper<T> {

    private List<T> list;
}
