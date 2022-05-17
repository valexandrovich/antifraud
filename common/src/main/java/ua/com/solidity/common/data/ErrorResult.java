package ua.com.solidity.common.data;

import lombok.Getter;
import ua.com.solidity.common.ErrorReport;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorResult  {
    private boolean errorState = false;
    private final List<ErrorReport> report = new ArrayList<>();

    public final void add(DataObject obj, String clarification) {
        report.add(ErrorReport.create(obj, clarification));
        errorState = true;
    }

    public final void error() {
        errorState = true;
    }

    public final void clear() {
        report.clear();
        errorState = false;
    }
}