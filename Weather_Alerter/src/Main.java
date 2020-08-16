import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner data = new Scanner(System.in);
        System.out.println("Please enter location.");
        String location = data.nextLine();
        System.out.println("Please enter phone numbber to recieve alert.");
        String phone = data.nextLine();

        GetWeather weather = new GetWeather(location);
        String[] weatherAndTemp = weather.currentWeather(weather.location[0],weather.location[1]);

        SendMessage message = new SendMessage(weatherAndTemp);
        message.sendWeatherAlert(phone,location);

    }
}
