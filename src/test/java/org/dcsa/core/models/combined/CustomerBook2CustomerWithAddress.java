package org.dcsa.core.models.combined;

import lombok.Data;
import org.dcsa.core.model.ForeignKey;
import org.dcsa.core.models.AbstractCustomerBook;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("customer_book_table")
public class CustomerBook2CustomerWithAddress extends AbstractCustomerBook {
    @Column("customer_book_id")
    private Long id;

    @Column("customer_book_name")
    private String name;

    @Column("customer_id")
    @ForeignKey(into="aliceCustomer", foreignFieldName="id", viaJoinAlias = "alice_customer")
    private Long aliceCustomerId;

    @Transient
    private CustomerWithAddress aliceCustomer;

    @Column("customer_id")
    @ForeignKey(into="bobCustomer", foreignFieldName="id", viaJoinAlias = "bob_customer")
    private Long bobCustomerId;

    @Transient
    private CustomerWithAddress bobCustomer;
}
