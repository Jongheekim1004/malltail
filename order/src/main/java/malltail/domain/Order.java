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

    @PrePersist
    public void onPrePersist() {
        setOrderStatus("Ordered");
    }

    @PostPersist
    public void onPostPersist(){

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        // Pay 생성
        malltail.external.Payment payment = new malltail.external.Payment();
        payment.setOrderNo(getId());
        payment.setItemNo(getItemNo());
        payment.setPaystatus("Paid");

        // mappings goes here
        OrderApplication.applicationContext
            .getBean(malltail.external.PaymentService.class)
            .pay(payment);

        Ordered ordered = new Ordered(this);
        ordered.publishAfterCommit();
    }

    public static OrderRepository repository(){
        OrderRepository orderRepository = OrderApplication.applicationContext.getBean(OrderRepository.class);
        return orderRepository;
    }

    public void cancel(){

        // 이미 shipping이 된 이후에는 취소할 수 없다.
        try{
            
            malltail.external.Shipping shipping =
            OrderApplication.applicationContext.getBean(malltail.external.ShippingService.class)
            .getShipping(getId());

            throw new RuntimeException("Cannot cancel!");
        } catch (Exception e){
            // shipping이 존재하지 않는 경우 
            
            // Order와 delivery 상태를 변경한다. 
            setOrderStatus("Canceled");
        }

        OrderCanceled orderCanceled = new OrderCanceled(this);
        orderCanceled.publishAfterCommit();
    }



}
