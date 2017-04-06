package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@EnableFeignClients
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class PwaEdgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PwaEdgeServiceApplication.class, args);
    }
}

@Data
class Beer {
    private String name;
}

@FeignClient("beer-catalog-service")
interface BeerClient {

    @RequestMapping(method = RequestMethod.GET, value = "/beers")
    Resources<Beer> lireLesBieres();
}

@RestController
class GoodBeerApiAdapterRestController {

    private final BeerClient beerClient;

    GoodBeerApiAdapterRestController(BeerClient beerClient) {
        this.beerClient = beerClient;
    }

    public Collection<Beer> fallback(){
        return new ArrayList<>();
    }

    @HystrixCommand (fallbackMethod = "fallback")
    @GetMapping("/good-beers")
    public Collection<Beer> goodBears() {
        return beerClient.lireLesBieres()
                .getContent()
                .stream()
                .filter(biere -> !biere.getName().equalsIgnoreCase("Budweiser"))
                .collect(Collectors.toList());
    }

}