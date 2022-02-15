package ua.com.solidity.common;

public class FloatObjectListItem {
    FloatObjectList list;
    FloatObjectListItem prior;
    FloatObjectListItem next;
    Object data;

    public FloatObjectListItem(Object data) {
        list = null;
        prior = next = null;
        this.data = data;
    }

    void detach() {
        if (list != null) {
            next.prior = prior;
            prior.next = next;
            if (list.root == this) {
                list.root = next != this ? next : null;
            }
        }
        next = prior = null; // for GC
        list = null;
    }

    private boolean rootCheck(FloatObjectList list) {
        if (list.root == null) {
            list.root = next = prior = this;
            this.list = list;
            return false;
        }
        return true;
    }

    void insertLast(FloatObjectList list) {
        if (list == null) return;
        if (rootCheck(list)) {
            insertAfter(list.root.prior);
        }
    }

    void insertFirst(FloatObjectList list) {
        if (list == null) return;
        if (rootCheck(list)) {
            insertBefore(list.root);
        }
    }

    void insertBefore(FloatObjectListItem item) {
        if (item == null || item.list == null || (list == item.list && item.prior == this)) return;
        detach();
        list = item.list;
        prior = item.prior;
        next = item;
        prior.next = item.prior = this;
        if (list.root == item) {
            list.root = this;
        }
    }

    void insertAfter(FloatObjectListItem item) {
        if (item == null || item.list == null || (list == item.list && item.next == this)) return;
        detach();
        list = item.list;
        next = item.next;
        prior = item;
        next.prior = item.next = this;
    }

    public void floatUp() {
        if (list == null || list.root == this) return;
        FloatObjectList parent = list;
        detach();
        insertFirst(parent);
    }

    public final FloatObjectList list() {
        return this.list;
    }

    public final FloatObjectListItem prior() {
        return prior;
    }

    public final FloatObjectListItem next() {
        return next;
    }

    public final <T> T get(Class<T> clazz) {
        return clazz.cast(data);
    }

    public final boolean isFirst() {
        return list == null || list.root == this;
    }

    public final boolean isLast() {
        return list == null || next == list.root;
    }

    public final FloatObjectListItem getPrior() {
        return prior;
    }

    public final FloatObjectListItem getNext() {
        return next;
    }
}
