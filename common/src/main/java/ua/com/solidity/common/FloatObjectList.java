package ua.com.solidity.common;

public class FloatObjectList {
    FloatObjectListItem root = null;

    public FloatObjectList() {
        // nothing
    }

    public FloatObjectList(Object...args) {
        for (Object obj : args) {
            add(obj);
        }
    }

    public final FloatObjectListItem add(Object obj) {
        FloatObjectListItem item = new FloatObjectListItem(obj);
        item.insertLast(this);
        return item;
    }

    public final FloatObjectListItem addFirst(Object obj) {
        FloatObjectListItem item = new FloatObjectListItem(obj);
        item.insertFirst(this);
        return item;
    }

    public final FloatObjectListItem addLast(Object obj) {
        return add(obj);
    }

    public final boolean isEmpty() {
        return root == null;
    }

    public final FloatObjectListItem getRoot() {
        return root;
    }
}
