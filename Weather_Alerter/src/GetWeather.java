import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetWeather {
    public String[] location;

    public GetWeather(String location) {
        this.location = locationToCordinates(location);

    }

    public String[] locationToCordinates(String location) {
        location = location.replace(" ","+"); //remove spaces for api call
        String apiKey = System.getenv("apiKeyLocation");
//        try (BufferedReader br = new BufferedReader(new FileReader("mapKey.txt"))){
//            apiKey = br.readLine();
//        } catch (IOException e) {
//            //apiKey = null;
//            //System.out.println("Please obtain APIkey and store in file mapKey.txt");
//        }

        String urlString = "http://www.mapquestapi.com/geocoding/v1/address?key=" + apiKey + "&location="+location;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            BufferedReader rd = new BufferedReader((new InputStreamReader(connection.getInputStream())));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch(MalformedURLException e){
            return null;
        } catch (IOException e){
            return  null;
        }
        Pattern latitude = Pattern.compile("((?<=lat\":).\\d*.\\d*)");
        Matcher m = latitude.matcher(result);
        m.find();
        String lat = m.group(0);

        Pattern longitude = Pattern.compile("(?<=lng\":).\\d*.\\d*");
        Matcher m2 = longitude.matcher(result);
        m2.find();
        String lond = m2.group(0);
        String [] latitudeAndLongitude = {lat,lond};

        return latitudeAndLongitude;
    }

    public String[] currentWeather(String lat, String lond) {
        /*/
        Returns a string of weather information for a given lat and longitude
         */
        String apiKey = System.getenv("apiKeyWeather");
        //System.out.println(lat);
//        try (BufferedReader br = new BufferedReader(new FileReader("weather_key.txt"))) {
//            apiKey = br.readLine();
//        } catch (IOException e) {
//            //apiKey = null;
//           // System.out.println("Please obtain APIkey and store in file weather_key.txt");
//        }

        String urlString = "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon="
                + lond + "&" + "exclude=minutely,daily&units=imperial&appid=" + apiKey;
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            BufferedReader rd = new BufferedReader((new InputStreamReader(connection.getInputStream())));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            System.out.println(result);
            Gson g = new Gson();
            String resultJson = g.toJson(result);
            Map[] hourlyWeather = stringToHashMap(resultJson);
            return checkWeather(hourlyWeather[0], hourlyWeather[1]);
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public Map[] stringToHashMap(String source) {
        /* This takes the result of API call and puts data into hashmap.
         */
        Map<Long, String> weatherData = new HashMap<Long, String>();
        Map<Long, Double> temperatureData = new HashMap<Long, Double>();

        String[] parts = source.split("},");
        Pattern datetime = Pattern.compile("(\\d*?)\\,");
        String tempPattern = "(?<=\\\\" + '"' + "temp\\\\" + '"' + ":)[^\\,]+\\d";
        Pattern temp = Pattern.compile(tempPattern);
        String weatherPattern = "((?<=\\\\\"main\\\\\":\\\\\").*?([A-z])(?=\\\\))";
        Pattern weather = Pattern.compile((weatherPattern));
        for (int i = 2; i < parts.length; i++) {
            String current = parts[i];

            //Get time
            Matcher m = datetime.matcher(current);
            m.find();
            long time = Long.parseLong(m.group(1));

            //Get temperature
            m = temp.matcher(current);
            m.find();
            Double temperature = Double.parseDouble(m.group(0));

            // Get weather
            m = weather.matcher(current);
            m.find();
            String weatherCurrent = m.group(1);
            weatherData.put(time, weatherCurrent);
            temperatureData.put(time, temperature);
        }
        Map[] result = {weatherData, temperatureData};
        return result;
    }

    public String[] checkWeather(Map<Long, String> weatherData, Map<Long, Double> temperatureData) {
        long currentTime = System.currentTimeMillis() / 1000L;
        String messageWeather = null;
        String messageTemp = null;
        String deltaTemp = null;

        //Check for rain.
        Set<Long> times = new HashSet<Long>();
        for (Map.Entry<Long, String> entry : weatherData.entrySet()) {
            if (entry.getValue().contains("Rain")) {
                times.add(entry.getKey());
            }
        }
        if (times.isEmpty() == false) {
            Long minTimeEpoch = Collections.min(times);
            Date minTime = new Date(minTimeEpoch * 1000);
            messageWeather = " Rain is possible at: " + minTime;
        }

        //Check for snow.
        Set<Long> times_snow = new HashSet<Long>();
        for (Map.Entry<Long, String> entry : weatherData.entrySet()) {
            if (entry.getValue().contains("Snow")) {
                times_snow.add(entry.getKey());
            }
        }
        if (times_snow.isEmpty() == false) {
            Long minTimeEpoch = Collections.min(times_snow);
            Date minTime = new Date(minTimeEpoch * 1000);
            messageWeather = " Snow is possible at: " + minTime;
        }

        // Check for temperatures
        Set<Long> temperatures = new HashSet<Long>();
        Double min = 100.0;
        Long minTime = null;
        Long maxTime =null;
        Double max = 0.0;
        for (Map.Entry<Long, Double> entry : temperatureData.entrySet()) { //Future feature, custom input.

            if (temperatureData.get(entry.getKey()) < min){
                min = temperatureData.get(entry.getKey());
                minTime = entry.getKey();
            }
            if (temperatureData.get(entry.getKey()) > max){
                max = temperatureData.get(entry.getKey());
                maxTime = entry.getKey();
            }

            if (temperatureData.get(entry.getKey()) < 50 || temperatureData.get(entry.getKey()) > 80) {
                temperatures.add(entry.getKey());
            }
        }
        if (temperatures.isEmpty() == false) {
            Long minTimeEpocTemp = Collections.min(temperatures);
            Date minTimeTemp = new Date(minTimeEpocTemp * 1000);
            messageTemp = ". Temperature warning of: " + temperatureData.get(minTimeEpocTemp) +
                    " at " + minTimeTemp;
        }
        double delta = max-min;
        delta = Math.round(delta);

            Date maxTimeReadable = new Date(maxTime * 1000);
            Date minTimeReadable = new Date(minTime * 1000);
            deltaTemp = ". High is " + max + " degress occuring at " + maxTimeReadable  + " and low is " + min +
                   " occuring at "  + minTimeReadable + " with a difference of " + delta + " degrees.";


        String[] allMessages = {messageWeather,messageTemp,deltaTemp};
        return allMessages;
    }
}
