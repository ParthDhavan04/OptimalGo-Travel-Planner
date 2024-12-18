package com.Minor.OptimalGo.route;

import com.Minor.OptimalGo.graph.BFS;
import com.Minor.OptimalGo.graph.BellmanFord;
import com.Minor.OptimalGo.graph.Dijkstra;
import com.Minor.OptimalGo.graph.Graph;

import java.util.Arrays;
import java.util.Scanner;
public class RouteService {
    private final Graph graph;
    private final Dijkstra dij;
    private final BellmanFord bellmanFord;
    private final BFS bfs;
    private final Scanner sc;

    public RouteService(Graph graph) {
        this.graph = graph;
        this.dij = new Dijkstra();
        this.bellmanFord = new BellmanFord();
        this.bfs = new BFS();
        this.sc = new Scanner(System.in);
    }
    public void findFastestRoute(String[] cities) {
        executeWithRuntime("fastest", () -> dij.calculateWithPriorityQueue(graph, cities[0], cities[1], true));
    }

    public void findCheapestRoute(String[] cities) {
        executeWithRuntime("cheapest", () -> dij.calculateWithRadixHeap(graph, cities[0], cities[1], false));
    }

    public void findMostDirectRoute(String[] cities) {
        executeWithRuntime("most direct", () -> bfs.findMostDirectRoute(graph, cities[0], cities[1]));
    }
    // Functional interface to decouple route finding from the user interface
    @FunctionalInterface
    interface RouteFinder {
        void findRoute(String[] cities);
    }
    public long measureAverageRuntimeUtil(int choice){
        String[] cities = getRouteInput();
        long runtime=0;
        switch (choice){
            case 1->runtime=measureAverageRuntime("fastest",() -> dij.calculateWithPriorityQueue(graph,cities[0],cities[1],true));
            case 2->runtime=measureAverageRuntime("fastest",() -> dij.calculateWithRadixHeap(graph,cities[0],cities[1],true));
            case 3->runtime=measureAverageRuntime("cheapest",() -> dij.calculateWithPriorityQueue(graph,cities[0],cities[1],false));
            case 4->runtime=measureAverageRuntime("cheapest",() -> dij.calculateWithRadixHeap(graph,cities[0],cities[1],false));
            case 5->runtime=measureAverageRuntime("cheapest",()-> bellmanFord.calculateCheapestRoute(graph,cities[0],cities[1]));
            case 6->runtime=measureAverageRuntime("direct",()-> bfs.findMostDirectRoute(graph,cities[0],cities[1]));
        }
        return runtime;
    }
    public long measureAverageRuntime(String routeType, Runnable routeFunction) {
        long totalRuntime = 0;
        for (int i = 0; i < 10; i++) {
            totalRuntime += executeWithRuntime(routeType, routeFunction);
        }
        return totalRuntime / 10;
    }
    public long executeWithRuntime(String routeType, Runnable routeFunction) {
        try {
            System.out.print("\033[1;36mCalculating the " + routeType + " route\033[0m");
            simulateProgress();
            long startTime = System.nanoTime();
            routeFunction.run();
            long endTime = System.nanoTime();

            long runtimeInMillis = (endTime - startTime) / 1_000_000;
            long runtimeInNanos = endTime - startTime;

            if (runtimeInMillis == 0) {
                System.out.println("\033[1;32m" + capitalizeFirstLetter(routeType) + " route completed in " + runtimeInNanos + " ns\033[0m");
                return runtimeInNanos;
            } else {
                System.out.println("\033[1;32m" + capitalizeFirstLetter(routeType) + " route completed in " + runtimeInMillis + " ms\033[0m");
                return  runtimeInMillis;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\033[1;31mError: " + e.getMessage() + "\033[0m");
        } catch (Exception e) {
            System.out.println("\033[1;31mAn unexpected error occurred while finding the " + routeType + " route: " + e.getMessage() + "\033[0m");
        }
        return  0;
    }

    public void compareFastestRoutes() {
        try {
            String[] cities = getRouteInput();
            System.out.println("\033[1;36mComparing fastest routes\033[0m");

            long runtimeRadixHeap = executeWithRuntime("fastest", () -> dij.calculateWithRadixHeap(graph, cities[0], cities[1], true));
            long runtimePriorityQueue = executeWithRuntime("fastest", () -> dij.calculateWithPriorityQueue(graph, cities[0], cities[1],true));
//           long avgradixHeapRuntime=measureAverageRuntime("fastest",()->dij.calculateWithRadixHeap(graph,cities[0],cities[1],true));
            printComparisonResults(new String[]{"ID", "Algorithm", "Runtime (ms)"}, new String[][]{
                    {"1", "PriorityQueue", String.valueOf(runtimePriorityQueue)},
                    {"2", "RadixHeap", String.valueOf(runtimeRadixHeap)},
            });

            determineFasterAlgorithm(runtimePriorityQueue, runtimeRadixHeap, "fastest");
        } catch (IllegalArgumentException e) {
            System.out.println("\033[1;31mError: " + e.getMessage() + "\033[0m");
        }
    }

    public void compareCheapestRoutes() {
        try {
            String[] cities = getRouteInput();
            System.out.println("\033[1;36mComparing cheapest routes\033[0m");

            long runtimeDijkstraRadixHeap =  executeWithRuntime("cheapest", () -> dij.calculateWithRadixHeap(graph, cities[0], cities[1], false));
            long runtimeDijkstra= executeWithRuntime("cheapest", () -> dij.calculateWithPriorityQueue(graph, cities[0], cities[1], false));
            long runtimeBellmanFord =  executeWithRuntime("cheapest", () -> bellmanFord.calculateCheapestRoute(graph, cities[0], cities[1]));

            printComparisonResults(new String[]{"ID", "Algorithm", "Runtime (ms)"}, new String[][]{
                    {"1","Dijkstra radix heap",String.valueOf(runtimeDijkstraRadixHeap)},
                    {"2", "Dijkstra priority", String.valueOf(runtimeDijkstra)},
                    {"3", "Bellman-Ford", String.valueOf(runtimeBellmanFord)}
            });

        } catch (IllegalArgumentException e) {

            System.out.println("\033[1;31mError: " + e.getMessage() + "\033[0m");
        }
    }

    private void printComparisonResults(String[] headers, String[][] rows) {
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }
        // Print headers
        System.out.printf("\033[1;34m\033[1m"); // Bold blue
        for (int i = 0; i < headers.length; i++) {
            System.out.printf("%-" + (columnWidths[i]) + "s | ", headers[i]);
        }
        System.out.println("\033[0m"); // Reset color after printing headers
        System.out.println("\033[1;34m" + "-".repeat(Arrays.stream(columnWidths).map(w -> w).sum() + (headers.length - 1) * 3) + "\033[0m");
        for (String[] row : rows) {
            System.out.printf("\033[1;35m");
            for (int i = 0; i < row.length; i++) {
                if (i > 0) {
                    System.out.printf("%" + (columnWidths[i]) + "s | ", row[i]);
                } else {
                    System.out.printf("%-" + (columnWidths[i]) + "s | ", row[i]);
                }
            }
            System.out.println("\033[0m");
        }
        System.out.println("\033[1;34m" + "-".repeat(Arrays.stream(columnWidths).map(w -> w).sum() + (headers.length - 1) * 3) + "\033[0m");
    }

    private void determineFasterAlgorithm(long runtime1, long runtime2, String routeType) {
        if (runtime2 > runtime1) {
            System.out.println("\033[1;32mPriority Queue Dijkstra's is faster\033[0m");
        } else if (runtime2 < runtime1) {
            System.out.println("\033[1;32mRadix Heap Dijkstra's is faster\033[0m");
        } else {
            System.out.println("\033[1;32mBoth are equally fast\033[0m");
        }
    }
    public String[] getRouteInput() {
        String source = "";
        String destination = "";

        while (true) {
            try {
                System.out.print("\033[1;34mEnter the starting city: \033[0m");
                source = sc.nextLine().trim().toLowerCase();
                source=capitalizeFirstLetter(source);
                if(source.length()<=3){
                    source = source.toUpperCase();
                }
                System.out.print("\033[1;34mEnter the destination city: \033[0m");
                destination = sc.nextLine().trim().toLowerCase();
                destination=capitalizeFirstLetter(destination);
                if (destination.length()<=3){
                    destination= destination.toUpperCase();
                }
                if (source.isEmpty() || destination.isEmpty()) {
                    throw new IllegalArgumentException("City names cannot be empty. Please enter valid source and destination.");
                }

                if (!graph.containsCity(source) || !graph.containsCity(destination)) {
                    throw new IllegalArgumentException("Invalid city names. Please enter valid source and destination.");
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("\033[1;31m" + e.getMessage() + "\033[0m"); // Print error message in red
            }
        }
        return new String[]{source, destination};
    }


    private void simulateProgress() {
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(500);
                System.out.print(".");
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String capitalizeFirstLetter(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
