package com.springbootpetclinic.web;

import com.springbootpetclinic.model.Owner;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PetClinicRestControllerTest {
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
        BasicAuthenticationInterceptor basicAuthenticationInterceptor = new BasicAuthenticationInterceptor("user", "secret");
        restTemplate.setInterceptors(Arrays.asList(basicAuthenticationInterceptor));
    }

    @Test
    public void testOwnerById() {
        ResponseEntity<Owner> response = restTemplate.getForEntity("http://localhost:8085/rest/owner/1", Owner.class);

        MatcherAssert.assertThat(response.getStatusCodeValue(), Matchers.equalTo(200));
        //MatcherAssert.assertThat(response.getBody().getFirstName(), Matchers.equalTo("Muhammed"));
    }

    @Test
    public void testOwnerByLastName() {
        ResponseEntity<List> response = restTemplate.getForEntity("http://localhost:8080/rest/owner?ln=Demir", List.class);
        MatcherAssert.assertThat(response.getStatusCodeValue(), Matchers.equalTo(200));
        List<Map<String, String>> body = response.getBody();
        List<String> firstNames = body.stream().map(e -> e.get("firstName")).collect(Collectors.toList()); //Listenin icerisindeki map elemanına ulaşarak
        // herbır map'in içerisinden lastName eriserek bir string listesine ekliyoruz.

        MatcherAssert.assertThat(firstNames, Matchers.containsInAnyOrder("Muhammed"));//Listenin içerisinde var mı sorusu
    }

    @Test
    public void testGetOwners() {
        ResponseEntity<List> response = restTemplate.getForEntity("http://localhost:8080/rest/owners", List.class);
        MatcherAssert.assertThat(response.getStatusCodeValue(), Matchers.equalTo(200));
        List<Map<String, String>> body = response.getBody();
        List<String> firstNames = body.stream().map(e -> e.get("firstName")).collect(Collectors.toList());//Listenin icerisindeki map elemanına ulaşarak
        // herbır map'in içerisinden lastName eriserek bir string listesine ekliyoruz.

        MatcherAssert.assertThat(firstNames, Matchers.containsInAnyOrder("Muhammed,Ahmet,Yunus,Bekir"));//Listenin içerisinde var mı sorusu
    }

    @Test
    public void testCreateOwner() {
        Owner owner = new Owner();
        owner.setFirstName("Mustafa");
        owner.setLastName("Karacuha");
        URI location = restTemplate.postForLocation("http://localhost:8080/rest/owner", owner);// nesneyi sunucu tarafında insert ediyoruz

        Owner owner1 = restTemplate.getForObject(location, Owner.class);//yeni oluşan owner nesnesine erisiyoruz.
        System.out.println(owner1.toString());

        MatcherAssert.assertThat(owner1.getFirstName(), Matchers.equalTo(owner.getFirstName()));
        MatcherAssert.assertThat(owner1.getLastName(), Matchers.equalTo(owner.getLastName()));
    }

    @Test
    public void testUpdateOwner() {
        Owner owner = restTemplate.getForObject("http://localhost:8080/rest/owner/1", Owner.class);// id si 1 olan owner bilgilerini cekiyorum
        MatcherAssert.assertThat(owner.getFirstName(), Matchers.equalTo("Muhammed"));
        owner.setFirstName("Eren");
        restTemplate.put("http://localhost:8080/rest/owner/1", owner);// 1 idli owner güncelleme işlemi yapıldı.

        owner = restTemplate.getForObject("http://localhost:8080/rest/owner/1", Owner.class);
        MatcherAssert.assertThat(owner.getFirstName(), Matchers.equalTo("Eren"));
    }

    @Test
    public void testDeleteOnwer() {
        restTemplate.delete("http://localhost:8080/rest/owner/2");// id'si 2 olan owner siliniyor
        try {
            Owner owner = restTemplate.getForObject("http://localhost:8080/rest/owner/2", Owner.class);// id si 2 olan owner bilgilerini cekiyorum
            Assert.fail("should have not returned owner ");
        } catch (HttpClientErrorException ex) {
            MatcherAssert.assertThat(ex.getStatusCode().value(), Matchers.equalTo(404));
        }

    }
}
