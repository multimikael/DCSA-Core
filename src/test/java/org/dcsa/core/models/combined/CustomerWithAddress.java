package org.dcsa.core.models.combined;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dcsa.core.model.JoinedWithModel;
import org.dcsa.core.model.ModelClass;
import org.dcsa.core.model.PrimaryModel;
import org.dcsa.core.models.Address;
import org.dcsa.core.models.Customer;

@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryModel(Customer.class)
@JoinedWithModel(lhsFieldName = "addressId", rhsModel = Address.class, rhsFieldName = "addressId")
public class CustomerWithAddress extends Customer {
    @ModelClass(Address.class)
    private String address;
}
