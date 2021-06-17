package org.dcsa.core;

import com.nimbusds.jose.util.Base64;
import lombok.RequiredArgsConstructor;
import org.dcsa.core.extendedrequest.ExtendedParameters;
import org.dcsa.core.extendedrequest.ExtendedRequest;
import org.dcsa.core.mock.MockR2dbcDialect;
import org.dcsa.core.models.combined.CustomerWithAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@SpringBootTest(properties = {
        "pagination.defaultPageSize=100",
})
@ContextConfiguration(classes = ExtendedParameters.class)
public class PaginationTest {

    private static final Pattern COLLAPSE_SPACE = Pattern.compile("\\s\\s++");
    private static final Pattern PRETTY_PRINT_SPLIT =
            Pattern.compile("\\s+(FROM|(?:LEFT|RIGHT)?\\s*(?:INNER|OUTER)?\\s*JOIN|WHERE|ORDER BY)\\s");

    @Autowired
    private ExtendedParameters extendedParameters;

    @Test
    public void testCustomerWithAddress() {
        String baseQuery = "SELECT customer_table.customer_id AS \"id\", customer_table.customer_name AS \"name\", address_table.street_name AS \"address\""
                + " FROM customer_table"
                + " JOIN address_table ON customer_table.address_id = address_table.address_id";
        request(CustomerWithAddress.class, extendedParameters)
                .withParam("cursor", Base64.encode("|prev|=2&limit=3").toString())
                .verify(baseQuery);
    }

    private static <T> PaginationTest.PaginationVerifier<T> request(Class<T> clazz, ExtendedParameters extendedParameters) {
        return new PaginationTest.PaginationVerifier<>(new ExtendedRequest<>(extendedParameters, new MockR2dbcDialect(), clazz));
    }

    @RequiredArgsConstructor
    private static class PaginationVerifier<T> {

        private final ExtendedRequest<T> request;

        private final LinkedHashMap<String, List<String>> params = new LinkedHashMap<>();

        public PaginationTest.PaginationVerifier<T> withParam(String param, String value) {
            this.params.computeIfAbsent(param, k -> new ArrayList<>()).add(value);
            return this;
        }

        public void verify(String query, Consumer<ExtendedRequest<T>> requestMutator) {
            String generated;
            if (params.isEmpty()) {
                request.resetParameters();
            } else {
                System.out.println(1);
                request.parseParameter(params);
            }
            if (requestMutator != null) {
                requestMutator.accept(request);
            }
            generated = request.getQuery().toQuery();
            Assertions.assertEquals(prettifyQuery(query), prettifyQuery(generated));
        }

        public void verify(String query) {
            this.verify(query, null);
        }

        // makes IntelliJ's "show differences" view more useful in case of a mismatch
        private static String prettifyQuery(String text) {
            String intermediate = COLLAPSE_SPACE.matcher(text).replaceAll(" ");
            return PRETTY_PRINT_SPLIT.matcher(intermediate).replaceAll("\n $1 ");
        }
    }
}
