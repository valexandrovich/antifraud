package ua.com.solidity.common.pgsql;

public enum RevisionExtensionType {
    ROOT,
    CHILD_SET,
    CHILD_SET_WITH_EXTRA_ID;

    private static final String CHILD = "child";
    private static final String CHILD_EXTRA = "child_extra_id";

    public static RevisionExtensionType parse(String name) {
        switch (name) {
            case CHILD:
                return RevisionExtensionType.CHILD_SET;
            case CHILD_EXTRA:
                return RevisionExtensionType.CHILD_SET_WITH_EXTRA_ID;
            default:
                break;
        }
        return RevisionExtensionType.ROOT;
    }
}
