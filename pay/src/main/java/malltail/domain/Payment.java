package malltail.domain;

import malltail.domain.OrderPaid;
import malltail.PayApplication;
import javax.persistence.*;
import java.util.List;
import lombok.Data;
import java.util.Date;


@Entity
@Table(name="Payment_table")
@Data

public class Payment  {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    
    private Long id;
    
    private Long payId;
    
    private Long orderNo;
    
    private String paystatus;
    
    private Long itemNo;

    @PrePersist
    public void onPrePersist(){  //적당한 시간 끌기        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PostPersist
    public void onPostPersist(){

        OrderPaid orderPaid = new OrderPaid(this);
        orderPaid.publishAfterCommit();

    }

    public static PaymentRepository repository(){
        PaymentRepository paymentRepository = PayApplication.applicationContext.getBean(PaymentRepository.class);
        return paymentRepository;
    }

    public void pay(){
    }

    public static void cancelPayment(OrderCanceled orderCanceled){

        repository().findById(orderCanceled.getId()).ifPresent(payment->{
            
            payment.setPaystatus("Refunded");
            repository().save(payment);

         });
        
    }


}
