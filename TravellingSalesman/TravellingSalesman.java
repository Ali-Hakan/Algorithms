/**
 * The program determines the most optimal route between indicated coordinates with an identified beginning point.
 * @author Ali Hakan Ozen
 * @since Date: 18.04.2022
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class MigrosDelivery {
	
	/**
	 * Finds the most optimal route using related methods.
	 * @param args; main input arguments are not used.
	 * @throws FileNotFoundException; in case of written input file being not found.
	 */
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("Enter the name of the input file that is requested to be processed: ");
		Scanner input = new Scanner(System.in);
		String fileName = input.next();
		input.close();
		double[][] coordinates = getCoordinates(fileName);
		int[] route = nearestNeighbourAlgorithm(coordinates);
		int[] optimizedRoute = twoOptAlgorithm(route, coordinates);
		int[] simulatedRoute = simulatedAnnealing(optimizedRoute, coordinates);
		System.out.println("Shortest Route: " + Arrays.toString(simulatedRoute));
		System.out.println("Distance: " + calculateTotalDistance(simulatedRoute, coordinates));
	}
	
	/**
	 * Arranges given coordinates to make them processable.
	 * @param fileName; name of the file entered as in input by user.
	 * @return coordinates
	 * @throws FileNotFoundException; in case of written input file being not found.
	 */
	public static double[][] getCoordinates(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.printf("%s could not be found.", fileName);
			System.exit(1);
		}
		Scanner countInputFile = new Scanner(file);
		int amountCoordinates = 0;
		while (countInputFile.hasNextLine()) {
			countInputFile.nextLine();
			amountCoordinates++;
		}
		countInputFile.close();
		Scanner inputFile = new Scanner(file);
		double[][] coordinates = new double[amountCoordinates][];
		for (int i = 0; inputFile.hasNextLine(); i++) {
			String line = inputFile.nextLine();
			String[] rawCoordinates = line.split(",");
			coordinates[i] = new double[rawCoordinates.length];
			for (int j = 0; j < rawCoordinates.length ; j++) {
				if (rawCoordinates[j].contains("Migros"))
					coordinates[i][j] = 0.0;
				else 
				coordinates[i][j] = Double.parseDouble(rawCoordinates[j]);
			}
		}
		inputFile.close();
		return coordinates;
	}
	
	/**
	 * Finds the nearest unvisited point each time to create an optimal route.
	 * @param coordinates; coordinates of the points that a route is created with.
	 * @return route
	 */
	public static int[] nearestNeighbourAlgorithm(double[][] coordinates) {
		int originCoordinate = 0;
		for (int i = 0; i < coordinates.length; i++)
			for (int j = 0; j < coordinates[i].length; j++)
				if (coordinates[i][j] == 0.0) {
					originCoordinate = i;
					break;
				}
		int[] route = new int[coordinates.length + 1];
		route[0] = originCoordinate;
		route[coordinates.length] = originCoordinate;
		int amountVisitedCoordinates = 1;
		do {
		double shortestDistance = 0;
		boolean visitedCoordinate = false;
		for (int i = 0; i < coordinates.length; i++) {
			for (int j = 0; j < amountVisitedCoordinates; j++)
				if (route[j] == i) {
					visitedCoordinate = true;
					break;
				}
			if (visitedCoordinate) {
				visitedCoordinate = false;
				continue;
			}
			double rawDistanceX = Math.pow(Math.abs(coordinates[route[amountVisitedCoordinates - 1]][0] - coordinates[i][0]), 2.0);						
			double rawDistanceY = Math.pow(Math.abs(coordinates[route[amountVisitedCoordinates - 1]][1] - coordinates[i][1]), 2.0);
			double distance = Math.sqrt(rawDistanceY + rawDistanceX);
			if (shortestDistance == 0 || shortestDistance > distance) {
				shortestDistance = distance;
				originCoordinate = i;
			}
		}
		amountVisitedCoordinates++;
		route[amountVisitedCoordinates - 1] = originCoordinate;
		} while (amountVisitedCoordinates < coordinates.length);
		return route;
	}
	
	/**
	 * Calculates the total distance of a route.
	 * @param route; route that is created by an algorithm.
	 * @param coordinates; coordinates of the points that a route is created with.
	 * @return total distance
	 */
	public static double calculateTotalDistance(int[] route, double[][] coordinates) {
		double totalDistance = 0;
		for (int i = 0; i < route.length - 1; i++) {
			double rawDistanceX = Math.pow(Math.abs(coordinates[route[i]][0] - coordinates[route[i+1]][0]), 2.0);
			double rawDistanceY = Math.pow(Math.abs(coordinates[route[i]][1] - coordinates[route[i+1]][1]), 2.0);
			double rawDistance = Math.sqrt(rawDistanceY + rawDistanceX);
			totalDistance += rawDistance;
		}
		return totalDistance;
	}
	
	/**
	 * Optimizes the given route by reordering it to ensure it does not cross over itself.
	 * @param existingRoute; route created by an algorithm, nearest neighbour in our case.
	 * @param coordinates; coordinates of the points that a route is created with.
	 * @return optimized route
	 */
	public static int[] twoOptAlgorithm(int[] existingRoute, double[][] coordinates) {
		int[] newRoute = new int[existingRoute.length];
		boolean routeFound = false;
		do {
		double totalDistance = calculateTotalDistance(existingRoute, coordinates);
		for (int i = 1; i <= existingRoute.length - 3; i++) {
			if (routeFound) {
				routeFound = false;
				break;
			}
			for (int j = i + 1; j <= existingRoute.length - 2; j++) {
				for (int k = 0; k < i; k++)
					newRoute[k] = existingRoute[k];
				for (int l = i; l <= j; l++)
					newRoute[l] = existingRoute[j - l + i];
				for (int m = j + 1; m <= existingRoute.length - 1; m++)
					newRoute[m] = existingRoute[m];
				double newTotalDistance = calculateTotalDistance(newRoute, coordinates);
				if (newTotalDistance < totalDistance) {
					existingRoute = newRoute.clone();
					routeFound = true;
					break;
				}
			}
		}
		} while (existingRoute == newRoute);
		return existingRoute;
	}
	
	/**
	 * Optimizes the given route probabilistically by approximating the most optimum route.
	 * @param existingRoute; route created by an algorithm, 2-opt in our case.
	 * @param coordinates; coordinates of the points that a route is created with.
	 * @return best route
	 */
	public static int[] simulatedAnnealing (int[] existingRoute, double[][] coordinates) {
		double temperature = 1000;
		double coolingFactor = 0.9999999;
		int[] bestRoute = existingRoute.clone();
		for (double t = temperature; t > 1; t *= coolingFactor) {
            int[] newRoute = new int[existingRoute.length];
            newRoute = existingRoute.clone();
            int point1 = (int) (Math.random() * (newRoute.length - 2) + 1);
            int point2 = (int) (Math.random() * (newRoute.length - 2) + 1);
            int tempPoint = newRoute[point1];
            newRoute[point1] = newRoute[point2];
            newRoute[point2] = tempPoint;
            if (Math.random() < probability(calculateTotalDistance(existingRoute, coordinates), calculateTotalDistance(newRoute, coordinates), t)) 
            	existingRoute = newRoute.clone();
            if (calculateTotalDistance(existingRoute, coordinates) < calculateTotalDistance(bestRoute, coordinates)) 
            	bestRoute = existingRoute.clone();
        }
		return bestRoute;
	}
	
	/**
	 * Determines whether to accept new route within probabilistic approach. 
	 * @param existingDistance; distance of a route created by other algorithms.
	 * @param newDistance; distance of a route created by shuffling.
	 * @param temperature; affects the acceptance criteria of newly created route. 
	 * @return determined probability if new route is not better than existing route, 1 if new route is better than existing route.
	 */
	public static double probability(double existingDistance, double newDistance, double temperature) {
		if (newDistance < existingDistance) 
			return 1;
        return Math.exp((existingDistance - newDistance) / temperature);
	}
}

