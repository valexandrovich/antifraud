package ua.com.solidity.common.data;

import lombok.Getter;
import ua.com.solidity.common.ErrorReport;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorResult  {
    private boolean errorState = false;
    private final List<ErrorReport> report = new ArrayList<>();

    public final void add(DataObject obj, String source, String clarification) {
        report.add(ErrorReport.create(obj, source, clarification));
        errorState = true;
    }

    public final void add(String source, String info, String clarification) {
        report.add(ErrorReport.create(source, info, clarification));
        errorState = true;
    }

    public final void error() {
        errorState = true;
    }

    public final void clear() {
        report.clear();
        errorState = false;
    }

    public final int getErrorCount() {
        return report.size();
    }
}