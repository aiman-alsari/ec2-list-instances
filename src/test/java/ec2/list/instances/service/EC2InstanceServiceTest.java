package ec2.list.instances.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import ec2.list.instances.model.EC2Instance;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.micronaut.test.annotation.MicronautTest;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceState;
import software.amazon.awssdk.services.ec2.model.Reservation;

@MicronautTest
public class EC2InstanceServiceTest {

    @Inject
    EC2InstanceService service;

    @Test
    void testGetInstancesNoResults() {
        Ec2Client ec2Client = mock(Ec2Client.class);
        ArgumentCaptor<DescribeInstancesRequest> requestCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
        DescribeInstancesResponse mockResponse = DescribeInstancesResponse.builder()
                                                                          .reservations(new ArrayList<Reservation>())
                                                                          .build();
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(mockResponse);
        List<EC2Instance> instances = service.getInstanceList(ec2Client, null, null).getInstances();
        assertEquals(0, instances.size());

        verify(ec2Client, only()).describeInstances(any(DescribeInstancesRequest.class));
        verify(ec2Client).describeInstances(requestCaptor.capture());

        //Validate the default max results per page is set to 6
        assertEquals(6, requestCaptor.getValue().maxResults());
        assertNull(requestCaptor.getValue().nextToken());
    }

    @Test
    void testGetInstancesMaxResults() {
        Ec2Client ec2Client = mock(Ec2Client.class);
        ArgumentCaptor<DescribeInstancesRequest> requestCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
        DescribeInstancesResponse mockResponse = DescribeInstancesResponse.builder()
                                                                          .reservations(new ArrayList<Reservation>())
                                                                          .build();
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(mockResponse);
        service.getInstanceList(ec2Client, null, 2);

        verify(ec2Client).describeInstances(requestCaptor.capture());

        //Validate the max results per page is overriden to 2
        assertEquals(2, requestCaptor.getValue().maxResults());
    }

    @Test
    void testGetInstancesNextPageToken() {
        Ec2Client ec2Client = mock(Ec2Client.class);
        ArgumentCaptor<DescribeInstancesRequest> requestCaptor = ArgumentCaptor.forClass(DescribeInstancesRequest.class);
        DescribeInstancesResponse mockResponse = DescribeInstancesResponse.builder()
                                                                          .reservations(new ArrayList<Reservation>())
                                                                          .build();
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(mockResponse);
        service.getInstanceList(ec2Client, "testNextToken", null);

        verify(ec2Client).describeInstances(requestCaptor.capture());

        assertEquals("testNextToken", requestCaptor.getValue().nextToken());
    }

    @Test
    void testGetInstancesOneResult() {
        Ec2Client ec2Client = mock(Ec2Client.class);
        Instance instance = Instance.builder()
                                    .instanceId("id")
                                    .instanceType("t1.nano")
                                    .state(InstanceState.builder().name("running").build())
                                    .publicIpAddress("1.2.3.4")
                                    .privateIpAddress("10.0.0.1")
                                    .build();
        Reservation reservation = Reservation.builder()
                                             .instances(instance)
                                             .build();
        DescribeInstancesResponse mockResponse = DescribeInstancesResponse.builder()
                                                                          .reservations(Arrays.asList(reservation))
                                                                          .build();
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(mockResponse);
        List<EC2Instance> instances = service.getInstanceList(ec2Client, null, null).getInstances();
        verify(ec2Client, only()).describeInstances(any(DescribeInstancesRequest.class));
        assertEquals(1, instances.size());
        assertEquals("id", instances.get(0).getId());
    }

    @Test
    void testGetInstancesMultipleResults() {
        Ec2Client ec2Client = mock(Ec2Client.class);
        Instance instance = Instance.builder()
                                    .instanceId("id1")
                                    .instanceType("t1.micro")
                                    .state(InstanceState.builder().name("running").build())
                                    .publicIpAddress("1.2.3.4")
                                    .privateIpAddress("10.0.0.1")
                                    .build();
        Instance instance2 = Instance.builder()
                                    .instanceId("id2")
                                    .instanceType("t1.small")
                                    .state(InstanceState.builder().name("stopped").build())
                                    .publicIpAddress("1.2.3.5")
                                    .privateIpAddress("10.0.0.2")
                                    .build();
        Reservation reservation = Reservation.builder()
                                             .instances(instance)
                                             .build();
        Reservation reservation2 = Reservation.builder()
                                             .instances(instance2)
                                             .build();
        DescribeInstancesResponse mockResponse = DescribeInstancesResponse.builder()
                                                                          .reservations(Arrays.asList(reservation, reservation2))
                                                                          .build();
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(mockResponse);
        List<EC2Instance> instances = service.getInstanceList(ec2Client, null, null).getInstances();
        verify(ec2Client, only()).describeInstances(any(DescribeInstancesRequest.class));
        assertEquals(2, instances.size());
        assertEquals("id1", instances.get(0).getId());
        assertEquals("t1.micro", instances.get(0).getType());
        assertEquals("running", instances.get(0).getState());
        assertEquals("1.2.3.4", instances.get(0).getPublicIP());
        assertEquals("10.0.0.1", instances.get(0).getPrivateIP());

        assertEquals("id2", instances.get(1).getId());
    }

    @Test
    void testGetInstancesMandatoryParameters(){
        final ConstraintViolationException exception =
            assertThrows(ConstraintViolationException.class, () ->
            service.getInstances("", "", "", null, null)
        );

        assertTrue(exception.getMessage().contains("getInstances.awsAccessKeyID: must not be blank"));
        assertTrue(exception.getMessage().contains("getInstances.awsAccessKeySecret: must not be blank"));
        assertTrue(exception.getMessage().contains("getInstances.region: must not be blank"));
    }

}
