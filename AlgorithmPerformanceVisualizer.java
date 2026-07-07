import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Algorithm Performance Visualizer - Enterprise Edition
 * Features: MVC, Custom Interactive Charts, Visualizer, HTML Reports, System Telemetry
 */
public class AlgorithmPerformanceVisualizer extends JFrame {

    // Theme Colors (Modern Dark UI)
    public static final Color BG_DARK = new Color(24, 24, 36);
    public static final Color BG_PANEL = new Color(30, 30, 46);
    public static final Color ACCENT = new Color(137, 180, 250);
    public static final Color ACCENT_HOVER = new Color(180, 190, 254);
    public static final Color TEXT_MAIN = new Color(205, 214, 244);
    public static final Color TEXT_MUTED = new Color(166, 173, 200);
    public static final Color BORDER_COLOR = new Color(49, 50, 68);

    public static void main(String[] args) {
        setupTheme();
        SwingUtilities.invokeLater(() -> {
            AlgorithmPerformanceVisualizer app = new AlgorithmPerformanceVisualizer();
            app.setVisible(true);
        });
    }

    private static void setupTheme() {
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("Label.foreground", TEXT_MAIN);
        UIManager.put("CheckBox.background", BG_PANEL);
        UIManager.put("CheckBox.foreground", TEXT_MAIN);
        UIManager.put("ComboBox.background", BG_DARK);
        UIManager.put("ComboBox.foreground", TEXT_MAIN);
        UIManager.put("TextField.background", BG_DARK);
        UIManager.put("TextField.foreground", TEXT_MAIN);
        UIManager.put("TextField.caretForeground", TEXT_MAIN);
        UIManager.put("Table.background", BG_DARK);
        UIManager.put("Table.foreground", TEXT_MAIN);
        UIManager.put("TableHeader.background", BG_PANEL);
        UIManager.put("TableHeader.foreground", ACCENT);
        UIManager.put("ScrollPane.background", BG_PANEL);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }

    public AlgorithmPerformanceVisualizer() {
        setTitle("Algorithm Performance Visualizer");
        setSize(1350, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        Model model = new Model();
        View view = new View();
        new Controller(model, view, this);

        setContentPane(view.getMainContainer());
    }

    // =====================================================================================
    // UTILITY & DATA MODELS
    // =====================================================================================
    public static String formatTime(double ns) {
        if (ns < 1000) return String.format("%.0f ns", ns);
        if (ns < 1_000_000) return String.format("%.2f μs", ns / 1000.0);
        if (ns < 1_000_000_000) return String.format("%.2f ms", ns / 1_000_000.0);
        return String.format("%.2f s", ns / 1_000_000_000.0);
    }

    public static String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / 1048576.0);
    }

    static class BenchmarkResult {
        String algorithm; int size;
        long avgTime, medianTime, minTime, maxTime, memoryBytes;
        public BenchmarkResult(String a, int s, long avg, long med, long min, long max, long mem) {
            algorithm=a; size=s; avgTime=avg; medianTime=med; minTime=min; maxTime=max; memoryBytes=mem;
        }
    }

    static class RunHistory {
        String timestamp, algorithms, config;
        Map<String, List<BenchmarkResult>> data;
        public RunHistory(String algos, String conf, Map<String, List<BenchmarkResult>> d) {
            timestamp = new SimpleDateFormat("HH:mm:ss dd/MM").format(new Date());
            algorithms = algos; config = conf; data = new LinkedHashMap<>(d);
        }
    }

    // =====================================================================================
    // MVC: MODEL
    // =====================================================================================
    static class Model {
        public static final String[] ALGORITHMS = {
                "Linear Search", "Binary Search", "Bubble Sort", "Selection Sort",
                "Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort", 
                "Counting Sort", "Radix Sort", "Fibonacci Recursive", "Fibonacci DP"
        };
        public static final String[] ORDERS = {"Random", "Ascending", "Descending", "Almost Sorted"};
        public static final int MEASURE_RUNS = 50;

        private final Map<String, List<BenchmarkResult>> results = new LinkedHashMap<>();
        public final List<RunHistory> history = new ArrayList<>();

        public void clearResults() { results.clear(); }
        public void addResult(String algo, BenchmarkResult res) { results.computeIfAbsent(algo, k -> new ArrayList<>()).add(res); }
        public Map<String, List<BenchmarkResult>> getResults() { return results; }
        public void saveToHistory(String algos, String conf) { history.add(0, new RunHistory(algos, conf, results)); }

        public int[] generateInputData(int size, String order) {
            int[] data = new int[size]; Random random = new Random();
            for (int i = 0; i < size; i++) data[i] = random.nextInt(Math.max(10, size * 10));
            switch (order) {
                case "Ascending": Arrays.sort(data); break;
                case "Descending":
                    Arrays.sort(data); for (int i = 0; i < size / 2; i++) { int t = data[i]; data[i] = data[size - 1 - i]; data[size - 1 - i] = t; } break;
                case "Almost Sorted":
                    Arrays.sort(data); for (int i = 0; i < Math.max(1, size / 20); i++) { int idx1 = random.nextInt(size), idx2 = random.nextInt(size); int t = data[idx1]; data[idx1] = data[idx2]; data[idx2] = t; } break;
            } return data;
        }

        public void runAlgoPrepared(String algorithm, int[] data, int target, int size) {
            switch (algorithm) {
                case "Linear Search": linearSearch(data, target); break;
                case "Binary Search": binarySearch(data, target); break;
                case "Bubble Sort": bubbleSort(data); break;
                case "Selection Sort": selectionSort(data); break;
                case "Insertion Sort": insertionSort(data); break;
                case "Merge Sort": mergeSort(data); break;
                case "Quick Sort": quickSort(data, 0, data.length - 1); break;
                case "Heap Sort": heapSort(data); break;
                case "Counting Sort": countingSort(data); break;
                case "Radix Sort": radixSort(data); break;
                case "Fibonacci Recursive": fibonacciRecursive(size); break;
                case "Fibonacci DP": fibonacciDP(size); break;
            }
        }
        
        // --- Core Algorithms ---
        private int linearSearch(int[] arr, int t) { for (int i=0; i<arr.length; i++) if (arr[i]==t) return i; return -1; }
        private int binarySearch(int[] arr, int t) { int l = 0, r = arr.length - 1; while (l <= r) { int m = l + (r - l) / 2; if (arr[m] == t) return m; if (arr[m] < t) l = m + 1; else r = m - 1; } return -1; }
        private void bubbleSort(int[] arr) { for (int i = 0; i < arr.length - 1; i++) { boolean swapped = false; for (int j = 0; j < arr.length - i - 1; j++) if (arr[j] > arr[j + 1]) { int t = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = t; swapped = true; } if (!swapped) break; } }
        private void selectionSort(int[] arr) { for (int i = 0; i < arr.length - 1; i++) { int min = i; for (int j = i + 1; j < arr.length; j++) if (arr[j] < arr[min]) min = j; int t = arr[min]; arr[min] = arr[i]; arr[i] = t; } }
        private void insertionSort(int[] arr) { for (int i = 1; i < arr.length; ++i) { int key = arr[i], j = i - 1; while (j >= 0 && arr[j] > key) { arr[j + 1] = arr[j]; j--; } arr[j + 1] = key; } }
        private void mergeSort(int[] arr) { if (arr.length > 1) { int mid = arr.length / 2; int[] l = Arrays.copyOfRange(arr, 0, mid), r = Arrays.copyOfRange(arr, mid, arr.length); mergeSort(l); mergeSort(r); int i = 0, j = 0, k = 0; while (i < l.length && j < r.length) arr[k++] = (l[i] < r[j]) ? l[i++] : r[j++]; while (i < l.length) arr[k++] = l[i++]; while (j < r.length) arr[k++] = r[j++]; } }
        private void quickSort(int[] arr, int low, int high) { if (low < high) { int pivot = arr[high], i = low - 1; for (int j = low; j < high; j++) if (arr[j] < pivot) { i++; int t = arr[i]; arr[i] = arr[j]; arr[j] = t; } int t = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = t; quickSort(arr, low, i); quickSort(arr, i + 2, high); } }
        private void heapSort(int[] arr) { int n = arr.length; for (int i = n / 2 - 1; i >= 0; i--) heapify(arr, n, i); for (int i = n - 1; i > 0; i--) { int t = arr[0]; arr[0] = arr[i]; arr[i] = t; heapify(arr, i, 0); } }
        private void heapify(int[] arr, int n, int i) { int max = i, l = 2 * i + 1, r = 2 * i + 2; if (l < n && arr[l] > arr[max]) max = l; if (r < n && arr[r] > arr[max]) max = r; if (max != i) { int t = arr[i]; arr[i] = arr[max]; arr[max] = t; heapify(arr, n, max); } }
        private void countingSort(int[] arr) { if(arr.length == 0) return; int max = arr[0]; for (int i = 1; i < arr.length; i++) if (arr[i] > max) max = arr[i]; int[] count = new int[max + 1], output = new int[arr.length]; for (int i=0; i<arr.length; i++) count[arr[i]]++; for (int i=1; i<=max; i++) count[i] += count[i - 1]; for (int i = arr.length - 1; i >= 0; i--) { output[count[arr[i]] - 1] = arr[i]; count[arr[i]]--; } System.arraycopy(output, 0, arr, 0, arr.length); }
        private void radixSort(int[] arr) { if(arr.length == 0) return; int max = arr[0]; for (int i = 1; i < arr.length; i++) if (arr[i] > max) max = arr[i]; for (int exp = 1; max / exp > 0; exp *= 10) { int[] output = new int[arr.length], count = new int[10]; for (int i = 0; i < arr.length; i++) count[(arr[i] / exp) % 10]++; for (int i = 1; i < 10; i++) count[i] += count[i - 1]; for (int i = arr.length - 1; i >= 0; i--) { output[count[(arr[i] / exp) % 10] - 1] = arr[i]; count[(arr[i] / exp) % 10]--; } System.arraycopy(output, 0, arr, 0, arr.length); } }
        private int fibonacciRecursive(int n) { if (n <= 1) return n; return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2); }
        private int fibonacciDP(int n) { if (n <= 1) return n; int a = 0, b = 1, c = 0; for(int i = 2; i <= n; i++) { c = a + b; a = b; b = c; } return c; }
    }

    // =====================================================================================
    // MVC: VIEW (Sidebar Navigation & Panels)
    // =====================================================================================
    static class View {
        private JPanel mainContainer, contentPanel;
        private CardLayout cardLayout;
        
        // Sidebar Buttons
        private JButton btnDashboard, btnVisualizer, btnEncyclopedia, btnHistory;
        
        // Dashboard Components
        Map<String, JCheckBox> algoChecks = new LinkedHashMap<>();
        JComboBox<String> orderCombo, statCombo;
        JTextField minSizeField, maxSizeField, stepField;
        JButton analyzeBtn, reportBtn, selectAllBtn;
        JProgressBar progressBar;
        JCheckBox showTheoryCheck;
        InteractiveChartPanel chartPanel;
        JTable resultsTable; DefaultTableModel tableModel;
        JLabel statMedian, statAvg, statMem;

        // Visualizer Components
        SortingVisualizerPanel visualizerPanel;
        JComboBox<String> visAlgoCombo;
        JButton btnPlay, btnPause, btnStep, btnReset;
        JSlider speedSlider;

        // History Components
        JList<String> historyList; DefaultListModel<String> historyModel;
        
        // Encyclopedia Components
        JEditorPane encyContent; JList<String> encyList;

        public View() {
            mainContainer = new JPanel(new BorderLayout());
            mainContainer.setBackground(BG_DARK);
            
            // Setup Sidebar
            JPanel sidebar = new JPanel();
            sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
            sidebar.setPreferredSize(new Dimension(220, 0));
            sidebar.setBackground(BG_PANEL);
            sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

            JLabel logo = new JLabel("<html>Algorithm Performance<br>Visualizer</html>");
            logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
            logo.setForeground(ACCENT);
            logo.setBorder(new EmptyBorder(20, 20, 30, 20));
            sidebar.add(logo);

            btnDashboard = createNavButton("Dashboard");
            btnVisualizer = createNavButton("Visualizer");
            btnEncyclopedia = createNavButton("Encyclopedia");
            btnHistory = createNavButton("Run History");

            sidebar.add(btnDashboard); sidebar.add(btnVisualizer); 
            sidebar.add(btnEncyclopedia); sidebar.add(btnHistory);
            sidebar.add(Box.createVerticalGlue());

            // Add Styled Hardware Telemetry Card to bottom of sidebar
            sidebar.add(createHardwarePanel());

            // Setup Content Area
            cardLayout = new CardLayout();
            contentPanel = new JPanel(cardLayout);
            contentPanel.setBackground(BG_DARK);
            
            contentPanel.add(createDashboard(), "Dashboard");
            contentPanel.add(createVisualizer(), "Visualizer");
            contentPanel.add(createEncyclopedia(), "Encyclopedia");
            contentPanel.add(createHistoryPanel(), "History");

            mainContainer.add(sidebar, BorderLayout.WEST);
            mainContainer.add(contentPanel, BorderLayout.CENTER);

            // Setup Navigation Actions
            setupNav(btnDashboard, "Dashboard"); setupNav(btnVisualizer, "Visualizer");
            setupNav(btnEncyclopedia, "Encyclopedia"); setupNav(btnHistory, "History");
            
            btnDashboard.setBackground(BORDER_COLOR); // Default active
        }

        public JPanel getMainContainer() { return mainContainer; }

        private JButton createNavButton(String text) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setForeground(TEXT_MAIN);
            btn.setBackground(BG_PANEL);
            btn.setBorder(new EmptyBorder(15, 25, 15, 25));
            btn.setFocusPainted(false); btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(220, 50));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return btn;
        }

        private void setupNav(JButton btn, String card) {
            btn.addActionListener(e -> {
                cardLayout.show(contentPanel, card);
                for (Component c : btn.getParent().getComponents()) {
                    if (c instanceof JButton) c.setBackground(BG_PANEL);
                }
                btn.setBackground(BORDER_COLOR);
            });
        }

        private JPanel createHardwarePanel() {
            JPanel hwPanel = new JPanel();
            hwPanel.setLayout(new BoxLayout(hwPanel, BoxLayout.Y_AXIS));
            hwPanel.setBackground(BG_DARK); // Inset background
            hwPanel.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 15, 20, 15), // Outer margin
                BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 1, true), // Rounded border
                    new EmptyBorder(12, 12, 12, 12) // Inner padding
                )
            ));
            hwPanel.setMaximumSize(new Dimension(200, 180));

            JLabel title = new JLabel("System Telemetry");
            title.setFont(new Font("Segoe UI", Font.BOLD, 12));
            title.setForeground(ACCENT);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            hwPanel.add(title);
            hwPanel.add(Box.createRigidArea(new Dimension(0, 10)));

            String os = System.getProperty("os.name");
            String arch = System.getProperty("os.arch");
            int cores = Runtime.getRuntime().availableProcessors();
            long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024 * 1024);
            String jvm = System.getProperty("java.version");

            // Unicode icons for hardware stats
            hwPanel.add(createHwRow("\u25A0", os + " (" + arch + ")")); // Square block for OS
            hwPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            hwPanel.add(createHwRow("\u2022", cores + " CPU Cores")); // Bullet for CPU
            hwPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            hwPanel.add(createHwRow("\u2022", Math.max(1, maxMem) + " GB Alloc. RAM")); // Bullet for RAM
            hwPanel.add(Box.createRigidArea(new Dimension(0, 6)));
            hwPanel.add(createHwRow("\u25B6", "Java " + jvm)); // Arrow for Java

            return hwPanel;
        }

        private JPanel createHwRow(String icon, String text) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(BG_DARK);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel iconLbl = new JLabel(icon);
            iconLbl.setForeground(ACCENT);
            JLabel textLbl = new JLabel(text);
            textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            textLbl.setForeground(TEXT_MUTED);
            
            row.add(iconLbl, BorderLayout.WEST);
            row.add(textLbl, BorderLayout.CENTER);
            return row;
        }

        private JPanel createDashboard() {
            JPanel p = new JPanel(new BorderLayout(15, 15)); p.setBackground(BG_DARK); p.setBorder(new EmptyBorder(20,20,20,20));
            
            // Control Panel (West)
            JPanel controls = new JPanel(new GridBagLayout()); controls.setBackground(BG_PANEL);
            controls.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(15,15,15,15)));
            GridBagConstraints gbc = new GridBagConstraints(); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5,5,5,5);

            JPanel algoGrid = new JPanel(new GridLayout(0, 2, 5, 5)); algoGrid.setBackground(BG_PANEL);
            for (String algo : Model.ALGORITHMS) {
                JCheckBox cb = new JCheckBox(algo); cb.setFocusPainted(false); algoChecks.put(algo, cb); algoGrid.add(cb);
            }
            algoChecks.get("Merge Sort").setSelected(true); algoChecks.get("Quick Sort").setSelected(true);

            gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; controls.add(new JLabel("Target Algorithms:"), gbc);
            gbc.gridy=1; controls.add(new JScrollPane(algoGrid) { @Override public Dimension getPreferredSize() { return new Dimension(320, 150); } }, gbc);
            selectAllBtn = createStyledButton("Select All", BG_DARK); gbc.gridy=2; controls.add(selectAllBtn, gbc);

            gbc.gridwidth=1;
            gbc.gridy=3; gbc.gridx=0; controls.add(new JLabel("Min Size:"), gbc); gbc.gridx=1; controls.add(minSizeField = new JTextField("1000"), gbc);
            gbc.gridy=4; gbc.gridx=0; controls.add(new JLabel("Max Size:"), gbc); gbc.gridx=1; controls.add(maxSizeField = new JTextField("20000"), gbc);
            gbc.gridy=5; gbc.gridx=0; controls.add(new JLabel("Step Size:"), gbc); gbc.gridx=1; controls.add(stepField = new JTextField("1000"), gbc);
            gbc.gridy=6; gbc.gridx=0; controls.add(new JLabel("Data Order:"), gbc); gbc.gridx=1; controls.add(orderCombo = new JComboBox<>(Model.ORDERS), gbc);
            
            showTheoryCheck = new JCheckBox("Show Theoretical O(n) Curves (On Hover)"); showTheoryCheck.setSelected(true);
            gbc.gridy=7; gbc.gridx=0; gbc.gridwidth=2; controls.add(showTheoryCheck, gbc);

            analyzeBtn = createStyledButton("Run Analytics", ACCENT); analyzeBtn.setForeground(BG_DARK);
            reportBtn = createStyledButton("Export Report (HTML/CSV)", BG_DARK);
            gbc.gridy=8; controls.add(analyzeBtn, gbc); gbc.gridy=9; controls.add(reportBtn, gbc);

            progressBar = new JProgressBar(); progressBar.setVisible(false); progressBar.setForeground(ACCENT);
            gbc.gridy=10; controls.add(progressBar, gbc);
            
            // Main Dashboard Area (Center)
            JPanel center = new JPanel(new BorderLayout(10, 10)); center.setBackground(BG_DARK);
            
            // Stats Header
            JPanel stats = new JPanel(new GridLayout(1, 4, 15, 0)); stats.setBackground(BG_DARK);
            statMedian = createStatCard(stats, "Aggregate Median", "--");
            statAvg = createStatCard(stats, "Aggregate Average", "--");
            statMem = createStatCard(stats, "Peak Memory Used", "--");
            createStatCard(stats, "Sample Iterations", String.valueOf(Model.MEASURE_RUNS));
            center.add(stats, BorderLayout.NORTH);

            // Interactive Chart Wrapper with Statistic Selector
            JPanel chartControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            chartControlPanel.setBackground(BG_DARK);
            JLabel statLbl = new JLabel("Plot Statistic: "); statLbl.setForeground(TEXT_MAIN);
            statCombo = new JComboBox<>(new String[]{"Median Time", "Avg Time", "Min Time", "Max Time"});
            chartControlPanel.add(statLbl); chartControlPanel.add(statCombo);
            
            JPanel chartWrapper = new JPanel(new BorderLayout());
            chartWrapper.setBackground(BG_DARK);
            chartPanel = new InteractiveChartPanel();
            chartWrapper.add(chartControlPanel, BorderLayout.NORTH);
            chartWrapper.add(chartPanel, BorderLayout.CENTER);
            
            String[] cols = {"Algorithm", "Input Size", "Median Time", "Avg Time", "Min Time", "Max Time", "Memory"};
            tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
            resultsTable = new JTable(tableModel); resultsTable.setRowHeight(25); resultsTable.setGridColor(BORDER_COLOR);
            DefaultTableCellRenderer rr = new DefaultTableCellRenderer(); rr.setHorizontalAlignment(JLabel.RIGHT);
            for(int i=1; i<7; i++) resultsTable.getColumnModel().getColumn(i).setCellRenderer(rr);
            
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartWrapper, new JScrollPane(resultsTable));
            split.setResizeWeight(0.7); split.setBorder(null); split.setDividerSize(5);

            center.add(split, BorderLayout.CENTER);
            p.add(controls, BorderLayout.WEST); p.add(center, BorderLayout.CENTER);
            return p;
        }

        private JButton createStyledButton(String text, Color bg) {
            JButton b = new JButton(text); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            b.setBackground(bg); b.setForeground(TEXT_MAIN); b.setFocusPainted(false);
            b.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR), new EmptyBorder(8, 15, 8, 15)));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR)); return b;
        }

        private JLabel createStatCard(JPanel parent, String title, String val) {
            JPanel card = new JPanel(new BorderLayout()); card.setBackground(BG_PANEL);
            card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(15, 15, 15, 15)));
            JLabel t = new JLabel(title, SwingConstants.CENTER); t.setForeground(TEXT_MUTED);
            JLabel v = new JLabel(val, SwingConstants.CENTER); v.setFont(new Font("Segoe UI", Font.BOLD, 18)); v.setForeground(ACCENT);
            card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.CENTER); parent.add(card); return v;
        }

        private JPanel createVisualizer() {
            JPanel p = new JPanel(new BorderLayout(10,10)); p.setBackground(BG_DARK); p.setBorder(new EmptyBorder(20,20,20,20));
            JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); top.setBackground(BG_PANEL);
            top.setBorder(new LineBorder(BORDER_COLOR, 1, true));

            visAlgoCombo = new JComboBox<>(new String[]{"Bubble Sort", "Selection Sort", "Insertion Sort", "Merge Sort", "Quick Sort", "Heap Sort"});
            btnPlay = createStyledButton("Play", ACCENT); btnPlay.setForeground(BG_DARK);
            btnPause = createStyledButton("Pause", BG_DARK); btnStep = createStyledButton("Step", BG_DARK); btnReset = createStyledButton("Reset", BG_DARK);
            speedSlider = new JSlider(1, 100, 50); speedSlider.setBackground(BG_PANEL);
            
            top.add(new JLabel("Algorithm:")); top.add(visAlgoCombo); top.add(btnReset); top.add(btnPlay); top.add(btnPause); top.add(btnStep);
            top.add(new JLabel("Speed:")); top.add(speedSlider);
            
            visualizerPanel = new SortingVisualizerPanel(this);
            p.add(top, BorderLayout.NORTH); p.add(visualizerPanel, BorderLayout.CENTER);
            return p;
        }

        private JPanel createEncyclopedia() {
            JPanel p = new JPanel(new BorderLayout(10,10)); p.setBackground(BG_DARK); p.setBorder(new EmptyBorder(20,20,20,20));
            encyList = new JList<>(Model.ALGORITHMS); encyList.setBackground(BG_PANEL); encyList.setForeground(TEXT_MAIN);
            encyList.setFont(new Font("Segoe UI", Font.PLAIN, 14)); encyList.setFixedCellHeight(30);
            
            encyContent = new JEditorPane("text/html", ""); encyContent.setEditable(false);
            encyContent.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
            encyContent.setBackground(BG_PANEL); encyContent.setForeground(TEXT_MAIN);
            
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(encyList), new JScrollPane(encyContent));
            split.setDividerLocation(250); split.setBorder(null);
            p.add(split, BorderLayout.CENTER); return p;
        }

        private JPanel createHistoryPanel() {
            JPanel p = new JPanel(new BorderLayout(10,10)); p.setBackground(BG_DARK); p.setBorder(new EmptyBorder(20,20,20,20));
            historyModel = new DefaultListModel<>();
            historyList = new JList<>(historyModel); historyList.setBackground(BG_PANEL); historyList.setForeground(TEXT_MAIN);
            historyList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            p.add(new JScrollPane(historyList), BorderLayout.CENTER);
            return p;
        }
    }

    // =====================================================================================
    // MVC: CONTROLLER
    // =====================================================================================
    static class Controller {
        private final Model model; private final View view; private final JFrame frame;
        private BenchmarkWorker currentWorker;

        public Controller(Model model, View view, JFrame frame) {
            this.model = model; this.view = view; this.frame = frame;
            
            view.analyzeBtn.addActionListener(e -> startBenchmark());
            view.reportBtn.addActionListener(e -> exportReport());
            view.selectAllBtn.addActionListener(e -> {
                boolean allSelected = view.algoChecks.values().stream().allMatch(JCheckBox::isSelected);
                view.algoChecks.values().forEach(cb -> cb.setSelected(!allSelected));
            });
            view.showTheoryCheck.addActionListener(e -> view.chartPanel.setShowTheory(view.showTheoryCheck.isSelected()));
            view.statCombo.addActionListener(e -> view.chartPanel.setStatistic((String)view.statCombo.getSelectedItem()));

            // Visualizer Listeners
            view.btnPlay.addActionListener(e -> view.visualizerPanel.play());
            view.btnPause.addActionListener(e -> view.visualizerPanel.pause());
            view.btnStep.addActionListener(e -> view.visualizerPanel.step());
            view.btnReset.addActionListener(e -> view.visualizerPanel.reset((String)view.visAlgoCombo.getSelectedItem()));
            view.visAlgoCombo.addActionListener(e -> view.visualizerPanel.reset((String)view.visAlgoCombo.getSelectedItem()));

            // Encyc Listeners
            view.encyList.addListSelectionListener(e -> updateEncyclopedia());
            view.encyList.setSelectedIndex(0);
        }

        private void startBenchmark() {
            List<String> selected = new ArrayList<>();
            for(Map.Entry<String, JCheckBox> e : view.algoChecks.entrySet()) if(e.getValue().isSelected()) selected.add(e.getKey());

            if (selected.isEmpty()) { JOptionPane.showMessageDialog(frame, "Select at least one algorithm."); return; }

            try {
                int min = Integer.parseInt(view.minSizeField.getText()), max = Integer.parseInt(view.maxSizeField.getText()), step = Integer.parseInt(view.stepField.getText());
                if (min <= 0 || max <= min || step <= 0) throw new NumberFormatException();
                
                boolean hasSlow = selected.stream().anyMatch(a -> a.contains("Bubble") || a.contains("Selection") || a.contains("Insertion"));
                if (hasSlow && max > 20000 && JOptionPane.showConfirmDialog(frame, "O(n²) algorithms selected with size > 20,000. Continue?", "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

                view.analyzeBtn.setEnabled(false); view.progressBar.setVisible(true);
                view.tableModel.setRowCount(0); model.clearResults(); view.chartPanel.clearData();

                currentWorker = new BenchmarkWorker(selected, min, max, step, (String) view.orderCombo.getSelectedItem());
                currentWorker.execute();
            } catch (Exception e) { JOptionPane.showMessageDialog(frame, "Invalid input ranges."); }
        }

        class BenchmarkWorker extends SwingWorker<Void, Object[]> {
            List<String> algos; int min, max, step; String order; long totalOps, currentOp = 0; BenchmarkResult lastResult;
            public BenchmarkWorker(List<String> a, int min, int max, int step, String order) {
                this.algos = a; this.min = min; this.max = max; this.step = step; this.order = order;
                totalOps = algos.size() * (((max - min) / step) + 1);
            }
            @Override protected Void doInBackground() throws Exception {
                for (String algo : algos) {
                    int currentMax = algo.equals("Fibonacci Recursive") ? Math.min(max, 40) : max;
                    for (int size = min; size <= currentMax; size += step) {
                        long[] times = new long[Model.MEASURE_RUNS];
                        for (int i = 0; i < 2; i++) { int[] data = model.generateInputData(Math.min(size, 500), order); model.runAlgoPrepared(algo, data, data.length>0?data[0]:0, Math.min(size, 500)); }
                        System.gc(); Thread.sleep(5);
                        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                        for (int i = 0; i < Model.MEASURE_RUNS; i++) {
                            int[] data = model.generateInputData(size, order); int target = data.length>0 ? data[data.length - 1] : 0;
                            if (algo.equals("Binary Search")) Arrays.sort(data);
                            long start = System.nanoTime(); model.runAlgoPrepared(algo, data, target, size); long end = System.nanoTime();
                            times[i] = end - start;
                        }
                        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        Arrays.sort(times); long minT = times[0], maxT = times[Model.MEASURE_RUNS-1], median = times[Model.MEASURE_RUNS/2], sum=0;
                        for(long t:times) sum+=t;
                        lastResult = new BenchmarkResult(algo, size, sum/Model.MEASURE_RUNS, median, minT, maxT, Math.max(0, memAfter - memBefore));
                        model.addResult(algo, lastResult); publish(new Object[]{lastResult});
                        currentOp++; setProgress((int) ((currentOp * 100) / totalOps));
                    }
                } return null;
            }
            @Override protected void process(List<Object[]> chunks) {
                for (Object[] row : chunks) {
                    BenchmarkResult r = (BenchmarkResult) row[0];
                    view.tableModel.addRow(new Object[]{ r.algorithm, r.size, formatTime(r.medianTime), formatTime(r.avgTime), formatTime(r.minTime), formatTime(r.maxTime), formatMemory(r.memoryBytes) });
                }
                view.chartPanel.updateData(model.getResults()); view.progressBar.setValue(getProgress());
                if (lastResult != null) { view.statMedian.setText(formatTime(lastResult.medianTime)); view.statAvg.setText(formatTime(lastResult.avgTime)); view.statMem.setText(formatMemory(lastResult.memoryBytes)); }
            }
            @Override protected void done() {
                view.analyzeBtn.setEnabled(true); view.progressBar.setVisible(false);
                String conf = String.format("Size: %d-%d, Order: %s", min, max, order);
                model.saveToHistory(String.join(", ", algos), conf);
                view.historyModel.add(0, model.history.get(0).timestamp + " | " + conf + " | Algos: " + String.join(", ", algos));
            }
        }
        
        // --- Export Engine (HTML/CSV) ---
        private void exportReport() {
            if (model.getResults().isEmpty()) { JOptionPane.showMessageDialog(frame, "No data to export."); return; }
            JFileChooser fc = new JFileChooser(); fc.setDialogTitle("Save Professional Report");
            fc.setSelectedFile(new File("AlgoBench_Report.html"));
            if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try (PrintWriter pw = new PrintWriter(f)) {
                    if(f.getName().endsWith(".csv")) {
                        pw.println("Algorithm,Size,MedianTime_ns,AvgTime_ns,MinTime_ns,MaxTime_ns,Memory_Bytes");
                        for(List<BenchmarkResult> list : model.getResults().values()) for(BenchmarkResult r : list) pw.printf("%s,%d,%d,%d,%d,%d,%d\n", r.algorithm, r.size, r.medianTime, r.avgTime, r.minTime, r.maxTime, r.memoryBytes);
                    } else {
                        pw.println("<html><head><style>body{font-family: Arial, sans-serif; padding: 20px;} table{width:100%; border-collapse: collapse; margin-top:20px;} th, td{border: 1px solid #ddd; padding: 8px; text-align: left;} th{background-color: #f2f2f2;} h1, h2{color: #2c3e50;}</style></head><body>");
                        pw.println("<h1>Algorithm Benchmark Professional Report</h1><p>Generated by Algorithm Performance Visualizer on " + new Date().toString() + "</p>");
                        pw.println("<h2>System Environment</h2><p>OS: " + System.getProperty("os.name") + " | Cores: " + Runtime.getRuntime().availableProcessors() + "</p>");
                        pw.println("<h2>Executive Summary</h2><table><tr><th>Algorithm</th><th>Max Size Tested</th><th>Median Time at Max</th><th>Time Complexity Class</th></tr>");
                        for (Map.Entry<String, List<BenchmarkResult>> entry : model.getResults().entrySet()) {
                            BenchmarkResult last = entry.getValue().get(entry.getValue().size()-1);
                            pw.printf("<tr><td>%s</td><td>%d</td><td>%s</td><td>%s</td></tr>", last.algorithm, last.size, formatTime(last.medianTime), getOClass(last.algorithm));
                        }
                        pw.println("</table><h2>Detailed Raw Data Matrix</h2><table><tr><th>Algorithm</th><th>Input Size</th><th>Median Time</th><th>Avg Time</th><th>Memory Allocation</th></tr>");
                        for(List<BenchmarkResult> list : model.getResults().values()) for(BenchmarkResult r : list) pw.printf("<tr><td>%s</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>", r.algorithm, r.size, formatTime(r.medianTime), formatTime(r.avgTime), formatMemory(r.memoryBytes));
                        pw.println("</table></body></html>");
                    }
                    JOptionPane.showMessageDialog(frame, "Report saved! Open the HTML file in any browser to view or print to PDF.");
                } catch (Exception e) { JOptionPane.showMessageDialog(frame, "Export failed: " + e.getMessage()); }
            }
        }
        
        private String getOClass(String algo) {
            if(algo.contains("Bubble")||algo.contains("Selection")||algo.contains("Insertion")) return "O(n²)";
            if(algo.contains("Merge")||algo.contains("Quick")||algo.contains("Heap")) return "O(n log n)";
            if(algo.contains("Binary")) return "O(log n)";
            return "O(n)";
        }

        // --- Encyclopedia Update ---
        private void updateEncyclopedia() {
            String algo = view.encyList.getSelectedValue(); if(algo == null) return;
            String b="O(1)", a="O(n)", w="O(n)", s="O(1)", st="N/A", ip="N/A", desc="Description missing.";
            
            switch (algo) {
                case "Linear Search": 
                    b="O(1)"; a="O(n)"; w="O(n)"; s="O(1)"; st="Yes"; ip="Yes"; 
                    desc="Checks each element sequentially until the target is found. Simple but slow for large datasets."; 
                    break;
                case "Binary Search": 
                    b="O(1)"; a="O(log n)"; w="O(log n)"; s="O(1)"; st="N/A"; ip="Yes"; 
                    desc="Efficiently finds an item from a sorted list by repeatedly halving the search space."; 
                    break;
                case "Bubble Sort": 
                    b="O(n)"; a="O(n²)"; w="O(n²)"; s="O(1)"; st="Yes"; ip="Yes"; 
                    desc="Simplest sorting algorithm that repeatedly swaps adjacent elements if they are in the wrong order."; 
                    break;
                case "Selection Sort": 
                    b="O(n²)"; a="O(n²)"; w="O(n²)"; s="O(1)"; st="No"; ip="Yes"; 
                    desc="Sorts an array by repeatedly finding the minimum element from the unsorted part and moving it to the beginning."; 
                    break;
                case "Insertion Sort": 
                    b="O(n)"; a="O(n²)"; w="O(n²)"; s="O(1)"; st="Yes"; ip="Yes"; 
                    desc="Builds the final sorted array one item at a time. Highly efficient for small or mostly sorted data."; 
                    break;
                case "Merge Sort": 
                    b="O(n log n)"; a="O(n log n)"; w="O(n log n)"; s="O(n)"; st="Yes"; ip="No"; 
                    desc="Divide and conquer algorithm that splits the array in halves, sorts them recursively, and merges them back."; 
                    break;
                case "Quick Sort": 
                    b="O(n log n)"; a="O(n log n)"; w="O(n²)"; s="O(log n)"; st="No"; ip="Yes"; 
                    desc="Partitions the array around a pivot element. Often the fastest general-purpose sort in practice due to cache efficiency."; 
                    break;
                case "Heap Sort": 
                    b="O(n log n)"; a="O(n log n)"; w="O(n log n)"; s="O(1)"; st="No"; ip="Yes"; 
                    desc="Uses a binary heap data structure to sort elements in-place with guaranteed O(n log n) performance."; 
                    break;
                case "Counting Sort": 
                    b="O(n + k)"; a="O(n + k)"; w="O(n + k)"; s="O(n + k)"; st="Yes"; ip="No"; 
                    desc="Non-comparison integer sort that operates by counting the number of objects having distinct key values."; 
                    break;
                case "Radix Sort": 
                    b="O(d(n + k))"; a="O(d(n + k))"; w="O(d(n + k))"; s="O(n + k)"; st="Yes"; ip="No"; 
                    desc="Avoids comparison by distributing elements into buckets according to their individual digits or radix."; 
                    break;
                case "Fibonacci Recursive": 
                    b="O(1)"; a="O(2ⁿ)"; w="O(2ⁿ)"; s="O(n)"; st="N/A"; ip="N/A"; 
                    desc="Calculates the nth Fibonacci number by recursively calling itself. Demonstrates extreme exponential time overhead."; 
                    break;
                case "Fibonacci DP": 
                    b="O(n)"; a="O(n)"; w="O(n)"; s="O(1)"; st="N/A"; ip="N/A"; 
                    desc="Calculates the nth Fibonacci number using a bottom-up dynamic programming approach, reducing exponential time to linear time."; 
                    break;
            }
            
            view.encyContent.setText(String.format("<html><body style='font-family:Segoe UI, sans-serif; padding:15px; color:#cdd6f4;'><h2>%s</h2><hr color='#313244'>" +
                "<p><b>Description:</b> %s</p><br>" +
                "<table width='100%%' style='border:1px solid #313244; border-collapse:collapse;' cellpadding='8'>" +
                "<tr style='background-color:#181825;'><td><b>Best Time</b></td><td>%s</td><td><b>Stable</b></td><td>%s</td></tr>" +
                "<tr><td><b>Avg Time</b></td><td>%s</td><td><b>In-Place</b></td><td>%s</td></tr>" +
                "<tr style='background-color:#181825;'><td><b>Worst Time</b></td><td>%s</td><td><b>Space</b></td><td>%s</td></tr>" +
                "</table></body></html>", algo, desc, b, st, a, ip, w, s));
        }
    }

    // =====================================================================================
    // CUSTOM COMPONENTS: Interactive Chart & Visualizer
    // =====================================================================================
    static class InteractiveChartPanel extends JPanel implements MouseMotionListener, MouseListener {
        private Map<String, List<BenchmarkResult>> dataMap = new LinkedHashMap<>();
        private final Color[] PALETTE = {new Color(137,180,250), new Color(243,139,168), new Color(166,227,161), new Color(249,226,175), new Color(203,166,247), Color.CYAN};
        private int mouseX = -1, mouseY = -1; private boolean showTheory = true;
        
        // Interactive Features
        private String selectedStat = "Median Time";
        private Set<String> hiddenAlgos = new HashSet<>();
        private Map<String, Rectangle> legendBounds = new HashMap<>();

        public InteractiveChartPanel() { 
            setBorder(BorderFactory.createTitledBorder(new LineBorder(BORDER_COLOR), "Interactive Performance Graph (Hover for Details, Click Legend to Toggle)", TitledBorder.LEFT, TitledBorder.TOP, null, ACCENT));
            setBackground(BG_PANEL); 
            addMouseMotionListener(this);
            addMouseListener(this);
        }
        public void updateData(Map<String, List<BenchmarkResult>> d) { this.dataMap = new LinkedHashMap<>(d); hiddenAlgos.clear(); repaint(); }
        public void clearData() { this.dataMap.clear(); hiddenAlgos.clear(); repaint(); }
        public void setShowTheory(boolean s) { this.showTheory = s; repaint(); }
        public void setStatistic(String stat) { this.selectedStat = stat; repaint(); }

        private long getStatValue(BenchmarkResult res) {
            switch(selectedStat) {
                case "Avg Time": return res.avgTime;
                case "Min Time": return res.minTime;
                case "Max Time": return res.maxTime;
                default: return res.medianTime;
            }
        }

        @Override public void mouseDragged(MouseEvent e) {}
        @Override public void mouseMoved(MouseEvent e) { mouseX = e.getX(); mouseY = e.getY(); repaint(); }
        @Override public void mouseClicked(MouseEvent e) {
            for (Map.Entry<String, Rectangle> entry : legendBounds.entrySet()) {
                if (entry.getValue().contains(e.getPoint())) {
                    if (hiddenAlgos.contains(entry.getKey())) hiddenAlgos.remove(entry.getKey());
                    else hiddenAlgos.add(entry.getKey());
                    repaint();
                    return;
                }
            }
        }
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); if (dataMap.isEmpty()) return;
            Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int pM = 70, w = getWidth(), h = getHeight(); long maxTime = 1; int maxSize = 1;
            legendBounds.clear();

            // Calculate max bounds ignoring hidden algorithms
            for (Map.Entry<String, List<BenchmarkResult>> entry : dataMap.entrySet()) {
                if (hiddenAlgos.contains(entry.getKey())) continue;
                for (BenchmarkResult res : entry.getValue()) {
                    long val = getStatValue(res);
                    if (val > maxTime) maxTime = val; 
                    if (res.size > maxSize) maxSize = res.size;
                }
            }

            // Draw Grid & Axes
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i <= 5; i++) {
                int y = h - pM - (i * (h - 2 * pM) / 5), x = pM + (i * (w - 2 * pM) / 5);
                g2d.setColor(BORDER_COLOR); g2d.drawLine(pM, y, w - pM, y); g2d.drawLine(x, h - pM, x, pM);
                g2d.setColor(TEXT_MUTED);
                String yL = formatTime((maxTime * i) / 5.0); g2d.drawString(yL, pM - g2d.getFontMetrics().stringWidth(yL) - 10, y + 4);
                g2d.drawString(String.valueOf((maxSize * i) / 5), x - 10, h - pM + 20);
            }
            g2d.setStroke(new BasicStroke(2f)); g2d.setColor(TEXT_MAIN); g2d.drawLine(pM, pM, pM, h - pM); g2d.drawLine(pM, h - pM, w - pM, h - pM);
            
            String hoverText = null; int hX=0, hY=0; Color hColor = Color.WHITE;

            // Draw Data Lines & Theory Curves
            int cIdx = 0, legY = pM;
            for (Map.Entry<String, List<BenchmarkResult>> entry : dataMap.entrySet()) {
                String algoName = entry.getKey();
                boolean isHidden = hiddenAlgos.contains(algoName);
                Color c = PALETTE[cIdx % PALETTE.length]; 
                
                // Draw Interactive Legend
                g2d.setColor(isHidden ? BORDER_COLOR : c); 
                g2d.fillRect(w - 140, legY, 12, 12);
                g2d.setColor(isHidden ? TEXT_MUTED : TEXT_MAIN); 
                g2d.drawString(algoName, w - 120, legY + 11);
                
                // Store bounds for click detection
                legendBounds.put(algoName, new Rectangle(w - 140, legY, 130, 15));
                legY += 20;
                cIdx++;

                if (isHidden) continue;

                List<BenchmarkResult> pts = entry.getValue(); g2d.setColor(c); g2d.setStroke(new BasicStroke(2.5f));
                
                // Theory Curve (On Hover of Legend or if enabled)
                if(showTheory) {
                    g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100)); // Transparent
                    if(algoName.contains("Bubble") || algoName.contains("Selection") || algoName.contains("Insertion")) {
                        // Plot O(n^2) normalized to last point
                        if(!pts.isEmpty()) {
                            BenchmarkResult last = pts.get(pts.size()-1); double constant = (double)getStatValue(last) / ((double)last.size * last.size);
                            int prevX = pM, prevY = h - pM;
                            for(int s=0; s<=maxSize; s+=maxSize/20) {
                                int x = pM + (int)((double)s/maxSize*(w-2*pM)), y = h - pM - (int)(((constant * s * s)/maxTime)*(h-2*pM));
                                g2d.drawLine(prevX, prevY, x, y); prevX=x; prevY=y;
                            }
                        }
                    }
                    g2d.setStroke(new BasicStroke(2.5f)); g2d.setColor(c);
                }

                // Actual Data
                for (int i = 0; i < pts.size(); i++) {
                    BenchmarkResult p1 = pts.get(i);
                    int x1 = pM + (int) ((double) p1.size / maxSize * (w - 2 * pM));
                    int y1 = h - pM - (int) ((double) getStatValue(p1) / maxTime * (h - 2 * pM));
                    
                    if (i < pts.size() - 1) {
                        BenchmarkResult p2 = pts.get(i + 1);
                        int x2 = pM + (int) ((double) p2.size / maxSize * (w - 2 * pM));
                        int y2 = h - pM - (int) ((double) getStatValue(p2) / maxTime * (h - 2 * pM));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                    g2d.fillOval(x1 - 4, y1 - 4, 8, 8);

                    // Check Hover
                    if (Math.abs(mouseX - x1) < 10 && Math.abs(mouseY - y1) < 10) {
                        hoverText = String.format("%s | Size: %d | Time: %s", p1.algorithm, p1.size, formatTime(getStatValue(p1)));
                        hX = x1; hY = y1; hColor = c;
                    }
                }
            }

            // Draw Tooltip Box
            if (hoverText != null) {
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12)); int tw = g2d.getFontMetrics().stringWidth(hoverText);
                g2d.setColor(new Color(24, 24, 36, 220)); g2d.fillRoundRect(hX - tw/2 - 10, hY - 30, tw + 20, 20, 5, 5);
                g2d.setColor(hColor); g2d.drawRoundRect(hX - tw/2 - 10, hY - 30, tw + 20, 20, 5, 5);
                g2d.setColor(TEXT_MAIN); g2d.drawString(hoverText, hX - tw/2, hY - 15);
            }
        }
    }

    static class SortingVisualizerPanel extends JPanel {
        private int[] array; private int currentIndex = -1, comparingIndex = -1;
        private volatile boolean isRunning = false, isPaused = false, stepRequested = false;
        private final Object lock = new Object(); private View viewRef; private Thread sortThread;

        public SortingVisualizerPanel(View v) { this.viewRef = v; reset("Bubble Sort"); }
        public void reset(String algo) { stop(); array = new int[70]; for (int i = 0; i < array.length; i++) array[i] = (int)(Math.random() * 300) + 20; currentIndex = -1; comparingIndex = -1; repaint(); }
        public void play() {
            if (sortThread != null && sortThread.isAlive()) { synchronized(lock) { isPaused = false; lock.notifyAll(); } }
            else {
                String algo = (String) viewRef.visAlgoCombo.getSelectedItem(); isRunning = true; isPaused = false;
                sortThread = new Thread(() -> {
                    try {
                        if(algo.equals("Bubble Sort")) visBubble(); else if(algo.equals("Selection Sort")) visSelection();
                        else if(algo.equals("Insertion Sort")) visInsertion(); else if(algo.equals("Quick Sort")) visQuick(0, array.length-1);
                        else if(algo.equals("Merge Sort")) visMerge(0, array.length-1); else if(algo.equals("Heap Sort")) visHeap();
                    } catch (InterruptedException e) {}
                    currentIndex = -1; comparingIndex = -1; isRunning = false; repaint();
                }); sortThread.start();
            }
        }
        public void pause() { synchronized(lock) { isPaused = true; } }
        public void step() { synchronized(lock) { isPaused = true; stepRequested = true; lock.notifyAll(); } }
        public void stop() { isRunning = false; if (sortThread != null) sortThread.interrupt(); }

        private void tick() throws InterruptedException {
            if (!isRunning) throw new InterruptedException();
            synchronized (lock) { while (isPaused && !stepRequested) lock.wait(); stepRequested = false; }
            Thread.sleep(101 - viewRef.speedSlider.getValue()); repaint();
        }

        private void visBubble() throws InterruptedException { for (int i = 0; i < array.length - 1; i++) for (int j = 0; j < array.length - i - 1; j++) { currentIndex = j; comparingIndex = j + 1; tick(); if (array[j] > array[j + 1]) { int t = array[j]; array[j] = array[j+1]; array[j+1] = t; } } }
        private void visSelection() throws InterruptedException { for (int i = 0; i < array.length - 1; i++) { int minIdx = i; for (int j = i + 1; j < array.length; j++) { currentIndex = j; comparingIndex = minIdx; tick(); if (array[j] < array[minIdx]) minIdx = j; } int t = array[minIdx]; array[minIdx] = array[i]; array[i] = t; } }
        private void visInsertion() throws InterruptedException { for (int i = 1; i < array.length; ++i) { int key = array[i], j = i - 1; while (j >= 0 && array[j] > key) { currentIndex = j; comparingIndex = j + 1; tick(); array[j + 1] = array[j]; j--; } array[j + 1] = key; } }
        private void visQuick(int low, int high) throws InterruptedException { if(low < high) { int p = array[high], i = low - 1; for(int j=low; j<high; j++) { currentIndex = j; comparingIndex = high; tick(); if(array[j] < p) { i++; int t = array[i]; array[i] = array[j]; array[j] = t; } } int t = array[i+1]; array[i+1] = array[high]; array[high] = t; visQuick(low, i); visQuick(i+2, high); } }
        private void visMerge(int l, int r) throws InterruptedException { if(l < r) { int m = l + (r-l)/2; visMerge(l, m); visMerge(m+1, r); int n1 = m - l + 1, n2 = r - m, L[] = new int[n1], R[] = new int[n2]; for(int i=0; i<n1; ++i) L[i] = array[l+i]; for(int j=0; j<n2; ++j) R[j] = array[m+1+j]; int i=0, j=0, k=l; while(i<n1 && j<n2) { currentIndex = k; tick(); if(L[i] <= R[j]) { array[k] = L[i]; i++; } else { array[k] = R[j]; j++; } k++; } while(i<n1) { array[k] = L[i]; i++; k++; tick(); } while(j<n2) { array[k] = R[j]; j++; k++; tick(); } } }
        private void visHeap() throws InterruptedException { int n = array.length; for (int i = n / 2 - 1; i >= 0; i--) visHeapify(n, i); for (int i = n - 1; i > 0; i--) { int t = array[0]; array[0] = array[i]; array[i] = t; currentIndex = i; tick(); visHeapify(i, 0); } }
        private void visHeapify(int n, int i) throws InterruptedException { int max = i, l = 2*i + 1, r = 2*i + 2; if(l < n && array[l] > array[max]) max = l; if(r < n && array[r] > array[max]) max = r; if(max != i) { int t = array[i]; array[i] = array[max]; array[max] = t; comparingIndex = max; tick(); visHeapify(n, max); } }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); if(array == null) return;
            Graphics2D g2d = (Graphics2D) g; setBackground(BG_DARK); int w = getWidth() / array.length;
            for (int i = 0; i < array.length; i++) {
                if(i == currentIndex) g2d.setColor(new Color(243,139,168)); else if(i == comparingIndex) g2d.setColor(new Color(249,226,175)); else g2d.setColor(ACCENT);
                g2d.fillRect(i * w, getHeight() - array[i], w - 2, array[i]);
            }
        }
    }
}