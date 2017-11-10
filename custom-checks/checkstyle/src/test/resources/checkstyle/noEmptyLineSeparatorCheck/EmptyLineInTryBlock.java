public class TestExample {
    
    // empty line after class is allowed
    
    public void method(){
        try {
            System.out.println("hi");
            
            return;
        } catch (RuntimeException e){
            System.out.println("got it");
        } finally {
            
            System.out.println("finally");
        }
        
        
        try {
            
            int i = Integer.parseInt("32");
            
        } finally {
            System.out.println("Done");
        }
    }

}
