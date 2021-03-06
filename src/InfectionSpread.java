// Arguments:
// N population size (def. 40)
// S probability that a sick person infects someone not immune (%, def. 0)
// minDays minimal sickness duration in days (def. 6)
// maxDays maximal sickness duration in days (def. 9)
// L probability that the infected person dies (def. 0)
// patients0 number of initially infected people (def. 1)
// placement0 placement of initially infected people (def. 20,20)
// output optional specification of output lines:
//   1 dailyInfected
//   2 dailyDead
//   3 dailyCured
//   4 currentlyInfected
//   5 totalInfected
//   6 totalDead
// Example:
// java InfectionSpread 100 10 6 9 0 1 15,15


import java.util.ArrayList;
import java.util.Random;

public class InfectionSpread {

    private static int N = 40;
    private static int S;
    private static int minDays = 6;
    private static int maxDays = 9;
    private static int L = 0;
    private static int patients0 = 1;
    private static ArrayList<int[]> toInfect;
    private static int date;
    private static person[][] population;
    private static boolean done = false;
    private static int dailyInfected;
    private static int totalInfected;
    private static int dailyDead;
    private static int totalDead;
    private static int dailyCured;
    private static int totalCured;
    private static int currentlyInfected;
    private static int totalLucky;
    private static int output;

    // Subclass for each unit of the population
    static class person {

        // 0, 1, 2, 3 <---> uninfected, sick, dead, cured&immune
        private int status;
        private int infectionDate;
        private int infectionLength;

        person() {
            this.status = 0;
        }

        int getStatus() {
            return status;
        }

        void infect() {
            if (this.getStatus() == 0) {
                this.status = 1;
                this.infectionDate = date;

                // Using the rectangular distribution decide the duration of the sickness
                Random rand = new Random();
                this.infectionLength = minDays + rand.nextInt(maxDays - minDays + 1);

                dailyInfected++;
                totalLucky--;
            }
        }

        // Each day a person is sick person.sick() is run
        // Returns 0 if dies, 1 otherwise
        int sick() {

            // A person is cured if they haven't died throughout the disease period
            if ((date - this.infectionDate) > this.infectionLength) {
                this.cure();
                return 3;
            }

            // A person has a probability L to die each day they are sick
            if ((date - this.infectionDate) > minDays) {

                // Using probability L decide if the person dies today
                Random rand = new Random();
                int pickedNumber = rand.nextInt(100) + 1;
                if (pickedNumber <= L) {
                    this.die();
                    return 2;
                }
            }

            return 1;
        }

        void die() {
            this.status = 2;
            dailyDead++;
        }

        void cure() {
            this.status = 3;
            dailyCured++;
        }
    } //person

    // Parse placement of initially infected in the population
    private static ArrayList<int[]> placement0(String placement0) {
        String[] split = placement0.trim().split(",");
        ArrayList<int[]> placements = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            int[] coord = {Integer.parseInt(split[i]), Integer.parseInt(split[++i])};
            placements.add(coord);
        }
        if (placements.size() != patients0) {
            throw new IllegalArgumentException("check infection placement!");

        }
//        for (int[] arr : placements
//                ) {
//            System.out.println(arr[0]);
//            System.out.println(arr[1]);
//        }
        return placements;
    }

    private static void init(int N) {
        date = 0;
        totalInfected = currentlyInfected = patients0;
        totalLucky = N * N;
//        System.out.println("luck" + totalLucky);

        // Initialize the population matrix
        population = new person[N][N];
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                population[row][col] = new person();
            }
        }

        // Place intially infected in the population matrix
        for (int[] coord : toInfect
                ) {
            population[coord[0]][coord[1]].infect();
        }
        toInfect.clear();

        System.out.println("Population initialized");
//        System.out.println("total inf" + totalInfected);
//        System.out.println("curr inf" + currentlyInfected);
//        System.out.println("patients0" + patients0);
//        System.out.println("luck" + totalLucky);
    }

    // A day of simulation
    private static void day() {
//        System.out.println("total inf" + totalInfected);
//        System.out.println("curr inf" + currentlyInfected);
//        System.out.println("patients0" + patients0);
//        System.out.println("luck" + totalLucky);
        date++;
        dailyInfected = 0;
        dailyCured = 0;
        dailyDead = 0;

        // Go through all population
        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {
                if (population[row][col].getStatus() == 1) {

                    // Determine neighbours of sick individuals
                    if (population[row][col].sick() == 1) {
                        int[] coord = {row, col};
                        toInfect.add(coord);
                    }
                }
            }
        }

        // Spread the disease
        spread();

        toInfect.clear();
        totalCured = totalCured + dailyCured;
        totalInfected = totalInfected + dailyInfected;
        totalDead = totalDead + dailyDead;
        currentlyInfected = currentlyInfected - dailyDead - dailyCured + dailyInfected;
        if (currentlyInfected == 0) {
            done = true;
        }

        // Print status
        if (output != -1) {
            report();
        }
    }

    // Prints status
    private static void report() {
        System.out.println("...");
        System.out.println("Infection spread report, day " + date);
        if (output == 0 || output > 6) {
            System.out.println("Infected today..............." + dailyInfected);
            System.out.println("Died today..................." + dailyDead);
            System.out.println("Recovered today.............." + dailyCured);
            System.out.println("Ill today:..................." + currentlyInfected);
            System.out.println("Total infected to date......." + totalInfected);
//        System.out.println("Total cured to date.........." + totalCured);
            System.out.println("Total died to date..........." + totalDead);
//        System.out.println("Total lucky.................." + totalLucky);
        } else {
            switch (output) {
                case 1:
                    System.out.println("Infected today..............." + dailyInfected);
                    break;
                case 2:
                    System.out.println("Died today..................." + dailyDead);
                    break;
                case 3:
                    System.out.println("Recovered today.............." + dailyCured);
                    break;
                case 4:
                    System.out.println("Ill today:..................." + currentlyInfected);
                    break;
                case 5:
                    System.out.println("Total infected to date......." + totalInfected);
                    break;
                case 6:
                    System.out.println("Total died to date..........." + totalDead);
                    break;
            }
        }
    }

    // Spreads the disease
    private static void spread() {
        for (int[] coord : toInfect
                ) {
            infectAround(coord[0], coord[1]);
        }
    }

    // Tries to infect all neighbours
    private static void infectAround(int row, int col) {
        // upper row
        if (row - 1 >= 0) {
            if (col - 1 >= 0) {
                infect(row - 1, col - 1);
            }
            infect(row - 1, col);
            if (col + 1 < N) {
                infect(row - 1, col + 1);
            }
        }
        // same row
        if (col - 1 >= 0) {
            infect(row, col - 1);
        }
        if (col + 1 < N) {
            infect(row, col + 1);
        }
        // lower row
        if (row + 1 < N) {
            if (col - 1 >= 0) {
                infect(row + 1, col - 1);
            }
            infect(row + 1, col);
            if (col + 1 < N) {
                infect(row + 1, col + 1);
            }
        }
    }

    // Tries to infect an individual
    private static void infect(int row, int col) {

        // Using probability S decide if the person gets infected
        Random rand = new Random();
        int pickedNumber = rand.nextInt(100) + 1;
        if (pickedNumber <= S) {
            population[row][col].infect();
        }
    }

    // Run the simulation until no infected individuals left
    private static void loop() {
        while (!done) {
            day();
        }
    }

    public static void main(String[] args) {
        if (args.length == 8) {
            output = Integer.parseInt(args[7]);
        }

        if (args.length >= 7) {
            N = Integer.parseInt(args[0]);
            S = Integer.parseInt(args[1]);
            minDays = Integer.parseInt(args[2]);
            maxDays = Integer.parseInt(args[3]);
            L = Integer.parseInt(args[4]);
            patients0 = Integer.parseInt(args[5]);
            toInfect = placement0(args[6]);
        }

        // Default args
        else {
            int[] initialPl = new int[2];
            initialPl[0] = initialPl[1] = 20;
            toInfect = new ArrayList<>();
            toInfect.add(initialPl);
        }
        init(N);
        loop();
    }
} //InfectionSpread
