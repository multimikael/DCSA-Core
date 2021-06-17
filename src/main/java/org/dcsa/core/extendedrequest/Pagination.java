package org.dcsa.core.extendedrequest;

import org.dcsa.core.exception.GetException;
import org.dcsa.core.model.OrderBy;
import org.dcsa.core.query.DBEntityAnalysis;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.sql.*;

import java.sql.SQLData;

/**
 * A class to help managing pagination parameters and limiting the sql result.
 * It parses pagination parameters (limit and internal cursorIndex).
 * It encodes the pagination parameters to be used in pagination links (cursor link).
 * It creates the SQL (LIMIT and OFFSET clause) used for database requests.
 * NB: It should be noted that OFFSET is used for offset based bagination - KeySet based pagination
 * will be implemented at a later stage
 * @param <T> the type of the class modeled by this {@code Class}
 */
public class Pagination<T> {
    public enum PageRequest {
        CURRENT,
        NEXT,
        PREVIOUS,
        FIRST,
        LAST
    }

    private static final String INDEX_CURSOR_SPLIT = "=";
    private static final String LIMIT_SPLIT = "=";

    private final ExtendedRequest<T> extendedRequest;
    private final ExtendedParameters extendedParameters;
    private final DBEntityAnalysis<T> dbEntityAnalysis;

    private Integer limit;
    private String cursorValue;
    private boolean isNextCursor;
    private int total;

    public Pagination(ExtendedRequest<T> extendedRequest, ExtendedParameters extendedParameters, DBEntityAnalysis<T> dbEntityAnalysis) {
        this.extendedRequest = extendedRequest;
        this.extendedParameters = extendedParameters;
        this.dbEntityAnalysis = dbEntityAnalysis;
        System.out.println("oh");
        limit = extendedParameters.getDefaultPageSize();
    }

    protected void parseInternalPrevCursor(String cursorValue) {
        this.cursorValue = cursorValue;
        isNextCursor = false;
    }

    protected void parseInternalNextCursor(String cursorValue) {
        this.cursorValue = cursorValue;
        isNextCursor = true;
    }

    protected void parseLimitParameter(String value, boolean fromCursor) {
        if (extendedRequest.isCursor()) {
            throw new GetException("Cannot use the Limit parameter while accessing a paginated result using the " + extendedParameters.getPaginationCursorName() + " parameter!");
        }

        try {
            limit = Integer.parseInt(value);
            if (!fromCursor) {
                extendedRequest.setNoCursor();
            }
        } catch (NumberFormatException numberFormatException) {
            throw new GetException("Unknown " + extendedParameters.getPaginationPageSizeName() + " value:" + value + ". Must be a Number!");
        }
    }

    protected void getLimitQueryString(StringBuilder sb) {
        if (limit != null) {
            sb.append(" LIMIT ").append(limit);
        }
    }

    protected <SB extends SelectBuilder.SelectWhere & SelectBuilder.SelectLimitOffset & SelectBuilder.SelectOrdered> SB applyPaginationQuery(SB t) {
        System.out.println(limit);
        Column orderByColumn = null;
        for (QueryField queryField : dbEntityAnalysis.getAllSelectableFields()) {
            if (queryField.getCombinedModelField().isAnnotationPresent(OrderBy.class)) {
                orderByColumn = queryField.getSelectColumn();
            }
        }
        if (orderByColumn == null) {
            throw new IllegalArgumentException("No @OrderBy");
        }
        if (limit != null) {
            t = (SB) t.limit(limit);
        }
        if (cursorValue != null) {
            if (isNextCursor) {
                t = (SB) t.where(Conditions.isGreater(SQL.literalOf(orderByColumn.getReferenceName()), SQL.literalOf(cursorValue)))
                        .orderBy(OrderByField.from(orderByColumn, Sort.Direction.ASC));
            } else {
                // SelectBuilder s = Select.builder().select(Expressions.asterisk()).from(t)
                t = (SB) t.where(Conditions.isLess(SQL.literalOf(orderByColumn.getReferenceName()), SQL.literalOf(cursorValue)))
                        .orderBy(OrderByField.from(orderByColumn, Sort.Direction.DESC));
                t = Select.builder().select(Expressions.asterisk()).from(t.build())
                        .orderBy(OrderByField.from(orderByColumn, Sort.Direction.ASC));
            }
        }
        return t;
    }

    protected boolean encodePagination(StringBuilder sb, PageRequest page) {
//        switch (page) {
//            case CURRENT: encodeIndexCursor(sb, indexCursor); return true;
//            case NEXT: return encodeNext(sb, indexCursor + limit);
//            case PREVIOUS: return encodePrevious(sb, indexCursor - limit);
//            case FIRST: return encodeFirst(sb);
//            case LAST: return encodeLast(sb, total - limit);
//            default: return false;
//        }
        return true;
    }

    private boolean encodeNext(StringBuilder sb, int nextIndex) {
        return true;
    }

    private boolean encodePrevious(StringBuilder sb, int previousIndex) {
        return true;
    }

    private boolean encodeFirst(StringBuilder sb) {
        return true;
    }

    private boolean encodeLast(StringBuilder sb, int lastIndex) {
        return true;
    }

    protected void encodeLimit(StringBuilder sb) {
        if (limit != null) {
            if (sb.length() != 0) {
                sb.append(ExtendedRequest.PARAMETER_SPLIT);
            }
            sb.append(extendedParameters.getPaginationPageSizeName()).append(LIMIT_SPLIT).append(limit);
        }
    }

    public Integer getLimit() {
        return limit;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }
}
