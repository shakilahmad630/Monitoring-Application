package com.vivatech.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonitoringApplication {

	public static void main(String[] args) {

		SpringApplication.run(MonitoringApplication.class, args);
		AppReader reader = new AppReader();
		reader.readApp(null);
	}

}
