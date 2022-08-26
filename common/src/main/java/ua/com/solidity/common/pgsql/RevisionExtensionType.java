package ua.com.solidity.common.pgsql;

public enum RevisionExtensionType {
    ROOT(0),
    CHILD_SET(1),
    CHILD_SET_WITH_EXTRA_ID(3);

    final int flags;

    private static final int CHILD_FLAG = 1;
    private static final int EXTRA_ID_FLAG = 2;

    private static final String CHILD = "CHILD";
    private static final String EXTRA_ID = "EXTRA";

    RevisionExtensionType(int flags) {
        this.flags = flags;
    }

    public static RevisionExtensionType parse(String name) {
        if (name != null && !name.isBlank()) {
            int flags = 0;
            String[] items = name.trim().toUpperCase().split("\\s*,\\s*");
            for (var item : items) {
                switch(item) {
                    case CHILD:
                        flags |= CHILD_FLAG;
                        break;

                    case EXTRA_ID:
                        flags |= CHILD_FLAG | EXTRA_ID_FLAG;
                        break;
                    default:
                }
            }

            switch (flags) {
                case 1:
                    return RevisionExtensionType.CHILD_SET;
                case 3:
                    return RevisionExtensionType.CHILD_SET_WITH_EXTRA_ID;
                default:
                    return RevisionExtensionType.ROOT;
            }
        }

        return RevisionExtensionType.ROOT;
    }

    public final boolean isRoot() {
        return !isChild();
    }

    public final boolean isChild() {
        return (flags & CHILD_FLAG) > 0;
    }

}
