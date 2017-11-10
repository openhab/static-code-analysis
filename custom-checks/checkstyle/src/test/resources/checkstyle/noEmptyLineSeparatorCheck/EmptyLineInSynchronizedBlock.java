public class TestExample {
    
    // empty line after class is allowed
    
    public void method(){
        synchronized (this) {
            
            // Important comment
            
            other();
        }
    }
    
    public void other() {
        System.out.println("other");
    }

}
