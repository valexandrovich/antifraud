package ua.com.solidity.common;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FloatObjectListTest {
    private String concatList(FloatObjectList list) {
        FloatObjectListItem item = list.root;
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(item.get(String.class));
            item = item.next;
        } while (item != list.root);
        return builder.toString();
    }

    @Test
    void floatListTest() {
        FloatObjectList list = new FloatObjectList();
        list.add("Hello");
        list.add(",");
        list.add("World!");
        assertThat(concatList(list)).isEqualTo("Hello,World!");
    }

    @Test
    void floatListTest2() {
        FloatObjectList list = new FloatObjectList();
        list.add(",");
        list.addFirst("Hello");
        list.addLast("World!");
        assertThat(concatList(list)).isEqualTo("Hello,World!");
    }
}
