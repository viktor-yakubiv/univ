import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;


public class ThreadsSampleManager {

    /* Global configuration */

    // Slider configuration
    public static int sliderMinValue     = 0;
    public static int sliderMaxValue     = 100;

    // Step of each thread for an iteration
    public static int threadStep = 1;

    //
    public static int threadsControlValue = 10000;

    // Threads aim
    public static int threadAim  = 40 * threadsControlValue;
    public static int rightThreadAim = 90;

    // Slider update time (ms)
    public static int updateTime = 5;


    /* Threads configuration */

    /**
     * Global semaphore.
     * Is using in semaphore mode.
     */
    Integer semaphore;

    /**
     * Slider value controlling variable
     *
     * Jobs change this variable. Then special thread updates slider value with this.
     */
    AtomicInteger sliderValueControl;
    int sliderInitialValue;

    /**
     * Thread workers
     * Execute main thread task - changing slider value.
     */
    class Job extends Thread {
        // Job control variable
        AtomicInteger controls;

        // Job step and direction
        int step;

        // Job endpoints
        int aim;

        /**
         *
         * @param controlVar a variable job updates in main loop
         * @param step       step and direction of updating variable
         * @param aim        an endpoint for stopping job
         */
        public Job(AtomicInteger controlVar, int step, int aim) {//, int minValue, int maxValue) {
            super();
            this.controls = controlVar;
            this.step = step;
            this.aim = aim;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                int result;
                synchronized (this.controls) {
                    result = controls.addAndGet(step);
                }
                if (Math.abs(result) >= Math.abs(aim))
                    break;

                yield();
            }
        }
    }

    // Threads
    Job leftThread, rightThread;

    /**
     * Starts needed jobs for non semaphore mode
     * Calls from running buttons action controls.
     * @see this.runningButtonActionListener()
     */
    private boolean startJobs(int startMask) {
        // Before starting check
        if (semaphore > 0)
            return false;

        // Starting threads.
        System.out.println(startMask & 0x10);
        if ((startMask & 0x10) != 0) {
            leftThread = new Job(sliderValueControl, -1 * threadStep, threadAim);
            leftThread.setPriority((Integer) leftPriorityControl.getValue());
            leftThread.start();
        }
        System.out.println(startMask & 0x01);
        if ((startMask & 0x01) != 0) {
            rightThread = new Job(sliderValueControl, threadStep, threadAim);
            rightThread.setPriority((Integer) rightPriorityControl.getValue());
            rightThread.start();
        }

        // Reading current slider value
        sliderInitialValue = slider.getValue();

        // Creating threads work listener
        uiUpdater = new SliderControlListener(sliderValueControl);
        uiUpdater.start();

        semaphore++;
        return true;
    }

    /**
     * Stops all jobs for non semaphore mode
     * Calls from running buttons action controls.
     * @see this.runningButtonActionListener()
     */
    private void stopJobs() {
        if (leftThread != null && leftThread.getState() == Thread.State.RUNNABLE)
            leftThread.interrupt();
        if (rightThread != null && rightThread.getState() == Thread.State.RUNNABLE)
            rightThread.interrupt();
        if (uiUpdater != null && uiUpdater.getState() == Thread.State.RUNNABLE)
            uiUpdater.interrupt();
        semaphore = 0;
    }


    /* GUI Components */

    /* Global GUI Frame */
    JFrame frame;

    /**
     * Controls mode of program execution:
     * if checked - semaphore enabled and we can start only one thread
     * else we can start both threads
     */
    JCheckBox modeControl;

    /**
     * Slider control for thread working showing.
     */
    JSlider slider;

    /**
     * Priority controls spinners.
     * Allow user to set priority for each thread before start.
     */
    JSpinner leftPriorityControl, rightPriorityControl;

    /**
     * Threads execution control buttons, for each and for both threads.
     * Can have one state: 'Start' or 'Stop'
     */
    JButton leftRunningControl, rightRunningControl;
    JButton threadsRunningControl;

    /**
     * Form reset button
     * Resets form to initial values except priority settings.
     */
    JButton threadsResetControl;


    /* UI Action model */

    /**
     * Jobs listener and UI Updater
     * This thread listens jobs control variable and updates slider on the frame.
     */
    class SliderControlListener extends Thread {
        AtomicInteger control;

        public SliderControlListener(AtomicInteger sliderControl) {
            super();
            control = sliderControl;
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                // Update slider.
                synchronized (control) {
                    int sliderValue = sliderInitialValue + control.get() / threadsControlValue;
                    slider.setValue(sliderValue);
                }

                // Check button states.
                if ((slider.getValue() <= 10 || slider.getValue() >= 90)
                ||  ((leftThread == null || !leftThread.isAlive()) && (rightThread == null || !rightThread.isAlive()))) {
                    stopJobs();
                    try {
                        if (leftThread != null && leftThread.isAlive()) leftThread.join();
                        if (rightThread != null && rightThread.isAlive()) rightThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    resetUI();
                }

                // Sleep.
                try {
                    Thread.sleep(updateTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
    // Slider control listener object
    SliderControlListener uiUpdater;

    /**
     * Switches GUI between semaphore mode
     * and non-semaphore mode
     */
    private void enableSemaphoreMode(boolean useSemaphore) {
        stopJobs();
        setDefaults();

        threadsRunningControl.setVisible(!useSemaphore);
        leftRunningControl.setVisible(useSemaphore);
        rightRunningControl.setVisible(useSemaphore);
//
//        threadsResetControl.setEnabled(true);
//        leftPriorityControl.setEnabled(true);
//        rightPriorityControl.setEnabled(true);
        resetUI();
    }

    /**
     * Resets defaults value on the frame and slider controls.
     */
    private void setDefaults() {
        sliderValueControl = new AtomicInteger(0);
        slider.setValue(50);
        semaphore = 0;
    }

    private void resetUI() {
        threadsRunningControl.setText("Start");
        threadsRunningControl.setActionCommand("start");

        leftRunningControl.setText("Start");
        leftRunningControl.setActionCommand("start");

        rightRunningControl.setText("Start");
        rightRunningControl.setActionCommand("start");

        threadsResetControl.setEnabled(true);
        leftPriorityControl.setEnabled(true);
        rightPriorityControl.setEnabled(true);
    }

    /**
     * Sets event listener for all buttons on the frame
     * Event listener represented as interface with calling a method from this this class.
     */
    private void setActionListeners() {
        // Mode processing
        modeControl.addActionListener(e -> enableSemaphoreMode(((JCheckBox) e.getSource()).isSelected()));

        // Reset action
        threadsResetControl.addActionListener(e -> setDefaults());


        // Running controls actions
        class RunningControlActionListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                JButton source = (JButton) e.getSource();

                if (command.equals("start")) {
                    // Getting threads running mask
                    int mask;
                    if (source == threadsRunningControl) {
                        mask = 0x11;
                    } else if (source == leftRunningControl) {
                        mask = 0x10;
                    } else {// if (source == rightRunningControl) {
                        mask = 0x01;
                    }

                    boolean startSuccess = startJobs(mask);
                    if (startSuccess) {
                        source.setText("Stop");
                        source.setActionCommand("stop");

                        threadsResetControl.setEnabled(false);
                        leftPriorityControl.setEnabled(false);
                        rightPriorityControl.setEnabled(false);
                    }
                    else {
                        JOptionPane.showMessageDialog(frame,
                                "Semaphore is busy",
                                "Starting error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    stopJobs();
                    resetUI();
                }
            }
        }

        threadsRunningControl.setActionCommand("start");
        threadsRunningControl.addActionListener(new RunningControlActionListener());

        leftRunningControl.setActionCommand("start");
        leftRunningControl.addActionListener(new RunningControlActionListener());

        rightRunningControl.setActionCommand("start");
        rightRunningControl.addActionListener(new RunningControlActionListener());
    }

    /**
     * Adds full stack of needed components for this application to specified pane.
     *
     * This is relative method to createAndShowGUI()
     *
     * @param pane a container with type GridBagLayout
     * @see   this.createAndShowGUI()
     */
    private void addComponentsToPane(Container pane) {
        // Designing constants
        final int hgapDefault = 10;
        final int vgapDefault = 6;

        // Creating slider.
        slider = new JSlider(sliderMinValue, sliderMaxValue);
        Hashtable sliderLabels = slider.createStandardLabels(10);
        sliderLabels.remove(0);
        sliderLabels.remove(100);
        slider.setLabelTable(sliderLabels);
        slider.setPaintLabels(true);
        pane.add(slider, BorderLayout.CENTER);

        // Adding checkbox control for semaphore.
        modeControl = new JCheckBox("Use semaphore", false);
        pane.add(modeControl, BorderLayout.PAGE_START);


        // Adding controls
        // ---------------

        // Controls settings.
        JPanel controlsPane = new JPanel(new GridBagLayout());
        pane.add(controlsPane, BorderLayout.PAGE_END);
        GridBagConstraints controlsGBC = new GridBagConstraints();

        // Thread priority controls settings.
        JPanel spinnerPane;
        JLabel spinnerLabel;
        controlsGBC.fill = GridBagConstraints.HORIZONTAL;

        // Left thread priority spinner.
        spinnerPane = new JPanel(new FlowLayout(FlowLayout.LEADING, hgapDefault, vgapDefault));
        spinnerPane.setToolTipText("Priority for first thread (0 to disable)");
            spinnerLabel = new JLabel("Priority");
            spinnerPane.add(spinnerLabel);
            leftPriorityControl = new JSpinner(new SpinnerNumberModel(5, 0, 10, 1));
            spinnerPane.add(leftPriorityControl);
        controlsGBC.gridx = 0;
        controlsPane.add(spinnerPane, controlsGBC);

        // Right thread priority spinner.
        spinnerPane = new JPanel(new FlowLayout(FlowLayout.TRAILING, hgapDefault, vgapDefault));
        spinnerPane.setToolTipText("Priority for second thread (0 to disable)");
            spinnerLabel = new JLabel("Priority");
            spinnerPane.add(spinnerLabel);
            rightPriorityControl = new JSpinner(new SpinnerNumberModel(5, 0, 10, 1));
            spinnerPane.add(rightPriorityControl);
        controlsGBC.gridx = 2;
        controlsPane.add(spinnerPane, controlsGBC);

        // Creating main control buttons.
        controlsGBC.gridy = 1;
        controlsGBC.insets = new Insets(vgapDefault, hgapDefault, vgapDefault, hgapDefault);

        // Left thread control button.
        leftRunningControl = new JButton("Start");
        controlsGBC.gridx = 0;
        controlsPane.add(leftRunningControl, controlsGBC);

        // Right thread control button.
        rightRunningControl = new JButton("Start");
        controlsGBC.gridx = 2;
        controlsPane.add(rightRunningControl, controlsGBC);
        
        // Add both threads control.
        threadsRunningControl = new JButton("Start");
        controlsGBC.insets = new Insets(vgapDefault, 60, vgapDefault, 60);
        controlsGBC.weightx = 1.0;
        controlsGBC.gridx = 1;
        controlsGBC.gridy = 0;
        controlsPane.add(threadsRunningControl, controlsGBC);

        // Add reset button control.
        threadsResetControl = new JButton("Reset");
        controlsGBC.gridy = 1;
        controlsPane.add(threadsResetControl, controlsGBC);
    }

    /**
     * Creates and show GUI for application.
     * Calls {@see this.addComponentsToPane()} for creating interface
     * and {@see this.setActionListeners()} for adding interaction.
     * Also resets frame for default mode using modeControl action listener.
     */
    private void createAndShowGUI() {
        // Create and set up the window.
        frame = new JFrame("Java Threads Sample");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(600, 200));
        frame.setSize(600, 300);

        // Add components to frame.
        addComponentsToPane(frame.getContentPane());

        // Add action processing
        setActionListeners();

        // Display the window.
        frame.setVisible(true);
    }

    /**
     * Application starter
     *
     * Schedule a job for the event-dispatching thread:
     * creating and showing this application's GUI.
     */
    public void start() {
        // Running GUI
        javax.swing.SwingUtilities.invokeLater(() -> {
            createAndShowGUI();

            // Setting initial values
            setDefaults();

            // Using current semaphore mode
            enableSemaphoreMode(modeControl.isSelected());
        });
    }

    public static void main(String[] args) {
        // Creating and starting application.
        ThreadsSampleManager threadsApp = new ThreadsSampleManager();
        threadsApp.start();
    }
}
