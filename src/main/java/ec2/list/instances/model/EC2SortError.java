package ec2.list.instances.model;

public class EC2SortError extends RuntimeException {

    private static final long serialVersionUID = 3489028812569434252L;

    public EC2SortError(String message){
        super(message);
    }

}
