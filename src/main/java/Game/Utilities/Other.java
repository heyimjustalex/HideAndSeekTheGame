package Game.Utilities;

public class Other {

    public static Double calculateDistanceToNearestBasePoint(double pos_x, double pos_y) {

        double xb = pos_x;
        double yb = pos_y;

        double[][] baseCoords = {
                {4.0, 4.0},
                {4.0, 5.0},
                {5.0, 5.0},
                {5.0, 4.0}
        };
        double xa = baseCoords[0][0];
        double ya = baseCoords[0][1];

        double calculated_distance = Math.sqrt((Math.pow(xa - xb, 2) + Math.pow(ya - yb, 2)));

        for (int i = 1; i < baseCoords.length; i++) {
            xa = baseCoords[i][0];
            ya = baseCoords[i][1];
            double new_distance = Math.sqrt((Math.pow(xa - xb, 2) + Math.pow(ya - yb, 2)));
            calculated_distance = Math.min(calculated_distance, new_distance);
        }

        return calculated_distance;
    }

}
