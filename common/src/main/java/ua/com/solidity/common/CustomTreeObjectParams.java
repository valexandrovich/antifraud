package ua.com.solidity.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomTreeObjectParams {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<String> path;
    private int equalsCount = 0;
    private int index = -1;

    public final boolean push(String name) {
        ++index;
        if (path == null || index >= path.size()) return false;
        if (name.equals(path.get(index))) {
            ++equalsCount;
        }
        return equalsCount == path.size();
    }

    public final void pop() {
        --index;
        if (index < equalsCount) --equalsCount;
    }
}