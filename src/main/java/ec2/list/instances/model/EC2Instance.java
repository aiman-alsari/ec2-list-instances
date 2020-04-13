package ec2.list.instances.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*
 * Holds a representation of an EC2Instance.
 * ID is the instance ID e.g. i-0069d953f5c1ba654
 * type is the VM type e.g. t2.micro
 * state is the instance state, e.g. running or stopped
 * publicIP and privateIP are self explanatory
 */
public class EC2Instance {

    private String id;
    private String type;
    private String state;
    private String publicIP;
    private String privateIP;

    public EC2Instance() {
    }

    public EC2Instance(String id, String type, String state, String publicIP, String privateIP) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.publicIP = publicIP;
        this.privateIP = privateIP;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPublicIP() {
        return this.publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getPrivateIP() {
        return this.privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public EC2Instance id(String id) {
        this.id = id;
        return this;
    }

    public EC2Instance type(String type) {
        this.type = type;
        return this;
    }

    public EC2Instance state(String state) {
        this.state = state;
        return this;
    }

    public EC2Instance publicIP(String publicIP) {
        this.publicIP = publicIP;
        return this;
    }

    public EC2Instance privateIP(String privateIP) {
        this.privateIP = privateIP;
        return this;
    }

    static Map<String,Comparator<EC2Instance>> COMPARATOR_LOOKUP;
    static {
        Map<String,Comparator<EC2Instance>> m = new HashMap<String,Comparator<EC2Instance>>();
        m.put("id", Comparator.comparing(i -> i.id));
        m.put("type", Comparator.comparing(i -> i.type));
        m.put("state", Comparator.comparing(i -> i.state));
        m.put("publicIP", Comparator.comparing(i -> i.publicIP));
        m.put("privateIP", Comparator.comparing(i -> i.privateIP));
        COMPARATOR_LOOKUP = Collections.unmodifiableMap(m);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EC2Instance)) {
            return false;
        }
        EC2Instance eC2Instance = (EC2Instance) o;
        return Objects.equals(id, eC2Instance.id) && Objects.equals(type, eC2Instance.type) && Objects.equals(state, eC2Instance.state) && Objects.equals(publicIP, eC2Instance.publicIP) && Objects.equals(privateIP, eC2Instance.privateIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, state, publicIP, privateIP);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", type='" + getType() + "'" +
            ", state='" + getState() + "'" +
            ", publicIP='" + getPublicIP() + "'" +
            ", privateIP='" + getPrivateIP() + "'" +
            "}";
    }

}
