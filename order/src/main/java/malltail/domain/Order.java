package malltail.domain;

import malltail.domain.Ordered;
import malltail.domain.OrderCanceled;
import malltail.OrderApplication;
import javax.persistence.*;
import java.util.List;
import lombok.Data;
import java.util.Date;


@Entity
@Table(name="Order_table")
@Data

public class Order  {


    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    
    
    
    
    
    private Long id;
    
    
    
    
    
    private String customerId;
    
    
    
    
    
    private Long itemNo;
    
    
    
    
    
    private Long qty;
    
    
    
    
    
    private Date createDate;
    
    
    
    
    
    private String orderStatus;
    
    
    
    
    
    private String deliveryStatus;
    
    
    
    
    
    private String customerAddress;
    
    
    
    
    
    private String customerName;
    
    
    
    
    
    private String phoneNumber;
    
    
    
    
    
    private Date updateDate;

    @PostPersist
    public void onPostPersist(){

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.




        Ordered ordered = new Ordered(this);
        ordered.publishAfterCommit();



        OrderCanceled orderCanceled = new OrderCanceled(this);
        orderCanceled.publishAfterCommit();

        // Get request from Shipping
        //malltail.external.Shipping shipping =
        //    Application.applicationContext.getBean(malltail.external.ShippingService.class)
        //    .getShipping(/** mapping value needed */);

    }

    public static OrderRepository repository(){
        OrderRepository orderRepository = OrderApplication.applicationContext.getBean(OrderRepository.class);
        return orderRepository;
    }



    public void cancel(){
    }



}
