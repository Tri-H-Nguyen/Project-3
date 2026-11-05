import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip;
	
	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	// returns time
	public int getTime() {
		return time;
	}
	
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}
	
	// uses the haversine formula for great sphere distance between two points
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}
	
	// finds the total distance traveled over the trip
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	public String toString() {
		
		return null;
	}

	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}
	
	//Code To Implement
	/**
	 * Finds all the stop zones in the trip ArrayList using the first heuristic method.
	 * Returns the amount of stops in trip ArrayList, initializes movingTrip, and fills movingTrips with all non-stop TripPoints
	 * @return
	 */
	public static int h1StopDetection() {
		//remove later
		try {
			readFile("triplog.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double dt = 0.6;
		movingTrip = new ArrayList<>();
		movingTrip.add(trip.get(0));
		
		int stopCount = 0;
		
		//Iterates through trip ArrayList
		for (int i = 1; i < trip.size(); i++) {
			
			/*
			 * Compares the haversineDistance between the current TripPoint and the previous tripPoint
			 * If the distance is within the displacement threshold, increment stopCount by one
			 * Otherwise, add the current TripPoint to movingTrip
			 */
			if (haversineDistance(trip.get(i - 1), trip.get(i)) <= dt) {
				stopCount++;
			} else {
				movingTrip.add(trip.get(i));
			}
		}
		
		return stopCount;
	}
	
	/**
	 * Finds groups of three or more that are a radius distance from each other using the second heuristic method.
	 * Returns the amount of stops that are in stopZones in trip ArrayList
	 * Initializes movingTrip, and fills it with all non stop TripPoints.
	 * @return
	 */
	public static int h2StopDetection() {
		
		double radius = 0.5;
		movingTrip = new ArrayList<>();
		
		int stopCount = 0;
		ArrayList<TripPoint> stopZone = new ArrayList<>();

		//Iterates through all TripPoints in trip
		for (TripPoint tp : trip) {
			
			/*
			 *Adds the first TripPoint to the initialized stopZone ArrayList.
			 * Then skips to the second TripPoint.
			 */
			if (stopZone.isEmpty()) {
				stopZone.add(tp);
				continue;
			}
			
			/*
			 *If the current TripPoint is within radius haversineDistance from any TripPoint
			 * currently in the stopZone, add the TripPoint to the stopZone.
			 */
			for (TripPoint stops : stopZone) {
				if (haversineDistance(tp, stops) <= radius) {
					stopZone.add(tp);
					break;
				}
			}
			
			/*
			 *If the TripPoint was not added : 
			 *    add the stopZone size to stopCount if >= 3
			 * OR add the TripPoints from the stopZone to movingTrip.
			 * 
			 * Clears the stopZone and adds the current TripPoint to the stopZone to check if
			 * the TripPoint is apart of different group.
			 */
			if(!stopZone.contains(tp)) {
				if (stopZone.size() >= 3) {
					stopCount += stopZone.size();
				} else {
					movingTrip.addAll(stopZone);
				}
				stopZone.clear();
				stopZone.add(tp);
				
			}
		}
		
		/* 
		 *If there are left over TripPoints in stopZone, they are considered as moving points
		 *and are added to movingTrip. 
		 */
		movingTrip.addAll(stopZone);
		
		return stopCount;
	}
		
	/**
	 * Returns the total time spent moving in the trip ArrayList in units of hours.
	 * @return
	 */
	public static double movingTime() { 
		double totalMovingTime = 0; 
		
		//Iterates through all of trip ArrayList
		for (int i = 1; i < trip.size(); i++) { 
			
			/*
			 * If movingTrip has the same TripPoint as the i index in trip, add the difference
			 * between the i index in trip and the i - 1 index.
			 * Then add the difference to the total.
			 */
			if (movingTrip.contains(trip.get(i))) { 
				totalMovingTime += trip.get(i).getTime() - trip.get(i - 1).getTime(); 
			} 
		} 
		
		//Returns the totalMovingTime in hours
		return totalMovingTime / 60;
	}
	
	/**
	 * Returns the total time spent stopped in the trip ArrayList in units of hours.
	 * @return
	 */
	public static double stoppedTime() { 
		
		/*
		 * The time spent stopped is the time spend not moving. So we return the difference between
		 * the total time and the moving time. Both methods are in units of hours, so we do not need to convert.
		 */
		return totalTime() - movingTime();
	} 
	
	/**
	 * Returns the total distance between each moving point divided by the total moving time.
	 * @return
	 */
	public static double avgMovingSpeed() {
		double totalMovingDistance = 0.0;
		
		//Iterates through movingTrip and finds the sum of distance between each moving point
		for (int i = 0; i < movingTrip.size() - 1; i++) {
			totalMovingDistance += haversineDistance(movingTrip.get(i), movingTrip.get(i + 1));
		}
		
		//Returns the Average Moving Speed.
		return totalMovingDistance / movingTime();
	}
	
	/**
	 * Returns a copy of movingTrip ArrayList.
	 * @return
	 */
	public static ArrayList<TripPoint> getMovingTrip() {
		return new ArrayList<TripPoint>(movingTrip);
	}

}
