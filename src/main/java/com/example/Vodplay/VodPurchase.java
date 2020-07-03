package com.example.Vodplay;

import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import javax.persistence.*;

@Entity
@Table(name="vodplay_table")
public class VodPurchase {
	@Id @GeneratedValue
	Long purchaseId;
	Long vodId;
	String vodName;
	String orderStatus = "ordered";
	 
	public Long getVodId() {return vodId;	}
	public void setVodId(Long vodId) {this.vodId = vodId;	}
	public String getVodName() {return vodName;	}
	public void setVodName(String vodName) {this.vodName = vodName;	}
	public Long getPurchaseId() { return purchaseId;}
	public void setPurchaseId(Long purchaseId) {  this.purchaseId = purchaseId;	}
	public String getOrderStatus() {return orderStatus;	}
	public void setOrderStatus(String orderStatus) {this.orderStatus = orderStatus;	}
	
	
	@PostPersist   // VodPurchased
	public void onCheck() {
//	 	if()
        // 1. 주문됨 이벤트 발송

//		VodPurchased vodPurchased = new VodPurchased();
//		vodPurchased.setPurchaseId(this.getPurchaseId());
//		vodPurchased.setVodId(this.getVodId());
//		vodPurchased.setVodName(this.getVodName());
//		
//		ObjectMapper objectMapper = new ObjectMapper();
//		String json = null;
//
//		try {
//		    json = objectMapper.writeValueAsString(vodPurchased);
//		} catch (JsonProcessingException e) {
//		    throw new RuntimeException("JSON format exception", e);
//		}
//
//		Processor processor = VodPurchaseApplication.applicationContext.getBean(Processor.class);
//		MessageChannel outputChannel = processor.output();
//		
//		outputChannel.send(MessageBuilder
//		        .withPayload(json)
//		        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
//		        .build());	 

	        // 2. 결재정보 post
		RestTemplate restTemplate = VodPurchaseApplication.applicationContext.getBean(RestTemplate.class);
		String payUrl = "http://localhost:8086/payments";
		Payment payment = new Payment();
		payment.setPurchaseId(this.getPurchaseId());
//		ResponseEntity<String> response = restTemplate.postForEntity(payUrl, payment, String.class);
		restTemplate.postForEntity(payUrl, payment, String.class);
	}		
	
	//put, patch
	@PostUpdate
	public void cancelOrder() {
		if(orderStatus != null && orderStatus.equals("cancel")) {
			System.out.println("[VodPurchase.java::cancelOrder ]");
			VodPurchaseRepository vodPurchaseRepository = 
					VodPurchaseApplication.applicationContext.getBean(VodPurchaseRepository.class);
			Optional<VodPurchase> opt = vodPurchaseRepository.findById(this.getPurchaseId());
			VodPurchase vod = opt.get();
			
			VodPurchaseCancelled vodPurchaseCancelled = new VodPurchaseCancelled();
			vodPurchaseCancelled.setPurchaseId(vod.getPurchaseId());
			vodPurchaseCancelled.setVodId(vod.getVodId());
			vodPurchaseCancelled.setVodName(vod.getVodName());
			
			ObjectMapper objectMapper = new ObjectMapper();
		    String json = null;

		    try {
		        json = objectMapper.writeValueAsString(vodPurchaseCancelled);
		    } catch (JsonProcessingException e) {
		        throw new RuntimeException("JSON format exception", e);
		    }

		    System.out.println("[VodPurchase.java:80:cancelOrder] "+json);
		    
		    Processor processor = VodPurchaseApplication.applicationContext.getBean(Processor.class);
		    MessageChannel outputChannel = processor.output();

		    outputChannel.send(MessageBuilder
		            .withPayload(json)
		            .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
		            .build());
		}
	}
}
