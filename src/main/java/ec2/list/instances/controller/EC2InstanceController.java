package ec2.list.instances.controller;

import javax.annotation.Nullable;
import javax.inject.Inject;

import ec2.list.instances.model.EC2InstanceList;
import ec2.list.instances.service.EC2InstanceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.QueryValue;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

@Controller("/")
public class EC2InstanceController {
    private static final Logger LOG = LoggerFactory.getLogger(EC2InstanceController.class);

    @Inject
    EC2InstanceService service;

    EC2InstanceController(EC2InstanceService service){
        this.service = service;
    }

    @Get("/instances/{region}")
    public HttpResponse<EC2InstanceList> getInstances(@Header("aws-access-key-id") String awsAccessKeyID,
                               @Header("aws-access-key-secret") String awsAccessKeySecret,
                               String region,
                               @Nullable @QueryValue String nextToken,
                               @Nullable @QueryValue Integer maxResults,
                               @Nullable @QueryValue String sortField,
                               @Nullable @QueryValue Boolean sortAscending){
        try{
            EC2InstanceList instances = service.getInstances(awsAccessKeyID, awsAccessKeySecret, region, nextToken, maxResults);
            instances.sort(sortField, sortAscending);
            return HttpResponse.ok(instances);
        } catch (Ec2Exception e){
            if (e.awsErrorDetails().errorCode().equals("401")){
                return HttpResponse.unauthorized();
            }
            LOG.error("Unexpected error", e);
            return HttpResponse.serverError();
        }
    }


}
