package ua.com.solidity.common.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayItemObject extends DataObject {
    private static final String ITEM_OBJECT_FIELD_NAME = "@item";
    private final DataField reference;
    private final boolean isObject;

    public ArrayItemObject(DataField field) {
        super(field.parent, field.parent.getLocation());
        this.reference = field;
        isObject = DataField.isObject(field);
    }

    @Override
    public DataField getField(String fieldName) {
        if (isObject) {
            return DataField.getObject(reference).getField(fieldName);
        } else if (fieldName.equals(ITEM_OBJECT_FIELD_NAME)) {
            return reference;
        }
        return null;
    }

    @Override
    public Iterable<FieldEntry> getFields() {
        if (isObject) {
            return DataField.getObject(reference).getFields();
        } else {
            return () -> new Iterator<>() {
                boolean nextInvoked = false;
                @Override
                public boolean hasNext() {
                    return !nextInvoked;
                }

                @Override
                public FieldEntry next() throws NoSuchElementException {
                    if (nextInvoked) throw new NoSuchElementException();
                    nextInvoked = true;
                    return new FieldEntry(ITEM_OBJECT_FIELD_NAME, reference);
                }
            };
        }
    }
}
