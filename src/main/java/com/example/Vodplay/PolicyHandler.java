package com.example.Vodplay;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.cloud.stream.messaging.Processor;

public class PolicyHandler {

	@Autowired
	VodPurchaseRepository vodPurchaseRepository;
	
	@StreamListener(Processor.INPUT)
	public void onVodRegister(@Payload PaymentApproved paymentApproved) {
		if( paymentApproved.getEventType().equals("PaymentApproved")){
			System.out.println("=========PaymentApproved STR=============");
			
			Long id = paymentApproved.getPayId();       
	        Optional<VodPurchase> vodById = vodPurchaseRepository.findById(id);
	        VodPurchase p = vodById.get();
			vodPurchaseRepository.save(p);
			System.out.println("=========PaymentApproved END=============");
		}
	}
}