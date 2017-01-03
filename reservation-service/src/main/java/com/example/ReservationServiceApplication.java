package com.example;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {
	
	@Bean
	CommandLineRunner runner(ReservationRepository r){
		return args -> {
			 Arrays.asList(("Ramakrishna, Kyramkonda, VaraLaxmi")
				   .split(","))
			 	   .forEach(e -> r.save(new Reservation(e)));
			 r.findAll().forEach(System.out :: println);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@RefreshScope
@RestController
class ReservationController{
	
	@Autowired
	ReservationRepository reservationRespository;
	
	@Value("${message}")
	String message;
	
	@RequestMapping("/message") 
	String getMessage(){
		return this.message;
	}
	
	@RequestMapping("/reservations")
	Collection<Reservation> reservations(){
		return this.reservationRespository.findAll();
	}
	
}

interface ReservationRepository extends JpaRepository<Reservation, Long> {
}

@Entity
class Reservation {
	
	@Id @GeneratedValue
	private Long id;
	
	private String reservationName;
	
	public Reservation() {
	}

	public Reservation(String reservationName) {
		this.reservationName = reservationName;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Reservation [id=" + id + ", reservationName=" + reservationName + "]";
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
	
	
}
