package malltail.domain;

import malltail.domain.*;
import malltail.infra.AbstractEvent;
import java.util.*;
import lombok.*;


@Data
@ToString
public class Ordered extends AbstractEvent {

    private Long id;
    private Long itemNo;
    private Long qty;
    private Date createDate;
    private String status;
    private String orderStatus;
    private String deliveryStatus;
    private String customerId;
    private String customerAddress;
    private String customerName;
    private String phoneNumber;

    public Ordered(Order aggregate){
        super(aggregate);
    }
    public Ordered(){
        super();
    }
}
