public class TestExample {

    public int switchWithoutBraces(int x) {
        switch (x) {
            case 1:
                return 1;
            default:
                return 0;
        }
    }

    public void methodAfterSwitch() {

        other();

    }

    public void other() {
        System.out.println("other");
    }
}
