public class TestExample {
    
    // empty line after class is allowed
    
    public void method(){
        int month = 2;
        
        java.util.ArrayList<String> futureMonths =
            new java.util.ArrayList<String>();
        
        switch (month) {
            
            case 1:  {
                futureMonths.add("January");
                int other = 4;
                
            }
            case 2:  
            
                int some = 5;
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
