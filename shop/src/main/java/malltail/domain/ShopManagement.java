package malltail.domain;

import malltail.domain.OrderDeliveryStarted;
import malltail.domain.OrderRejected;
import malltail.ShopApplication;
import javax.persistence.*;
import java.util.List;
import lombok.Data;
import java.util.Date;


@Entity
@Table(name="ShopManagement_table")
@Data

public class ShopManagement  {


    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    
    
    
    
    
    private Long id;
    
    
    
    
    
    private Long orderNo;
    
    
    
    
    
    private String customerId;
    
    
    
    
    
    private Long itemNo;
    
    
    
    
    
    private Long qty;
    
    
    
    
    
    private Date createDate;
    
    
    
    
    
    private String orderStatus;
    
    
    
    
    
    private String deliveryStatus;
    
    
    
    
    
    private String customerAddress;
    
    
    
    
    
    private String shippingAddress;
    
    
    
    
    
    private Long payId;
    
    
    
    
    
    private String customerName;
    
    
    
    
    
    private String phoneNumber;
    
    
    
    
    
    private String shopName;

    @PostPersist
    public void onPostPersist(){


        OrderDeliveryStarted orderDeliveryStarted = new OrderDeliveryStarted(this);
        orderDeliveryStarted.publishAfterCommit();



        OrderRejected orderRejected = new OrderRejected(this);
        orderRejected.publishAfterCommit();

        // Get request from StatusView
        //malltail.external.StatusView statusView =
        //    Application.applicationContext.getBean(malltail.external.StatusViewService.class)
        //    .getStatusView(/** mapping value needed */);

        // Get request from StatusView
        //malltail.external.StatusView statusView =
        //    Application.applicationContext.getBean(malltail.external.StatusViewService.class)
        //    .getStatusView(/** mapping value needed */);

    }

    public static ShopManagementRepository repository(){
        ShopManagementRepository shopManagementRepository = ShopApplication.applicationContext.getBean(ShopManagementRepository.class);
        return shopManagementRepository;
    }



    public void acceptOrder(){
    }
    public void rejectOrder(){
    }

    public static void orderInfoUpdate(OrderPaid orderPaid){

        ShopManagement shopManagement = new ShopManagement();
        shopManagement.setOrderNo(orderPaid.getOrderNo());
        shopManagement.setItemNo(orderPaid.getItemNo());
        shopManagement.setDeliveryStatus("Ready");

        repository().save(shopManagement);        
    }
    public static void orderInfoUpdate(OrderCanceled orderCanceled){

        /** Example 1:  new item 
        ShopManagement shopManagement = new ShopManagement();
        repository().save(shopManagement);

        */

        /** Example 2:  finding and process
        
        repository().findById(orderCanceled.get???()).ifPresent(shopManagement->{
            
            shopManagement // do something
            repository().save(shopManagement);


         });
        */

        
    }


}
