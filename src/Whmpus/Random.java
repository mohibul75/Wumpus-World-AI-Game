package Whmpus;

public class Random {

    public Random XObj()
    {
        fName = "dip";
        return  this;
    }

    public String fName="ifti", lName="pagla";

    public void setfName(String fName) {
        this.fName = fName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    @Override
    public String toString() {
        return "Random{" +
                "fName='" + fName + '\'' +
                ", lName='" + lName + '\'' +
                '}';
    }
}
