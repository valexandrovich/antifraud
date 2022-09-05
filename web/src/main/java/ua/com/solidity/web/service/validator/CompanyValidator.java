package ua.com.solidity.web.service.validator;

import static ua.com.solidity.util.validator.Validator.isValidEdrpou;
import static ua.com.solidity.util.validator.Validator.isValidInn;
import static ua.com.solidity.util.validator.Validator.isValidPdv;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ua.com.solidity.common.UtilString;
import ua.com.solidity.db.entities.ManualCompany;
import ua.com.solidity.db.repositories.YCompanyRoleRepository;
import ua.com.solidity.db.repositories.YCompanyStateRepository;
import ua.com.solidity.web.response.secondary.ManualCompanyStatus;

@Component
@RequiredArgsConstructor
public class CompanyValidator {
    private final YCompanyRoleRepository companyRoleRepository;
    private final YCompanyStateRepository companyStateRepository;

    private static final String MESSAGE_LONG_VALUE = "Довжина поля перевищує 255 символів";
    private static final String MESSAGE_EMPTY_DATA = "Одне з полів має бути заповненим: ЄДРПОУ, ПДВ";
    private static final String MESSAGE_RELATION_PERSON = "Обидва поля мають бути заповненими: ІНН та тип зв'язку";
    private static final String MESSAGE_RELATION_COMPANY = "Обидва поля мають бути заповненими: ЄДРПОУ та тип зв'язку";

    public List<ManualCompanyStatus> manualCompanyValidate(List<ManualCompany> companies) {
        List<ManualCompanyStatus> statusList = new ArrayList<>();

        for (ManualCompany company : companies) {
            if (!valid(company.getCnum(), DataRegex.CNUM.getRegex()))
                statusList.add(new ManualCompanyStatus(company.getId(), 0, DataRegex.CNUM.getMessage()));

            if (StringUtils.isNotBlank(company.getName()) && company.getName().length() == 255
                    && company.getName().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 1, MESSAGE_LONG_VALUE));

            if (StringUtils.isNotBlank(company.getNameEn()) && company.getNameEn().length() == 255
                    && company.getNameEn().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 2, MESSAGE_LONG_VALUE));

            if (StringUtils.isNotBlank(company.getShortName()) && company.getShortName().length() == 255
                    && company.getShortName().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 3, MESSAGE_LONG_VALUE));

            if (!valid(company.getEdrpou(), DataRegex.EDRPOU.getRegex()) || (StringUtils.isNotBlank(company.getEdrpou()) && !isValidEdrpou(company.getEdrpou())))
                statusList.add(new ManualCompanyStatus(company.getId(), 4, DataRegex.EDRPOU.getMessage()));

            if (!valid(company.getPdv(), DataRegex.PDV.getRegex()) || (StringUtils.isNotBlank(company.getPdv()) && !isValidPdv(company.getPdv())))
                statusList.add(new ManualCompanyStatus(company.getId(), 5, DataRegex.PDV.getMessage()));

            if (StringUtils.isNotBlank(company.getAddress()) && company.getAddress().length() == 255
                    && company.getAddress().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 6, MESSAGE_LONG_VALUE));

            if (StringUtils.isNotBlank(company.getState()) && company.getState().length() == 255
                    && company.getState().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 7, MESSAGE_LONG_VALUE));
            if (companyStateRepository.findByState(UtilString.toUpperCase(company.getState())).isEmpty() &&
                    StringUtils.isNotBlank(company.getState()))
                statusList.add(new ManualCompanyStatus(company.getId(), 7, "Невідомий тип стану компанії"));

            if (StringUtils.isNotBlank(company.getLname()) && company.getLname().length() == 255
                    && company.getLname().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 8, MESSAGE_LONG_VALUE));
            if (!valid(company.getLname(), DataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualCompanyStatus(company.getId(), 8, DataRegex.NAME_UK.getMessage()));

            if (StringUtils.isNotBlank(company.getFname()) && company.getFname().length() == 255
                    && company.getFname().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 9, MESSAGE_LONG_VALUE));
            if (!valid(company.getFname(), DataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualCompanyStatus(company.getId(), 9, DataRegex.NAME_UK.getMessage()));

            if (StringUtils.isNotBlank(company.getPname()) && company.getPname().length() == 255
                    && company.getPname().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 10, MESSAGE_LONG_VALUE));
            if (!valid(company.getPname(), DataRegex.NAME_UK.getRegex()))
                statusList.add(new ManualCompanyStatus(company.getId(), 10, DataRegex.NAME_UK.getMessage()));

            if (!valid(company.getInn(), DataRegex.INN.getRegex()) || (StringUtils.isNotBlank(company.getInn()) && !isValidInn(company.getInn(), null, null)))
                statusList.add(new ManualCompanyStatus(company.getId(), 11, DataRegex.INN.getMessage()));

            if (StringUtils.isNotBlank(company.getTypeRelationPerson()) && company.getTypeRelationPerson().length() == 255
                    && company.getTypeRelationPerson().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 12, MESSAGE_LONG_VALUE));
            if (companyRoleRepository.findByRole(UtilString.toUpperCase(company.getTypeRelationPerson())).isEmpty()
                    && StringUtils.isNotBlank(company.getTypeRelationPerson()))
                statusList.add(new ManualCompanyStatus(company.getId(), 12, "Невідомий тип зв'язку"));

            if (StringUtils.isNotBlank(company.getCname()) && company.getCname().length() == 255
                    && company.getCname().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 13, MESSAGE_LONG_VALUE));

            if (!valid(company.getEdrpouRelationCompany(), DataRegex.EDRPOU.getRegex()) || (StringUtils.isNotBlank(company.getEdrpouRelationCompany())
                    && !isValidEdrpou(company.getEdrpouRelationCompany())))
                statusList.add(new ManualCompanyStatus(company.getId(), 14, DataRegex.EDRPOU.getMessage()));

            if (StringUtils.isNotBlank(company.getTypeRelationCompany()) && company.getTypeRelationCompany().length() == 255
                    && company.getTypeRelationCompany().contains("..."))
                statusList.add(new ManualCompanyStatus(company.getId(), 15, MESSAGE_LONG_VALUE));
            if (companyRoleRepository.findByRole(UtilString.toUpperCase(company.getTypeRelationCompany())).isEmpty()
                    && StringUtils.isNotBlank(company.getTypeRelationCompany()))
                statusList.add(new ManualCompanyStatus(company.getId(), 15, "Невідомий тип зв'язку"));

            if (StringUtils.isBlank(company.getEdrpou())
                    && StringUtils.isBlank(company.getPdv())) {
                statusList.add(new ManualCompanyStatus(company.getId(), 4, MESSAGE_EMPTY_DATA));
                statusList.add(new ManualCompanyStatus(company.getId(), 5, MESSAGE_EMPTY_DATA));
            }

            if ((StringUtils.isNotBlank(company.getInn()) && StringUtils.isBlank(company.getTypeRelationPerson()))
                    || (StringUtils.isNotBlank(company.getTypeRelationPerson()) && StringUtils.isBlank(company.getInn()))) {
                statusList.add(new ManualCompanyStatus(company.getId(), 11, MESSAGE_RELATION_PERSON));
                statusList.add(new ManualCompanyStatus(company.getId(), 12, MESSAGE_RELATION_PERSON));
            }

            if ((StringUtils.isNotBlank(company.getEdrpouRelationCompany()) && StringUtils.isBlank(company.getTypeRelationCompany()))
                    || (StringUtils.isNotBlank(company.getTypeRelationCompany()) && StringUtils.isBlank(company.getEdrpouRelationCompany()))) {
                statusList.add(new ManualCompanyStatus(company.getId(), 14, MESSAGE_RELATION_COMPANY));
                statusList.add(new ManualCompanyStatus(company.getId(), 15, MESSAGE_RELATION_COMPANY));
            }
        }
        return statusList;
    }

    private static boolean valid(String value, String regex) {
        if (value == null || regex == null) return true;
        return value.matches(regex);
    }
}
