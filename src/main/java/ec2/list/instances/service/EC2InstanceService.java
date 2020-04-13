package ec2.list.instances.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import ec2.list.instances.model.EC2Instance;
import ec2.list.instances.model.EC2InstanceList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.context.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

@Singleton
public class EC2InstanceService {

    private static final Logger LOG = LoggerFactory.getLogger(EC2InstanceService.class);

    @Value("${default-results-per-page:6}")
    protected int defaultResultsPerPage;

    public EC2InstanceService(){}

    /*
     * Returns an EC2InstanceList POJO representation of the interesting fields.
     * Whilst we could have just returned the DescribeInstancesResponse object for completeness,
     *   it has a lot of excessive information that we don't really care about.
     *
     * Requires AWS access credentials and region
     * nextToken (for pagination) and resultsPerPage (default 6) are optional.
     */
    public EC2InstanceList getInstances(@NotBlank String awsAccessKeyID,
                                        @NotBlank String awsAccessKeySecret,
                                        @NotBlank String region,
                                        String nextToken,
                                        Integer resultsPerPage) {

        AwsCredentials awsCreds = AwsBasicCredentials.create(awsAccessKeyID, awsAccessKeySecret);
        Ec2Client client = Ec2Client.builder()
                                    .region(Region.of(region))
                                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                                    .build();
        return getInstanceList(client, nextToken, resultsPerPage);
	}

    //This is here to facilitate easier mocking of Ec2Client
	EC2InstanceList getInstanceList(@NotNull Ec2Client ec2Client, String nextToken, Integer resultsPerPage) {
        if(resultsPerPage == null){
            resultsPerPage = defaultResultsPerPage;
        }
        DescribeInstancesRequest request = DescribeInstancesRequest.builder().maxResults(resultsPerPage).nextToken(nextToken).build();
        DescribeInstancesResponse response = ec2Client.describeInstances(request);

        List<EC2Instance> instances = new ArrayList<EC2Instance>();
        for (Reservation reservation : response.reservations()) {
            for (Instance instance : reservation.instances()){
                instances.add(new EC2Instance(instance.instanceId(),
                                            instance.instanceType().toString(),
                                            instance.state().nameAsString(),
                                            instance.publicIpAddress(),
                                            instance.privateIpAddress()));
            }
        }
        LOG.info("Found " + instances.size() + " results, maxResults: " + resultsPerPage);

        return new EC2InstanceList(instances, response.nextToken());
    }


}
