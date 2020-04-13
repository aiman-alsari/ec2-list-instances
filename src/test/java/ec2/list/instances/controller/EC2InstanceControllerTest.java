package ec2.list.instances.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import ec2.list.instances.model.EC2Instance;
import ec2.list.instances.model.EC2InstanceList;
import ec2.list.instances.service.EC2InstanceService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;

@MicronautTest
public class EC2InstanceControllerTest {

    @Inject
    @Client("/")
    RxHttpClient client;

    @Inject
    EC2InstanceService service;


    @Test
    void testGetInstancesMandatoryParameters(){
        when( service.getInstances("test-key-id", "test-key", "us-east-2", null, null) )
             .thenReturn(new EC2InstanceList(new ArrayList<EC2Instance>(), null));

        HttpRequest<Object> request = HttpRequest.GET("/instances/us-east-2");
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(request, EC2InstanceList.class)
        );
        assertEquals("Required Header [aws-access-key-id] not specified", e.getMessage());
        assertEquals(400, e.getResponse().getStatus().getCode());

        HttpRequest<Object> request2 = HttpRequest.GET("/instances/us-east-2")
                                                  .header("aws-access-key-id", "test-key-id");
        e = assertThrows(HttpClientResponseException.class, () ->
            client.toBlocking().retrieve(request2, EC2InstanceList.class)
        );
        assertEquals("Required Header [aws-access-key-secret] not specified", e.getMessage());
        assertEquals(400, e.getResponse().getStatus().getCode());

        HttpRequest<Object> request3 = HttpRequest.GET("/instances/us-east-2")
                                                  .header("aws-access-key-id", "test-key-id")
                                                  .header("aws-access-key-secret", "test-key");
        client.toBlocking().retrieve(request3, EC2InstanceList.class);

    }

    @Test
    void testGetInstancesDefaultSortByIdAscending(){
        List<EC2Instance> instances = Arrays.asList(
            new EC2Instance("2", "type", "state", "publicIP", "privateIP"),
            new EC2Instance("3", "type", "state", "publicIP", "privateIP"),
            new EC2Instance("1", "type", "state", "publicIP", "privateIP")
        );

        when( service.getInstances("test-key-id", "test-key", "us-east-2", null, null) )
        .thenReturn(new EC2InstanceList(instances, null));

        HttpRequest<Object> request = HttpRequest.GET("/instances/us-east-2")
                                                  .header("aws-access-key-id", "test-key-id")
                                                  .header("aws-access-key-secret", "test-key");
        EC2InstanceList result = client.toBlocking().retrieve(request, EC2InstanceList.class);
        assertEquals(3, result.getCount());
        assertEquals("1", result.getInstances().get(0).getId());
        assertEquals("2", result.getInstances().get(1).getId());
        assertEquals("3", result.getInstances().get(2).getId());
    }

    @Test
    void testGetInstancesSortByTypeDescending(){
        List<EC2Instance> instances = Arrays.asList(
            new EC2Instance("1", "t1.xyz", "state", "publicIP", "privateIP"),
            new EC2Instance("2", "t1.abc", "state", "publicIP", "privateIP"),
            new EC2Instance("3", "t2.abc", "state", "publicIP", "privateIP")
        );

        when( service.getInstances("test-key-id", "test-key", "us-east-2", null, null) )
        .thenReturn(new EC2InstanceList(instances, null));

        HttpRequest<Object> request = HttpRequest.GET("/instances/us-east-2?sortField=type&sortAscending=false")
                                                  .header("aws-access-key-id", "test-key-id")
                                                  .header("aws-access-key-secret", "test-key");
        EC2InstanceList result = client.toBlocking().retrieve(request, EC2InstanceList.class);
        assertEquals(3, result.getCount());
        assertEquals("t2.abc", result.getInstances().get(0).getType());
        assertEquals("t1.xyz", result.getInstances().get(1).getType());
        assertEquals("t1.abc", result.getInstances().get(2).getType());
    }


    @MockBean(EC2InstanceService.class)
    EC2InstanceService ec2InstanceService() {
        return mock(EC2InstanceService.class);
    }
}
