import java.util.ArrayList;

public class Station {

    private ArrayList<Route> lines;
    private String stopName;

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public ArrayList<Route> getLines() {
        return lines;
    }

    public void addRoute(String route, String relatedStation1) {
        if (lines == null) {
            lines = new ArrayList<>();
        }

        // Check if the route already exists
        Route currentRoute = linesGetRouteByName(route);
        if (currentRoute== null) {
            lines.add(new Route(route, relatedStation1));
        }else{
            currentRoute.addStation(relatedStation1);
        }
    }

    private Route linesGetRouteByName(String route){
        for (int i = 0; i < lines.size(); i++) {
            if((lines.get(i).getRoute()).equals(route)){
                return lines.get(i);
            }
        }
        return null;
    }

    public String getCurrentLine(String prevName) {
        for (Route route : lines) {
            if (route.isIn(prevName)) {
                return route.getRoute();
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Station otherStation = (Station) obj;

        return this.getStopName().equals(otherStation.getStopName());
    }

    @Override
    public String toString() {
        return stopName;
    }

    private class Route {
        String route;
        ArrayList<String> relatedStations = new ArrayList<>();

        // Constructor adds one station
        Route(String route, String relatedStation1) {
            this.route = route;
            relatedStations.add(relatedStation1);
        }

        public void addStation(String relatedStation2) {
            if(!relatedStations.contains(relatedStation2)){
                relatedStations.add(relatedStation2);

            }
        }

        public boolean isIn(String station) {
            return relatedStations.contains(station);
        }

        public String getRoute() {
            return route;
        }
    }
}
