package com.example;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@EnableDiscoveryClient // Acts as a Service Discover client
@SpringBootApplication // Acts as a Spring Boot 
@EnableFeignClients // Acts as a Balanced Rest Template
@EnableCircuitBreaker // Acts as a Hystrix as circuit breaker
@EnableZuulProxy // Acts as a Gateway API
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
	
	@Bean
	CommandLineRunner dc(DiscoveryClient dc){
		return args -> 
			dc.getInstances("reservation-service").forEach( i -> System.err.println(
				String.format("(%s), %s, %s", i.getServiceId(), i.getHost(), i.getPort())
			) );
	}
	
	@Bean
	CommandLineRunner rt(RestTemplate rt){
		return args->
			rt.exchange("http://reservation-service/reservations", HttpMethod.GET, null, 
				new ParameterizedTypeReference<List<Reservation>>() { }
			).getBody().forEach(System.err::println);		
	}
	
	@Bean
	CommandLineRunner fc(ReservationClient rc){
		return args->
			rc.getReservations().forEach(System.err::println);
	}
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
}

@Component
class ReservationNames {
	
	@Autowired
	ReservationClient reservationClient;
	
	public Collection<String> getReservationNamesFallback(){
		return Collections.emptyList();
	}
	
	
	@HystrixCommand(fallbackMethod="getReservationNamesFallback")
	public Collection<String> getReservationNames(){
	return this.reservationClient.getReservations()
			.stream()
			.map(r -> r.getReservationName())
			.collect(Collectors.toList());
	}
}

@RestController
class ReservationNamesController {
	
	@Autowired
	ReservationNames names;
	
	@RequestMapping("/reservation-names" )
	Collection<String> getNames(){
		return names.getReservationNames();
	}
}

@FeignClient("reservation-service")
interface ReservationClient {
	
	@RequestMapping(value="/reservations", method=RequestMethod.GET)
	Collection<Reservation> getReservations();
}

class Reservation {
	
	private Long id;
	private String reservationName;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getReservationName() {
		return reservationName;
	}
	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
	@Override
	public String toString() {
		return "Reservation [id=" + id + ", reservationName=" + reservationName + "]";
	}
	
	
}
