import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SendMessage {
    private String[] alert;

    public SendMessage(String[] alert) {
        this.alert = alert;
    }

    public void sendWeatherAlert(String phone,String location) {
        String ACCOUNT_SID = System.getenv("ACCOUNT_SID");
        String AUTH_TOKEN = System.getenv("AUTH_TOKEN");

//        try (BufferedReader br = new BufferedReader(new FileReader("twilio_key.txt"))) {
//            ACCOUNT_SID = br.readLine();
//            AUTH_TOKEN = br.readLine();
//        } catch (IOException e) {
//           // System.out.println(e);
//        }

        String alertMessage = "Current weather conditions at " + location;
        if (alert[0] == null || alert[1] == null) {
            alertMessage =  alertMessage +  " Weather is clear. " + alert[2];

        }else if (alert[0] != null || alert[1] !=null){
            alertMessage = alertMessage +  alert[0] + alert[1] + alert[2];
        }else if (alert[0] != null) {
            alertMessage = alertMessage + alert[0] + alert[2];
        } else if (alert[1] != null) {
            alertMessage = alertMessage+ alert[1] + alert[2];
        }
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message
                .creator(new PhoneNumber(phone), // to
                        new PhoneNumber("+12053748656"), // from
                        alertMessage)
                .create();
    }
}


