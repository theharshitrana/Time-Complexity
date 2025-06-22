import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Random;

public class TimeComplexityAnalyzer extends JFrame {
    private JComboBox<String> algorithmComboBox;
    private JTextField minSizeField, maxSizeField, stepField;
    private JComboBox<String> orderComboBox;
    private JTextArea resultArea;
    private JButton analyzeButton;
    private JPanel chartPanel;
    private JTextArea complexityInfoArea;

    private static final String[] ALGORITHMS = {
            "Linear Search", "Binary Search", "Bubble Sort", 
            "Merge Sort", "Quick Sort", "Fibonacci Recursive"
    };
    
    private static final String[] ORDERS = {
            "Random", "Ascending", "Descending", "Almost Sorted"
    };

    // Complexity information for each algorithm
    private static final String[] COMPLEXITY_INFO = {
            "Linear Search:\n" +
            "Time Complexity:\n" +
            "  Best: O(1) - Element found at first position\n" +
            "  Average: O(n) - Element found in the middle\n" +
            "  Worst: O(n) - Element not found or at last position\n" +
            "Space Complexity: O(1) - No additional space needed",
            
            "Binary Search:\n" +
            "Time Complexity:\n" +
            "  Best: O(1) - Element found at middle\n" +
            "  Average: O(log n) - Element found in logarithmic time\n" +
            "  Worst: O(log n) - Element not found\n" +
            "Space Complexity: O(1) - Iterative implementation\n" +
            "Space Complexity: O(log n) - Recursive implementation",
            
            "Bubble Sort:\n" +
            "Time Complexity:\n" +
            "  Best: O(n) - When array is already sorted\n" +
            "  Average: O(n²) - For random data\n" +
            "  Worst: O(n²) - When array is reverse sorted\n" +
            "Space Complexity: O(1) - In-place sorting",
            
            "Merge Sort:\n" +
            "Time Complexity:\n" +
            "  Best: O(n log n) - All cases\n" +
            "  Average: O(n log n)\n" +
            "  Worst: O(n log n)\n" +
            "Space Complexity: O(n) - Additional space required",
            
            "Quick Sort:\n" +
            "Time Complexity:\n" +
            "  Best: O(n log n) - Good pivot selection\n" +
            "  Average: O(n log n)\n" +
            "  Worst: O(n²) - When pivot is smallest or largest\n" +
            "Space Complexity: O(log n) - Recursion stack space",
            
            "Fibonacci Recursive:\n" +
            "Time Complexity:\n" +
            "  Best: O(2^n) - Exponential time\n" +
            "  Average: O(2^n)\n" +
            "  Worst: O(2^n)\n" +
            "Space Complexity: O(n) - Recursion stack depth"
    };

    public TimeComplexityAnalyzer() {
        setTitle("Time Complexity Analyzer");
        setSize(900, 700); // Increased size to accommodate complexity info
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Main panel for inputs and complexity info
        JPanel topPanel = new JPanel(new BorderLayout());

        // Input panel with horizontal layout for each field
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create horizontal rows for each input pair
        JPanel algorithmPanel = createInputRow("Algorithm:", algorithmComboBox = new JComboBox<>(ALGORITHMS));
        JPanel minSizePanel = createInputRow("Min Size:", minSizeField = new JTextField("100"));
        JPanel maxSizePanel = createInputRow("Max Size:", maxSizeField = new JTextField("10000"));
        JPanel stepPanel = createInputRow("Step Size:", stepField = new JTextField("500"));
        JPanel orderPanel = createInputRow("Input Order:", orderComboBox = new JComboBox<>(ORDERS));

        // Add components to input panel
        inputPanel.add(algorithmPanel);
        inputPanel.add(minSizePanel);
        inputPanel.add(maxSizePanel);
        inputPanel.add(stepPanel);
        inputPanel.add(orderPanel);

        // Centered analyze button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        analyzeButton = new JButton("Analyze Time Complexity");
        buttonPanel.add(analyzeButton);
        inputPanel.add(buttonPanel);

        // Complexity information panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Algorithm Complexity Information"));
        complexityInfoArea = new JTextArea();
        complexityInfoArea.setEditable(false);
        complexityInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        complexityInfoArea.setText(COMPLEXITY_INFO[0]); // Default to first algorithm
        infoPanel.add(new JScrollPane(complexityInfoArea), BorderLayout.CENTER);

        // Add both panels to top panel
        topPanel.add(inputPanel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Chart panel
        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Chart drawing will be implemented here
            }
        };
        chartPanel.setPreferredSize(new Dimension(400, 300));
        chartPanel.setBackground(Color.WHITE);

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(chartPanel, BorderLayout.SOUTH);

        // Event listeners
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analyzeTimeComplexity();
            }
        });

        algorithmComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update complexity info when algorithm changes
                int selectedIndex = algorithmComboBox.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < COMPLEXITY_INFO.length) {
                    complexityInfoArea.setText(COMPLEXITY_INFO[selectedIndex]);
                }
            }
        });

        // Add input validation listeners
        addInputValidationListeners();
    }

    private JPanel createInputRow(String labelText, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panel.add(new JLabel(labelText));
        component.setPreferredSize(new Dimension(200, 25));
        panel.add(component);
        return panel;
    }

    private void addInputValidationListeners() {
        DocumentListener listener = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateInputsVisual(); }
            public void removeUpdate(DocumentEvent e) { validateInputsVisual(); }
            public void insertUpdate(DocumentEvent e) { validateInputsVisual(); }
        };
        
        minSizeField.getDocument().addDocumentListener(listener);
        maxSizeField.getDocument().addDocumentListener(listener);
        stepField.getDocument().addDocumentListener(listener);
    }

    private void validateInputsVisual() {
        try {
            int minSize = Integer.parseInt(minSizeField.getText());
            int maxSize = Integer.parseInt(maxSizeField.getText());
            int step = Integer.parseInt(stepField.getText());
            
            if (maxSize <= minSize) {
                maxSizeField.setBackground(Color.PINK);
            } else {
                maxSizeField.setBackground(Color.WHITE);
            }
            
            if (step <= 0 || step > (maxSize - minSize)) {
                stepField.setBackground(Color.PINK);
            } else {
                stepField.setBackground(Color.WHITE);
            }
            
            if (minSize <= 0) {
                minSizeField.setBackground(Color.PINK);
            } else {
                minSizeField.setBackground(Color.WHITE);
            }
        } catch (NumberFormatException e) {
            // Ignore while typing
        }
    }

    private boolean validateInputs() {
        try {
            int minSize = Integer.parseInt(minSizeField.getText());
            int maxSize = Integer.parseInt(maxSizeField.getText());
            int step = Integer.parseInt(stepField.getText());
            
            if (minSize <= 0 || maxSize <= 0 || step <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "All size values must be positive numbers",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (maxSize <= minSize) {
                JOptionPane.showMessageDialog(this, 
                    "Maximum size must be greater than minimum size\n" +
                    "Current values: Min=" + minSize + ", Max=" + maxSize,
                    "Invalid Size Range", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (step > (maxSize - minSize)) {
                JOptionPane.showMessageDialog(this, 
                    "Step size is too large for the given range\n" +
                    "Maximum allowed step: " + (maxSize - minSize),
                    "Invalid Step Size", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid numbers for all size fields",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void analyzeTimeComplexity() {
        // Validate inputs before proceeding
        if (!validateInputs()) {
            return;
        }
        
        try {
            // Get user inputs
            String algorithm = (String) algorithmComboBox.getSelectedItem();
            int minSize = Integer.parseInt(minSizeField.getText());
            int maxSize = Integer.parseInt(maxSizeField.getText());
            int step = Integer.parseInt(stepField.getText());
            String order = (String) orderComboBox.getSelectedItem();

            // For Fibonacci, limit max size to prevent stack overflow
            if (algorithm.equals("Fibonacci Recursive") && maxSize > 40) {
                int newMax = Math.min(maxSize, 40);
                maxSizeField.setText(String.valueOf(newMax));
                maxSize = newMax;
                JOptionPane.showMessageDialog(this, 
                    "Fibonacci recursive limited to max size 40 to prevent stack overflow",
                    "Input Adjusted", JOptionPane.WARNING_MESSAGE);
            }

            resultArea.setText("");
            resultArea.append("Analyzing " + algorithm + " with " + order + " order...\n");
            resultArea.append("Input Sizes: " + minSize + " to " + maxSize + " with step " + step + "\n\n");

            // Prepare data for chart
            int[] sizes = new int[(maxSize - minSize) / step + 1];
            long[] times = new long[sizes.length];
            int index = 0;

            for (int size = minSize; size <= maxSize; size += step) {
                try {
                    // Generate input data based on order
                    int[] data = generateInputData(size, order);

                    long startTime = System.nanoTime();
                    
                    // Execute selected algorithm
                    executeAlgorithm(algorithm, data, size);
                    
                    long endTime = System.nanoTime();
                    long duration = endTime - startTime;

                    sizes[index] = size;
                    times[index] = duration;
                    index++;

                    resultArea.append("Size: " + size + " - Time: " + duration + " ns\n");
                } catch (OutOfMemoryError e) {
                    JOptionPane.showMessageDialog(this, 
                        "Out of memory at size " + size + ". Analysis stopped.",
                        "Memory Error", JOptionPane.ERROR_MESSAGE);
                    break;
                } catch (StackOverflowError e) {
                    JOptionPane.showMessageDialog(this, 
                        "Stack overflow at size " + size + ". Analysis stopped.",
                        "Stack Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }

            // Draw chart if we have at least 2 data points
            if (index >= 2) {
                drawChart(Arrays.copyOf(sizes, index), Arrays.copyOf(times, index), algorithm);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Not enough data points to draw chart", 
                    "Chart Error", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "An unexpected error occurred: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void executeAlgorithm(String algorithm, int[] data, int size) {
        switch (algorithm) {
            case "Linear Search":
                linearSearch(data, data[size-1]);
                break;
            case "Binary Search":
                Arrays.sort(data);
                binarySearch(data, data[size-1]);
                break;
            case "Bubble Sort":
                bubbleSort(data.clone());
                break;
            case "Merge Sort":
                mergeSort(data.clone());
                break;
            case "Quick Sort":
                quickSort(data.clone(), 0, data.length-1);
                break;
            case "Fibonacci Recursive":
                fibonacci(size % 40);
                break;
            default:
                throw new IllegalArgumentException("Unknown algorithm selected");
        }
    }

    private int[] generateInputData(int size, String order) {
        try {
            int[] data = new int[size];
            Random random = new Random();

            for (int i = 0; i < size; i++) {
                data[i] = random.nextInt(size * 10);
            }

            switch (order) {
                case "Ascending":
                    Arrays.sort(data);
                    break;
                case "Descending":
                    Arrays.sort(data);
                    reverseArray(data);
                    break;
                case "Almost Sorted":
                    Arrays.sort(data);
                    for (int i = 0; i < size / 10; i++) {
                        int idx1 = random.nextInt(size);
                        int idx2 = random.nextInt(size);
                        int temp = data[idx1];
                        data[idx1] = data[idx2];
                        data[idx2] = temp;
                    }
                    break;
            }

            return data;
        } catch (OutOfMemoryError e) {
            throw new OutOfMemoryError("Failed to generate data for size " + size);
        }
    }

    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    private int linearSearch(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }

    private int binarySearch(int[] array, int target) {
        int left = 0, right = array.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (array[mid] == target) return mid;
            if (array[mid] < target) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    private void bubbleSort(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }

    private void mergeSort(int[] array) {
        if (array.length > 1) {
            int mid = array.length / 2;
            int[] left = Arrays.copyOfRange(array, 0, mid);
            int[] right = Arrays.copyOfRange(array, mid, array.length);

            mergeSort(left);
            mergeSort(right);

            int i = 0, j = 0, k = 0;
            while (i < left.length && j < right.length) {
                if (left[i] < right[j]) {
                    array[k++] = left[i++];
                } else {
                    array[k++] = right[j++];
                }
            }
            while (i < left.length) array[k++] = left[i++];
            while (j < right.length) array[k++] = right[j++];
        }
    }

    private void quickSort(int[] array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);
            quickSort(array, low, pi - 1);
            quickSort(array, pi + 1, high);
        }
    }

    private int partition(int[] array, int low, int high) {
        int pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (array[j] < pivot) {
                i++;
                int temp = array[i];
                array[i] = array[j];
                array[j] = temp;
            }
        }
        int temp = array[i + 1];
        array[i + 1] = array[high];
        array[high] = temp;
        return i + 1;
    }

    private int fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    private void drawChart(int[] sizes, long[] times, String algorithm) {
        try {
            long maxTime = Arrays.stream(times).max().orElse(1);
            int maxSize = sizes[sizes.length - 1];

            Graphics2D g2d = (Graphics2D) chartPanel.getGraphics();
            if (g2d == null) return;
            
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, chartPanel.getWidth(), chartPanel.getHeight());

            g2d.setColor(Color.BLACK);
            g2d.drawLine(50, chartPanel.getHeight() - 50, chartPanel.getWidth() - 50, chartPanel.getHeight() - 50);
            g2d.drawLine(50, chartPanel.getHeight() - 50, 50, 50);

            g2d.drawString("Input Size (n)", chartPanel.getWidth() / 2 - 30, chartPanel.getHeight() - 10);
            g2d.drawString("Time (ns)", 10, chartPanel.getHeight() / 2);
            g2d.drawString("Time Complexity: " + algorithm, chartPanel.getWidth() / 2 - 80, 20);

            g2d.setColor(Color.BLUE);
            int prevX = 0, prevY = 0;
            for (int i = 0; i < sizes.length; i++) {
                int x = 50 + (sizes[i] * (chartPanel.getWidth() - 100)) / maxSize;
                int y = chartPanel.getHeight() - 50 - (int)(times[i] * (chartPanel.getHeight() - 100) / maxTime);

                g2d.fillOval(x - 2, y - 2, 4, 4);
                if (i > 0) {
                    g2d.drawLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }
        } catch (Exception e) {
            System.err.println("Error drawing chart: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                TimeComplexityAnalyzer analyzer = new TimeComplexityAnalyzer();
                analyzer.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Failed to initialize application: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}