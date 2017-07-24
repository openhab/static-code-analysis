import java.util.ArrayList;

public class TestExample {
    
    // empty line after class is allowed
    
    public void method(){
        int month = 2;
        
        ArrayList<String> futureMonths = new ArrayList<String>();
        switch (month) {
            case 1:  {
                
                futureMonths.add("January");
                int other = 4;
            }
            case 2:  { futureMonths.add("February");}
            case 3:  { 
                
            }
            case 4:  { futureMonths.add("April");}
            case 5:  { futureMonths.add("May");
                     break;
            }
            default: break;
        }
    }

}
