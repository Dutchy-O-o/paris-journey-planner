import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;

public class Operations {

    private DirectedGraph<Station> Graph;

    private String Start;
    private String Destination;
    private int intchoice;
    public Operations(String dataFilePath, String walkFilePath,String TestFilePath) {
        // Default filePath
        Graph = new DirectedGraph<>(); // Create an instance of the Graph class
        readFile(dataFilePath);
        addWalkEdges(walkFilePath);
        //ProcessTestFile(TestFilePath);
        getChoices();
        //Graph.DisplayEdges();
        //path(Start,Destination,intchoice);

        /*Station stat=getExistingStation("Nation");

        System.out.println(Graph.getNumberOfEdges());*/

        //Graph.shortestPath(getExistingStation("Voltaire (Léon Blum)"),getExistingStation("Edgar-Quinet"));

        //Graph.shortestPathWithMinStops(getExistingStation("Voltaire (Léon Blum)"),getExistingStation("Edgar-Quinet"));
    }

    private void ProcessTestFile(String filePath){
        try{

            Scanner fileScanner=new Scanner(new File(filePath));
            //Skipping Header
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            while (fileScanner.hasNextLine()) {
                String line=fileScanner.nextLine();
                String[] stations=line.split(",");
                String start=stations[0].trim();
                String dest=stations[1].trim();
                int choice=Integer.parseInt(stations[2].trim())+1;
                processUserChoices(start, dest, choice);
            }
        }catch(FileNotFoundException fileNotFoundException){
            System.err.println("File not found: "+ filePath);
            System.out.println("Please check your filePath");
        }
        }

    private void addWalkEdges(String filePath) {
        try {
            Scanner fileScanner = new Scanner(new File(filePath));

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] stations = line.split(",");

                Station StartStat = getExistingStation(stations[0].trim());
                Station DestStat = getExistingStation(stations[1].trim());

                // Check if the vertices exist before adding the edge
                if (StartStat != null && DestStat != null) {
                    // Add the edge with 0 weight
                    Graph.addEdge(StartStat, DestStat, 300);
                }
            }

            // Close the file scanner
            fileScanner.close();
        } catch (FileNotFoundException fileNotFoundException) {
            System.err.println("File not found: " + filePath);
            System.out.println("Please check your filePath");
        }
    }

    private void readFile(String filePath) {
        try {
            Scanner fileScanner = new Scanner(new File(filePath));
            fileScanner.nextLine(); // Skip the header line

            // Create an Alist to store lines
            ArrayList<String> lines = new ArrayList<>();

            // Read lines into the Alist
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }

            // Close the file scanner
            fileScanner.close();

            // Process the lines
            processLines(lines);

        } catch (FileNotFoundException fileNotFoundException) {
            System.err.println("File not found: " + filePath);
            System.out.println("Please check your filePath");
        }
    }

    private void processLines(ArrayList<String> lines) {
        // Initialize variables to keep track of the previous and current station
        Station previousStation = null;
        Station currentStation;

        // Process each line using a for loop
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] values = line.split(",");

            // Check if the station with the same name already exists
            currentStation = getExistingStation(values[1].trim());

            if (currentStation == null) {
                // If it doesn't exist, create a new instance
                currentStation = new Station();
                currentStation.setStopName(values[1].trim());
                Graph.addVertex(currentStation);
            }

            Integer arrivaltimecurrent = getStringAsInt(values[2].trim());
            Integer stopsequancecurrent = getStringAsInt(values[3].trim());
            Integer arrivaltimeprevious = 0;
            Integer stopsequanceprevious = 0;

            if (i > 0) {
                String prevLine = lines.get(i - 1);
                String[] prevValues = prevLine.split(",");
                arrivaltimeprevious = getStringAsInt(prevValues[2].trim());
                stopsequanceprevious = getStringAsInt(prevValues[3].trim());
                if(previousStation != null && Math.abs(stopsequancecurrent-stopsequanceprevious)==1){
                   currentStation.addRoute(values[5], previousStation.getStopName());
                   Graph.addEdge(previousStation, currentStation, Math.abs(arrivaltimecurrent - arrivaltimeprevious));
                }

            }
            previousStation = currentStation;

        }
    }
    
    public void getChoices() {
        Scanner scanner = new Scanner(System.in);

        String originStation = null;
        String destinationStation = null;
        int preference = 5;

        // Loop for the origin station input
        while (true) {
            try {
                System.out.println("Origin station: ");
                originStation = scanner.nextLine();
                Start=originStation;
                // Check if the given station name is valid
                if (getExistingStation(originStation)!=null) {
                    break; // Break the loop if the origin station is valid
                } else {
                    System.err.println("Invalid station name. Please enter a valid station name.");
                }
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a valid input.");
                // Clear the scanner buffer
                scanner.nextLine();
            }
        }

        // Loop for the destination station input
        while (destinationStation == null || (getExistingStation(destinationStation) == null)) {
            try {
                System.out.println("Destination station: ");
                destinationStation = scanner.nextLine();
                Destination=destinationStation;

                // Check if the given station name is valid
                if (getExistingStation(destinationStation) == null) {
                    System.err.println("Invalid station name. Please enter a valid station name.");
                    destinationStation = null;
                }
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a valid input.");
                // Clear the scanner buffer
                scanner.nextLine();
            }

        }

        // Loop for the preference input
        while (preference == 5) {
            try {
                System.out.println("Preference:");
                System.out.println("0. Fewer Stops");
                System.out.println("1. Minimum Time");
                preference = scanner.nextInt();
                intchoice=preference;

                if (preference < 0 || preference > 1) {
                    System.err.println("Invalid preference. Please enter a valid preference.");
                    preference = 5;
                }
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a valid input.");
                // Clear the scanner buffer
                scanner.nextLine();
            }
        }

        // Process user choices
        processUserChoices(originStation, destinationStation, preference);

        // Close the scanner to prevent resource leak
        scanner.close();
    }



    private Station getExistingStation(String stopName) {
        ArrayList<Station> vertices = Graph.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            Station existingStation = vertices.get(i);
            if (existingStation.getStopName().equals(stopName)) {
                return existingStation;
            }
        }
        return null; // No existing station found
    }

    private void processUserChoices(String originStation, String destinationStation, int preference) {
        System.out.println("User choices:");
        System.out.println("Origin station: " + originStation);
        System.out.println("Destination station: " + destinationStation);
        System.out.println("Preference: " + preference);

        if(intchoice == 1 )
            Graph.shortestPath(getExistingStation(originStation),getExistingStation(destinationStation));
        else if(intchoice == 0)
            Graph.shortestPathWithMinStops((getExistingStation(originStation)),getExistingStation(destinationStation));
    }


    public int getStringAsInt(String string){
        try{
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e){
            return 0;
        }
    }

}
