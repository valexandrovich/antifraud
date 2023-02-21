package ua.com.solidity.report.service;

import ua.com.solidity.report.model.DocumentData;

import java.util.List;

public interface DocumentService {
    String createDocument(List<DocumentData> dataList);
}
