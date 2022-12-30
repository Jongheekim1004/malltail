package malltail.infra;

import malltail.domain.*;
import malltail.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class StatusViewViewHandler {

    @Autowired
    private StatusViewRepository statusViewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1 (@Payload Ordered ordered) {
        try {

            if (!ordered.validate()) return;

            // view 객체 생성
            StatusView statusView = new StatusView();
            // view 객체에 이벤트의 Value 를 set 함
            statusView.setId(ordered.getId());
            statusView.setOrderNo(ordered.getOrderNo());
            statusView.setCustomerId(ordered.getCustomerId());
            statusView.setItemNo(ordered.getItemNo());
            statusView.setQty(ordered.getQty());
            statusView.setCreateDate(ordered.getCreateDate());
            statusView.setOrderStatus(ordered.getOrderStatus());
            // view 레파지 토리에 save
            statusViewRepository.save(statusView);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_UPDATE_1(@Payload OrderCanceled orderCanceled) {
        try {
            if (!orderCanceled.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(orderCanceled.getOrderNo());
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setOrderStatus(orderCanceled.getOrderStatus());
                    statusView.setUpdateDate(orderCanceled.getUpdateDate());
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderDeliveryStarted_then_UPDATE_2(@Payload OrderDeliveryStarted orderDeliveryStarted) {
        try {
            if (!orderDeliveryStarted.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(orderDeliveryStarted.getOrderNo());
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setDeliveryStatus(orderDeliveryStarted.getDeliveryStatus());
                    //statusView.setUpdateDate(sysdate);
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderRejected_then_UPDATE_3(@Payload OrderRejected orderRejected) {
        try {
            if (!orderRejected.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(orderRejected.getOrderNo());
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setOrderStatus(orderRejected.getOrderStatus());
                    //statusView.setUpdateDate(sysdate);
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderPaid_then_UPDATE_4(@Payload OrderPaid orderPaid) {
        try {
            if (!orderPaid.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(orderPaid.getOrderNo());
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setOrderStatus(orderPaid.getPaystatus());
                    //statusView.setUpdateDate(sysdate);
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenShippingStarted_then_UPDATE_5(@Payload ShippingStarted shippingStarted) {
        try {
            if (!shippingStarted.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(Long.valueOf(shippingStarted.getOrderNo()));
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setDeliveryStatus(shippingStarted.getShippingStatus());
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenShippingCompleted_then_UPDATE_6(@Payload ShippingCompleted shippingCompleted) {
        try {
            if (!shippingCompleted.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(Long.valueOf(shippingCompleted.getOrderNo()));
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setDeliveryStatus(shippingCompleted.getShippingStatus());
                    statusView.setTransportNo(String.valueOf(shippingCompleted.getTransportNo()));
                    statusView.setIsShipped(true);
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCustomsRejected_then_UPDATE_7(@Payload CustomsRejected customsRejected) {
        try {
            if (!customsRejected.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(Long.valueOf(customsRejected.getOrderNo()));
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setDeliveryStatus(customsRejected.getDeliveryStatus());
                    statusView.setIsCoutomsApproved(false);
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_8(@Payload DeliveryStarted deliveryStarted) {
        try {
            if (!deliveryStarted.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(Long.valueOf(deliveryStarted.getOrderNo()));
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setDeliveryStatus(deliveryStarted.getDeliveryStatus());
                    statusView.setInvoiceNo(String.valueOf(deliveryStarted.getInvoiceNo()));
                    statusView.setIsCoutomsApproved(true);
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCompleted_then_UPDATE_9(@Payload DeliveryCompleted deliveryCompleted) {
        try {
            if (!deliveryCompleted.validate()) return;
                // view 객체 조회

                List<StatusView> statusViewList = statusViewRepository.findByOrderNo(Long.valueOf(deliveryCompleted.getOrderNo()));
                for(StatusView statusView : statusViewList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    statusView.setDeliveryStatus(deliveryCompleted.getDeliveryStatus());
                // view 레파지 토리에 save
                statusViewRepository.save(statusView);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCanceled_then_DELETE_1(@Payload OrderCanceled orderCanceled) {
        try {
            if (!orderCanceled.validate()) return;
            // view 레파지 토리에 삭제 쿼리
            statusViewRepository.deleteByOrderNo(orderCanceled.getOrderNo());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

