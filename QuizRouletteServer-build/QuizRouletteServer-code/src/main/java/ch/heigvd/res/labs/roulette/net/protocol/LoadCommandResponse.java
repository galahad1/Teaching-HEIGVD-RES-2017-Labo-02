package ch.heigvd.res.labs.roulette.net.protocol;

/**
 * Created by tano on 24.03.17.
 */
public class LoadCommandResponse {
    private String status;
    private int numberOfNewStudents;

    public LoadCommandResponse(){}

    public LoadCommandResponse(String state, int nbrNewStudents)
    {
        this.status = state;
        this.numberOfNewStudents = nbrNewStudents;
    }
    public String getStatus(){
        return status;
    }

    public int getNumberOfNewStudents()
    {
        return numberOfNewStudents;
    }
}
