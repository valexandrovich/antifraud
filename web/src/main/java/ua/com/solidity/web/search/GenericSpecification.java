package ua.com.solidity.web.search;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
public class GenericSpecification<T> implements Specification<T> {
    private static final long serialVersionUID = 1974060241950251807L;

    private transient List<SearchCriteria> list;

    public GenericSpecification() {
        list = new ArrayList<>();
    }

    @Override
    public Predicate toPredicate(@NotNull Root<T> root, @NotNull CriteriaQuery<?> query, @NotNull CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        query.distinct(true);

        for (SearchCriteria criteria : list) {
            Path<T> path = root;
            if (!StringUtils.isBlank(criteria.getJoinedTable()))
                path = root.join(criteria.getJoinedTable(), JoinType.INNER);

            switch (criteria.getOperation()) {
                case GREATER_THAN:
                    predicates.add(builder.greaterThan(path.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case EQUALS:
                    predicates.add(builder.equal(path.get(criteria.getKey()), criteria.getValue()));
                    break;
                case LESS_THAN:
                    predicates.add(builder.lessThan(path.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case GREATER_THAN_EQUAL:
                    predicates.add(builder.greaterThanOrEqualTo(path.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case LESS_THAN_EQUAL:
                    predicates.add(builder.lessThanOrEqualTo(path.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case NOT_EQUAL:
                    predicates.add(builder.notEqual(path.get(criteria.getKey()), criteria.getValue()));
                    break;
                case MATCH:
                    predicates.add(builder.like(path.get(criteria.getKey()),
                            "%" + criteria.getValue().toString() + "%"));
                    break;
                case LIKE:
                    predicates.add(builder.like(path.get(criteria.getKey()),
                            criteria.getValue().toString() + "%"));
                    break;
                case BETWEEN:
                    predicates.add(builder.between(path.get(criteria.getKey()),
                            LocalDate.parse(criteria.getValue().toString()).minusYears(1).plusDays(1),
                            LocalDate.parse(criteria.getValue().toString())));
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + criteria.getOperation());
            }
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

    public void add(SearchCriteria criteria) {
        list.add(criteria);
    }

    public void clear() {
        list.clear();
    }
}
