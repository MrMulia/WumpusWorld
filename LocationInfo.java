
import java.util.Arrays;

public class LocationInfo{
    private int[] coordinates;
    private boolean hasBreeze;
    private boolean hasStench;

    public LocationInfo(int[] coordinates, boolean hasBreeze, boolean hasStench){
        this.coordinates = coordinates;
        this.hasBreeze = hasBreeze;
        this.hasStench = hasStench;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public boolean hasBreeze() {
        return hasBreeze;
    }

    public boolean hasStench() {
        return hasStench;
    }

    public String toString() {
        char breezeVar;
        char stenchVar;
        if (hasBreeze == true){
            breezeVar = 'B';
        }
        else{
            breezeVar = '-';
        }

        if (hasStench == true) {
            stenchVar = 'S';
        }
        else{
            stenchVar = '-';
        }

        return "(" + Arrays.toString(coordinates) + breezeVar + stenchVar + ")";
    }
}