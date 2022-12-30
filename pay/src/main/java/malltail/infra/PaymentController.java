package malltail.infra;
import malltail.domain.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;


@RestController
// @RequestMapping(value="/payments")
@Transactional
public class PaymentController {
    @Autowired
    PaymentRepository paymentRepository;



    // @RequestMapping(value = "payments/{id}/pay",
    //     method = RequestMethod.PUT,
    //     produces = "application/json;charset=UTF-8")
    // public Payment pay(@PathVariable(value = "id") Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
    //         System.out.println("##### /payment/pay  called #####");
    //         Optional<Payment> optionalPayment = paymentRepository.findById(id);
            
    //         optionalPayment.orElseThrow(()-> new Exception("No Entity Found"));
    //         Payment payment = optionalPayment.get();
    //         payment.pay();
            
    //         paymentRepository.save(payment);
    //         return payment;
            
    // }
    



}
