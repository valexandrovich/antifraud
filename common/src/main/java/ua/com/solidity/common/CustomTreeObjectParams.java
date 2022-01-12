package ua.com.solidity.common;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomTreeObjectParams {
    private List<String> path;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int equalsCount = 0;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int index = 0;

    public final boolean isReady() {
        return equalsCount == index && equalsCount == (path == null ? 0 : path.size());
    }
    public final void push(String name) {
        ++index;
        if (path == null || index > path.size()) return;
        if (name.equals(path.get(index - 1))) {
            ++equalsCount;
        }
    }

    public final void pop() {
        --index;
        if (index < equalsCount) --equalsCount;
    }
}