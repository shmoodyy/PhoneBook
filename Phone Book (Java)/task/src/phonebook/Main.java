package phonebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.*;

public class Main {

    static boolean tookTooLong = false;
    static int foundEntries = 0;
    static Duration linearDuration;

    public static void main(String[] args) {
        List<String> directoryEntries = readFromFile("/Users/hamud/Downloads/directory.txt");
        List<String> entriesToFind = readFromFile("/Users/hamud/Downloads/find.txt");

        findEntries(directoryEntries, entriesToFind, "linear");
        findEntries(directoryEntries, entriesToFind, "jump");
        findEntries(directoryEntries, entriesToFind, "binary");
        findEntries(directoryEntries, entriesToFind, "hash");
    }

    private static void findEntries(List<String> directoryEntries, List<String> entriesToFind, String searchMode) {
        foundEntries = 0;
        switch (searchMode) {
            case "linear" -> performLinearSearch(directoryEntries, entriesToFind);
            case "jump"   -> performBubbleSortedJumpSearch(directoryEntries, entriesToFind);
            case "binary" -> performQuickSortedBinarySearch(directoryEntries, entriesToFind);
            case "hash"   -> performHashTableSearch(directoryEntries, entriesToFind);
        }
    }

    private static void performHashTableSearch(List<String> directoryEntries, List<String> entriesToFind) {
        long creationStartTime = System.currentTimeMillis();
        Set<String> directoryHashTable = readFromFileIntoHashTable("/Users/hamud/Downloads/directory.txt");
        Duration creationDuration = Duration.ofMillis(System.currentTimeMillis() - creationStartTime);

        System.out.println("\nStart searching (hash table)...");
        long startTime = System.currentTimeMillis();

        for (String entry : entriesToFind) {
            if (directoryHashTable.contains(entry)) {
                foundEntries++;
            }
        }

        // How the search would work if the numbers were included in the directory: 5 seconds longer.
//        for (String entry : entriesToFind) {
//            for (String element : directoryHashTable) {
//                if (getFullName(element).compareTo(entry) == 0) {
//                    foundEntries++;
//                    break; // No need to check further once substring is found
//                }
//            }
//        }
        Duration hashSearchDuration = Duration.ofMillis(System.currentTimeMillis() - startTime);

        Duration totalDuration = creationDuration.plus(hashSearchDuration);
        printDuration(entriesToFind.size(), "Creating", totalDuration, creationDuration, hashSearchDuration);
    }

    private static void performQuickSortedBinarySearch(List<String> directoryEntries, List<String> entriesToFind) {
        System.out.println("\nStart searching (quick sort + binary search)...");
        long startTime = System.currentTimeMillis();

        quickSort(directoryEntries, 0, directoryEntries.size() - 1);
        Duration quickDuration = Duration.ofMillis(System.currentTimeMillis() - startTime);

        for (String entry : entriesToFind) {
            if (binarySearch(directoryEntries, entry)) {
                foundEntries++;
            }
        }
        Duration binaryDuration = Duration.ofMillis(System.currentTimeMillis() - startTime);

        Duration totalDuration = quickDuration.plus(binaryDuration);
        printDuration(entriesToFind.size(), "Sorting", totalDuration, quickDuration, binaryDuration);
    }

    private static void performBubbleSortedJumpSearch(List<String> directoryEntries, List<String> entriesToFind) {
        System.out.println("Start searching (bubble sort + jump search)...");
        long startTime = System.currentTimeMillis();

        bubbleSort(directoryEntries);
        Duration bubbleDuration = Duration.ofMillis(System.currentTimeMillis() - startTime);

        if (tookTooLong) {
            findEntries(directoryEntries, entriesToFind, "linear");
        } else {
            for (String entry : entriesToFind) {
                if (jumpSearch(directoryEntries, entry)) {
                    foundEntries++;
                }
            }
        }
        Duration jumpDuration = tookTooLong ?
                linearDuration : Duration.ofMillis(System.currentTimeMillis() - startTime);

        Duration totalDuration = bubbleDuration.plus(jumpDuration);
        printDuration(entriesToFind.size(), "Sorting", totalDuration, bubbleDuration, jumpDuration);
        tookTooLong = false;
    }

    private static void printDuration(int entriesToFindSize, String creationMode ,
                                      Duration totalDuration, Duration creationDuration, Duration searchDuration) {
        System.out.printf("""
                        Found %d / %d entries. Time taken: %d min. %d sec. %d ms.
                        %s time: %d min. %d sec. %d ms.%s
                        Searching time: %d min. %d sec. %d ms.
                        """,
                foundEntries, entriesToFindSize,
                totalDuration.toMinutesPart(), totalDuration.toSecondsPart(), totalDuration.toMillisPart(),
                creationMode,
                creationDuration.toMinutesPart(), creationDuration.toSecondsPart(), creationDuration.toMillisPart(),
                tookTooLong ? " - STOPPED, moved to linear search" : "",
                searchDuration.toMinutesPart(), searchDuration.toSecondsPart(), searchDuration.toMillisPart());
    }

    private static void performLinearSearch(List<String> directoryEntries, List<String> entriesToFind) {
        System.out.print((tookTooLong) ? "" : "Start searching (linear search)...\n");
        long linearStartTime = System.currentTimeMillis();

        for (String entry : entriesToFind) {
            if (linearSearch(directoryEntries, entry)) {
                foundEntries++;
            }
        }
        linearDuration = Duration.ofMillis(System.currentTimeMillis() - linearStartTime);

        if (!tookTooLong) {
            System.out.printf("Found %d / %d entries. Time taken: %d min. %d sec. %d ms.\n\n"
                    , foundEntries, entriesToFind.size()
                    , linearDuration.toMinutesPart(), linearDuration.toSecondsPart(), linearDuration.toMillisPart());
        }
    }

    public static boolean binarySearch(List<String> list, String target) {
        int left = 0;
        int right = list.size();
        while (left <= right) {
            int middle = left + ((right - left) / 2);

            if (getFullName(list.get(middle)).compareTo(target) == 0) {
                return true;
            } else if (getFullName(list.get(middle)).compareTo(target) > 0) {
                right = middle - 1;
            } else {
                left = middle + 1;
            }
        }
        return false;
    }

    public static void quickSort(List<String> list, int lowIndex, int highIndex) {
        if (lowIndex >= highIndex) return;

        int pivotIndex = new Random().nextInt(highIndex - lowIndex) + lowIndex;
        String pivot = getFullName(list.get(pivotIndex));
        swap(list, pivotIndex, highIndex);

        int leftPointer = partition(list, lowIndex, highIndex, pivot);

        quickSort(list, lowIndex, leftPointer - 1);
        quickSort(list, leftPointer + 1, highIndex);
    }

    private static int partition(List<String> list, int lowIndex, int highIndex, String pivot) {
        int leftPointer = lowIndex;
        int rightPointer = highIndex - 1;
        while (leftPointer < rightPointer) {
            // Walk from the left until we find a number greater than the pivot, or hit the right pointer.
            while (getFullName(list.get(leftPointer)).compareTo(pivot) <= 0 && leftPointer < rightPointer) {
                leftPointer++;
            }
            // Walk from the right until we find a number less than the pivot, or hit the left pointer.
            while (getFullName(list.get(rightPointer)).compareTo(pivot) >= 0 && leftPointer < rightPointer) {
                rightPointer--;
            }
            swap(list, leftPointer, rightPointer);
        }

        if (getFullName(list.get(leftPointer)).compareTo(getFullName(list.get(highIndex))) > 0) {
            swap(list, leftPointer, highIndex);
        } else {
            leftPointer = highIndex;
        }
        return leftPointer;
    }

    public static boolean jumpSearch(List<String> list, String target) {
        int curr = 0;
        int prev = 0;
        int size = list.size();
        int last = size - 1;
        int step = (int) Math.floor(Math.sqrt(size));

        if (size == 0) return false;

        while (getFullName(list.get(curr)).compareTo(target) < 0) {
            if (curr == last) return false;
            prev = curr;
            curr = Math.min(curr + step, last);
        }

        while (getFullName(list.get(curr)).compareTo(target) > 0) {
            curr--;
            if (curr <= prev) return false;
        }
        return getFullName(list.get(curr)).compareTo(target) == 0;
    }

    public static void bubbleSort(List<String> list) {
        long startSortTime = System.currentTimeMillis();
        int size = list.size();
        boolean swapped;
        for (int i = 0; i < size - 1; i++) {
            swapped = false;
            for (int j = 0; j < size - i - 1; j++) { // inner loop checks til end of the curr iteration, rest is sorted
                if (System.currentTimeMillis() - startSortTime > linearDuration.toMillis() * 10) {
                    tookTooLong = true;
                    break;
                } if (getFullName(list.get(j)).compareTo(getFullName(list.get(j + 1))) > 0) {
                    swap(list, j, j + 1);
                    swapped = true;
                }
            }
            if (tookTooLong || !swapped) break; // if sorting algo takes too long or is already swapped (for efficiency)
        }
    }

    private static void swap(List<String> list, int index1, int index2) {
        String temp = list.get(index1);
        list.set(index1, list.get(index2));
        list.set(index2, temp);
    }

    private static String getFullName(String element) {
        return element.substring(element.indexOf(' ') + 1);
    }

    public static boolean linearSearch(List<String> list, String target) {
        for (String entry : list) {
            if (entry.contains(target)) return true;
        }
        return false;
    }

    private static Set<String> readFromFileIntoHashTable(String filename) {
        Set<String> entries = new HashSet<>();
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String entry = scanner.nextLine().strip();
                entry = entry.replaceFirst("^\\d+\\s*", ""); // remove
                entries.add(entry);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("The file was not found.");
            e.printStackTrace();
        }
        return entries;
    }

    private static List<String> readFromFile(String filename) {
        List<String> entries = new ArrayList<>();
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String entry = scanner.nextLine().strip();
                entries.add(entry);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("The file was not found.");
            e.printStackTrace();
        }
        return entries;
    }
}