package Local;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        LocalImp implementacija = new LocalImp();

        Scanner input = new Scanner(System.in);
        String answer = input.nextLine();

        implementacija.connectStorage(answer);
        //implementacija.deleteFolder("C:\\Users\\KYGAS\\Desktop\\Test\\Test1\\Test123");
        //System.out.println(implementacija.createFile("haha", ""));
        //System.out.println(implementacija.uploadFile("C:\\Users\\KYGAS\\Desktop\\Test\\Test2\\Aca.txt",""));
        //System.out.println(implementacija.uploadFile("C:\\Users\\KYGAS\\Desktop\\Test\\Test2\\Baca.txt",""));
        //System.out.println(implementacija.uploadFile("C:\\Users\\KYGAS\\Desktop\\Test\\Test2\\Maca.txt",""));

        System.out.println(implementacija.downloadFolder("Test123","C:/Users/KYGAS/Desktop/Test/Test2"));

        implementacija.disconnectStorage();
    }
}
//C:\Users\KYGAS\Desktop\Test\Test1\Aca
//"C:\Users\KYGAS\Desktop\Test\Test1"