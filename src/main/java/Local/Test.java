package Local;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        LocalImp implementacija = new LocalImp();
        Scanner input = new Scanner(System.in);
        String answer = input.nextLine();
        implementacija.connectStorage(answer);
        implementacija.disconnectStorage();
    }
}