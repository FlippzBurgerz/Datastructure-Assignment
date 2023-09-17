import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;


/**
 * Retrieves temperature data from a weather station file.
 *
 * @author Franco Kovacevic
 * @author Carl-Johan Johansson
 * @Date 2022-03-20
 */
public class WeatherDataHandler
{
    private final TreeMap<LocalDateTime, ArrayList<String>> map = new TreeMap<>();
    private LocalDateTime submapDateTimeFrom;
    private LocalDateTime submapDateTimeTo;

    /**
     * Load weather data from file.
     *
     * @param filePath path to file with weather data
     */
    public void loadData(String filePath) throws IOException
    {
        for (String data : Files.readAllLines(Paths.get(filePath)))
        {
            String[] array = data.split(";");
            LocalDate date = LocalDate.parse(array[0]);
            LocalTime time = LocalTime.parse(array[1]);
            LocalDateTime localdatetime = LocalDateTime.of(date, time);

            ArrayList<String> list = new ArrayList<>();
            list.add(array[2]);
            list.add(array[3]);

            map.put(localdatetime, list);
        }
    }

    /**
     * Search for average temperature for all dates between the two dates (inclusive).
     * Result is sorted by date (ascending). When searching from 2000-01-01 to 2000-01-03
     * the result should be:
     * 2000-01-01 average temperature: 0.42 degrees Celsius
     * 2000-01-02 average temperature: 2.26 degrees Celsius
     * 2000-01-03 average temperature: 2.78 degrees Celsius
     *
     * @param dateFrom start date (YYYY-MM-DD) inclusive
     * @param dateTo   end date (YYYY-MM-DD) inclusive
     * @return average temperature for each date, sorted by date
     */
    public List<String> averageTemperatures(LocalDate dateFrom, LocalDate dateTo)
    {
        submapDateTimeFrom = LocalDateTime.of(dateFrom, LocalTime.MIDNIGHT);
        submapDateTimeTo = LocalDateTime.of(dateTo, LocalTime.MAX);

        CheckForErrors();

        Map<LocalDateTime, ArrayList<String>> dateSpan = map.subMap(submapDateTimeFrom,
                true, submapDateTimeTo, true);

        double sumOfTemperaturesPerDate = 0;
        int keysWithSameDate = 0;

        int iterationCounter = 0;
        LocalDate currentDate = dateFrom;
        ArrayList<String> returnList = new ArrayList<>();


        for (Map.Entry<LocalDateTime, ArrayList<String>> entry : dateSpan.entrySet())
        {
            iterationCounter++;

            //Basfallet: om currentDate är samma some dateTo och räknaren har gått lika många varv som
            //storleken på dateSpan så finns det inga fler nycklar kvar, och vi summerar vad som finns
            //i variablerna just nu och lägger till i returnList:
            if (entry.getKey().toLocalDate().equals(dateTo) && iterationCounter == dateSpan.size())
            {
                sumOfTemperaturesPerDate += Double.parseDouble(entry.getValue().get(0));
                keysWithSameDate++;

                returnList.add(currentDate + ": average temperature is " + String.format("%.2f",
                        (sumOfTemperaturesPerDate / keysWithSameDate)) + " degrees Celsius. Number of measurements: "
                        + keysWithSameDate);

                return returnList;
            } else if (entry.getKey().toLocalDate().equals(currentDate))
            {
                sumOfTemperaturesPerDate += Double.parseDouble(entry.getValue().get(0));
                keysWithSameDate++;
            } else if (entry.getKey().toLocalDate().equals(currentDate.plusDays(1)))
            {
                returnList.add(currentDate + ": average temperature is " + String.format("%.2f",
                        (sumOfTemperaturesPerDate / keysWithSameDate)) + " degrees Celsius. Number of measurements: "
                        + keysWithSameDate);

                keysWithSameDate = 1;
                currentDate = currentDate.plusDays(1);
                sumOfTemperaturesPerDate = Double.parseDouble(entry.getValue().get(0));
            }
        }
        return returnList;
    }

    /**
     * Search for missing values between the two dates (inclusive) assuming there should be 24 measurement
     * values for public List<String> averageTemperatures(LocalDate dateFrom, LocalDate dateTo)
     * Result is sorted by number of missing values (descending). When searching from
     * 2000-01-01 to 2000-01-03 the result should be:
     * 2000-01-02 missing 1 values
     * 2000-01-03 missing 1 values
     * 2000-01-01 missing 0 values
     *
     * @param dateFrom start date (YYYY-MM-DD) inclusive
     * @param dateTo   end date (YYYY-MM-DD) inclusive
     * @return dates with missing values together with number of missing values for each date, sorted by number of
     * missing values (descending)
     */
    public List<String> missingValues(LocalDate dateFrom, LocalDate dateTo)
    {
        submapDateTimeFrom = LocalDateTime.of(dateFrom, LocalTime.MIDNIGHT);
        submapDateTimeTo = LocalDateTime.of(dateTo, LocalTime.MAX);

        CheckForErrors();

        Map<LocalDateTime, ArrayList<String>> dateSpan = map.subMap(submapDateTimeFrom,
                true, submapDateTimeTo, true);

        int counter = 0;
        int iterationCounter = 0;
        LocalDate currentDate = dateFrom;
        //Här skapar vi en lista som ska kunna hålla objekt av typen MissingValues för att sedan sortera
        //dem i fallande ordning efter hur många mätvärden som saknas när vi itererat genom allting i dateSpan:
        ArrayList<MissingValues> missingValuesList = new ArrayList<>();

        for (Map.Entry<LocalDateTime, ArrayList<String>> entry : dateSpan.entrySet())
        {
            iterationCounter++;

            //Basfallet: Om datumet i LocalDateTime-objektet är samma som dateTo och loopräknaren är samma som
            //längden på dateSpan har vi kommit till sista raden i dateSpan, och vi vill då summera vad vi har i
            //variablerna för det aktuella datumet:
            if (entry.getKey().toLocalDate().equals(dateTo) && iterationCounter == dateSpan.size())
            {
                counter++;
                //Här skapar vi ett nytt objekt av klassen MissingValues som tar två argument i sin konstruktor:
                // 1) En konkatenerad sträng (den som ska skrivas ut av programmet i slutändan) och
                // 2) En int som berättar hur många mätvärden som saknas för den aktuella dagen som
                // strängutskriften syftar på:
                MissingValues missingValuesObject = new MissingValues((currentDate + ": missing " + (24 - counter)
                        + " values"), (24 - counter));
                missingValuesList.add(missingValuesObject);
            } else if (entry.getKey().toLocalDate().equals(currentDate))
            {
                counter++;
            } else if (entry.getKey().toLocalDate().equals(currentDate.plusDays(1)))
            {
                //Här skapar vi ett nytt objekt av klassen MissingValues som tar två argument i sin konstruktor:
                // 1) En konkatenerad sträng (den som ska skrivas ut av programmet i slutändan) och
                // 2) En int som berättar hur många mätvärden som saknas för den aktuella dagen som
                // strängutskriften syftar på:
                MissingValues missingValuesObject = new MissingValues((currentDate + ": missing " + (24 - counter)
                        + " values"), (24 - counter));
                missingValuesList.add(missingValuesObject);
                counter = 1;
                currentDate = currentDate.plusDays(1);
            }
        }

        ArrayList<String> returnList = new ArrayList<>();

        //Vi kan nu använda Collections.sort för att sortera listan med MissingValues-objekt efter antal saknade
        //mätvärden eftersom klassen MissingValues implementerar javas Comparable-interface:
        Collections.sort(missingValuesList);

        //Vi itererar sedan genom objektlistan där allting nu är sorterat i fallande ordning, och för varje objekt
        //lägger vi till dess strängutskrift i en ArrayList med strängar som vi sedan returnerar till Main():
        for (MissingValues obj : missingValuesList) {
            returnList.add(obj.printMissingValuesString());
        }
        return returnList;
    }


    /**
     * Search for percentage of approved values between the two dates (inclusive).
     * When searching from 2000-01-01 to 2000-01-03 the result should be:
     * Approved values between 2000-01-01 and 2000-01-03: 32.86 %
     *
     * @param dateFrom start date (YYYY-MM-DD) inclusive
     * @param dateTo   end date (YYYY-MM-DD) inclusive
     * @return period and percentage of approved values for the period
     */
    public List<String> approvedValues(LocalDate dateFrom, LocalDate dateTo)
    {
        submapDateTimeFrom = LocalDateTime.of(dateFrom, LocalTime.MIDNIGHT);
        submapDateTimeTo = LocalDateTime.of(dateTo, LocalTime.MAX);

        CheckForErrors();

        Map<LocalDateTime, ArrayList<String>> dateSpan = map.subMap(submapDateTimeFrom,
                true, submapDateTimeTo, true);

        double counter = 0;
        double approved = 0;

        int iterationCounter = 0;
        LocalDate currentDate = dateFrom;
        ArrayList<String> returnList = new ArrayList<>();

        for (Map.Entry<LocalDateTime, ArrayList<String>> entry : dateSpan.entrySet())
        {
            iterationCounter++;

            if (entry.getKey().toLocalDate().equals(dateTo) && iterationCounter == dateSpan.size())
            {
                counter++;
                if (entry.getValue().get(1).equals("G"))
                    approved++;

                returnList.add("Approved values between " + dateFrom + " and " + dateTo
                        + ": " + String.format("%.2f", ((approved / counter) * 100)) + " %");
                return returnList;
            } else if (entry.getKey().toLocalDate().equals(currentDate))
            {
                counter++;
                if (entry.getValue().get(1).equals("G"))
                    approved++;
            } else if (entry.getKey().toLocalDate().equals(currentDate.plusDays(1)))
            {
                counter++;
                if (entry.getValue().get(1).equals("G"))
                    approved++;
                currentDate = currentDate.plusDays(1);
            }
        }
        return returnList;
    }


    public void CheckForErrors()
    {
        LocalDateTime lastDateInTreemap = LocalDateTime.of(LocalDate.parse("2020-12-31"), LocalTime.MAX);
        boolean done = false;
        String input;
        Scanner scan = new Scanner(System.in);
        while(!done)
        {
            if(submapDateTimeTo.isBefore(submapDateTimeFrom) || submapDateTimeFrom.isAfter(lastDateInTreemap)
                    || submapDateTimeTo.isAfter(lastDateInTreemap))
            {
                System.out.println("Please re-enter correct dates:");
                System.out.println("Start date (will be included)\nEnter date (YYYY-MM-DD):");
                input = scan.nextLine().trim();

                try
                {
                    submapDateTimeFrom = LocalDateTime.of(LocalDate.parse(input), LocalTime.MIDNIGHT);
                }
                catch (DateTimeParseException e)
                {
                    System.out.println("Invalid date");
                }

                System.out.println("End date (will be included)\nEnter date (YYYY-MM-DD):");
                input = scan.nextLine().trim();
                try
                {
                    submapDateTimeTo = LocalDateTime.of(LocalDate.parse(input), LocalTime.MAX);
                    done = true;
                }
                catch (DateTimeParseException e)
                {
                    System.out.println("Invalid date");
                }
            }
            else
                done = true;
        }
    }
}