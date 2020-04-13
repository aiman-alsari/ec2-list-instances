package ec2.list.instances.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class EC2InstanceList {

    private List<EC2Instance> instances;
    private String nextToken;

    public EC2InstanceList() {
    }

    public EC2InstanceList(List<EC2Instance> instances, String nextToken) {
        this.instances = instances;
        this.nextToken = nextToken;
    }

    public List<EC2Instance> getInstances() {
        return this.instances;
    }

    public void setInstances(List<EC2Instance> instances) {
        this.instances = instances;
    }

    public String getNextToken() {
        return this.nextToken;
    }

    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }

    public int getCount(){
        return instances.size();
    }

    public EC2InstanceList instances(List<EC2Instance> instances) {
        this.instances = instances;
        return this;
    }

    public EC2InstanceList nextToken(String nextToken) {
        this.nextToken = nextToken;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EC2InstanceList)) {
            return false;
        }
        EC2InstanceList eC2InstanceList = (EC2InstanceList) o;
        return Objects.equals(instances, eC2InstanceList.instances) && Objects.equals(nextToken, eC2InstanceList.nextToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instances, nextToken);
    }

    @Override
    public String toString() {
        return "{" +
            " instances='" + getInstances() + "'" +
            ", nextToken='" + getNextToken() + "'" +
            "}";
    }


    /*
     * Sort based on field name and direction
     * Unfortunately this only sorts the current page of results, as the AWS SDK has no mechanism to retrieve sorted results.
     * Fetching everything just to sort it here defeats the purpose of paging results, and will require re-implementing pagination which would make this
     * application not stateless.
     */
    public void sort(String field, Boolean ascending) {
        if(field == null) {
            field = "id";
        }
        if(ascending == null){
            ascending = true;
        }
        Comparator<EC2Instance> c = EC2Instance.COMPARATOR_LOOKUP.get(field);
        if (c == null){
            throw new IllegalArgumentException(field);
        }
        instances.sort(c);
        if(!ascending){
            Collections.reverse(instances);
        }
    }

}
