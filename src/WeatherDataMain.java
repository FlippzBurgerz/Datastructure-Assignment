import java.io.IOException;

/**
 * Simple application for retrieving and presenting temperature 
 * data from a weather station file.
 */
public class WeatherDataMain {

	/**
	 * Program entry point.
	 * 
	 * @param args optional argument for path to weather data file
	 */
	public static void main(String[] args)
	{
		WeatherDataHandler weatherData = new WeatherDataHandler();
		String fileName = "smhi-opendata.csv";
		if(args.length > 0)
		{
			fileName = args[0];
		}		
		try
		{
			weatherData.loadData(fileName);
			new WeatherDataUI(weatherData).startUI();
		} catch (Exception e)
		{
				String TEXT_RESET = "\u001B[0m";
				String TEXT_RED = "\u001B[31m";
				System.out.println(TEXT_RED + "Kunde inte parsa instruktionen." + TEXT_RESET);
				new WeatherDataUI(weatherData).startUI();
		}		
	}
}