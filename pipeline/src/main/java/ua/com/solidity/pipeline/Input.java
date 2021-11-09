package ua.com.solidity.pipeline;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Input {
    final Item item;
    final Class<?> inputClass;
    final String inputItemName;
    Item inputItem = null;
    final String nameOfSet;
    int index;

    protected Input(Item item, String nameOfSet, String inputItemName, Class<?> inputClass) {
        this.item = item;
        this.inputClass = inputClass;
        this.inputItemName = inputItemName;
        this.nameOfSet = nameOfSet;
        this.index = -1;
    }

    final boolean joinedItemNeeded() {
        if (inputItem == null) {
            if (item == null || item.pipeline == null) return false;
            inputItem = item.pipeline.itemByName.getOrDefault(inputItemName, null);
        }
        return inputItem != null && inputClass.isAssignableFrom(inputItem.getOutputClass());
    }

    final boolean prepare() {
        if (!joinedItemNeeded()) {
            if (inputItem == null)
                log.warn("Pipeline illegal item reference from item({}) to ({})", item.name, inputItemName);
            else if (inputItem == item) {
                log.warn("Pipeline can't join item input to itself item({}) to ({})", item.name, inputItemName);
            } else {
                log.warn("Pipeline incompatible types in reference from item({}) to ({})", item.name, inputItemName);
            }
            return false;
        }
        return true;
    }

    public final <T> T getValue(Class<T> clazz) {
        return inputItem != null ? inputItem.getValue(clazz) : null;
    }

    @SuppressWarnings("unused")
    public final boolean isSimple() {
        return !isIterator();
    }

    public final boolean isIterator() {
        return inputItem != null && (inputItem.flags & Item.FLAG_ITERATOR) == Item.FLAG_ITERATOR;
    }

    @SuppressWarnings("unused")
    public final boolean isError() {
        return inputItem != null && (inputItem.flags & Item.FLAG_ERROR) == Item.FLAG_ERROR;
    }

    public final boolean isBOF() {
        return isIterator() && (inputItem.flags & Item.FLAG_BOF) == Item.FLAG_BOF;
    }

    public final boolean isEOF() {
        return isIterator() && (inputItem.flags & Item.FLAG_EOF) == Item.FLAG_EOF;
    }

    public final boolean isCompleted() {
        return inputItem != null && inputItem.completed;
    }
}
