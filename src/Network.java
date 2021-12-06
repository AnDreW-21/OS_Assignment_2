import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

class Router {
    private final ArrayList<Device> devicesLoggedIn;
    private final Semaphore semaphore;
    private String out;


    Router() {
        devicesLoggedIn = null;
        semaphore = new Semaphore(0);
    }

    Router(Semaphore sem) {
        this.semaphore = sem;
        this.devicesLoggedIn = new ArrayList<Device>(sem.getBound());
    }

    public boolean LogIn(Device dev) throws InterruptedException, IOException {
        semaphore.use(dev.getDeviceName());
        devicesLoggedIn.add(dev);
        out = Thread.currentThread().getName() + dev + "Logged In";
        System.out.println(out);
        new logs(out);


        return true;
    }

    public void LogOut(Device dev) throws InterruptedException, IOException {
        devicesLoggedIn.removeIf(d -> d == dev);
        semaphore.release(dev.getDeviceName());
    }

    public void performActivity(Device dev) throws IOException {
        for (Device d : devicesLoggedIn) {
            if (d == dev) {
                out = Thread.currentThread().getName() + dev + "performing online activity";
                System.out.println(out);
                new logs(out);
            } else {
                out = Thread.currentThread().getName() + dev + "not logged in yet";
                System.out.println(out);
                new logs(out);
            }
        }
    }
}

class Semaphore {
    private int bound;
    String out;

    public Semaphore(int bound) {
        this.bound = bound;
    }

    public synchronized void use(String devName) throws InterruptedException, IOException {
        if (bound > 0) {
            bound--;
            out = Thread.currentThread().getName() + " Name: " + devName + " Occupied";
            System.out.println(out);
            new logs(out);
        } else {
            out = Thread.currentThread().getName() + " Name: " + devName + " arrived and waiting";
            System.out.println(out);
            new logs(out);
            wait();
        }
    }

    public synchronized void release(String devName) throws IOException {
        this.bound++;
        if (bound > 0)
            this.notify();
        out = Thread.currentThread().getName() + " Name: " + devName + " Logged Out";
        System.out.println(out);
        new logs(out);
    }

    public int getBound() {
        return bound;
    }

    public void setBound(int bound) {
        this.bound = bound;
    }


}

class Device extends Thread {
    private String deviceName, type;
    private final Router router;
    private final Semaphore semaphore;
    private Thread thread;

    Device() {
        deviceName = "";
        type = "";
        semaphore = new Semaphore(0);
        router = new Router(semaphore);
    }

    Device(String deviceName, String type, Semaphore sem) {

        this.deviceName = deviceName;
        this.type = type;
        this.router = new Router(sem);
        semaphore = sem;
        thread = new Thread(this);
    }

    public void setDeviceName(String name) {
        this.deviceName = deviceName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getType() {
        return type;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public void run() {
        try {
            System.out.println("Name: " + deviceName + " type: " + type + " arrived");
            new logs("Name: " + deviceName + " type: " + type + " arrived");
            router.LogIn(this);
            router.performActivity(this);
            router.LogOut(this);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return " Name: " + deviceName + " type: " + type + " ";
    }
}

public class Network {
    public static int N, TC;
    public static ArrayList<Device> TC_lines = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new logs();
        new GUI();
        Scanner inp = new Scanner(System.in);
        System.out.println("What is the number of Wi-Fi connections");
        N = inp.nextInt();
        System.out.println("What is the number of Devices connecting");
        TC = inp.nextInt();
        inp.nextLine();
        Semaphore sem = new Semaphore(N);
        for (int i = 0; i < TC; i++) {
            String dev;
            dev = inp.nextLine();
            String[] devData = dev.split(" ", 2);
            if (devData.length != 2) {
                System.out.println("Wrong input terminating!");
                System.exit(0);
            }
            Device device = new Device(devData[0], devData[1], sem);

            TC_lines.add(device);
        }
        for (Device device : TC_lines) {
            device.start();

        }
    }

}


class logs {
    File file = new File("logs.txt");

    logs(String log) throws IOException {
        FileWriter logFile = new FileWriter(file, true);
        logFile.write(log + "\n");
        logFile.flush();
        logFile.close();
    }

    logs() throws IOException {
        FileWriter logFile = new FileWriter(file);
    }

}

class GUI extends JFrame {
    GUI() {
        JLabel lab1 = new JLabel("What is the number of Wi-Fi connections?");
        lab1.setBounds(10, 15, 600, 20);
        lab1.setFont(new Font("Verdana", Font.PLAIN, 18));
        add(lab1);

        JLabel lab2 = new JLabel("What is the number of Devices connecting?");
        lab2.setBounds(500, 15, 600, 20);
        lab2.setFont(new Font("Verdana", Font.PLAIN, 18));
        add(lab2);

        JLabel lab3 = new JLabel("list of Devices' name");
        lab3.setBounds(10, 140, 384, 20);
        lab3.setFont(new Font("Verdana", Font.PLAIN, 18));
        add(lab3);

        JLabel lab4 = new JLabel("list of devices' type");
        lab4.setBounds(500, 140, 348, 20);
        lab4.setFont(new Font("Verdana", Font.PLAIN, 18));
        add(lab4);

        JTextField textField1 = new JTextField();
        textField1.setBounds(10, 50, 300, 26);
        add(textField1);
        textField1.setVisible(true);
        textField1.requestFocus();

        JTextField textField2 = new JTextField();
        textField2.setBounds(500, 50, 300, 26);
        add(textField2);
        textField2.requestFocus();

        JTextArea devs = new JTextArea();
        devs.setBounds(10, 180, 300, 120);
        add(devs);
        devs.requestFocus();


        JTextArea types = new JTextArea();
        types.setBounds(500, 180, 300, 120);
        add(types);
        types.requestFocus();

        JButton b = new JButton(" Start");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Network.N = Integer.parseInt(textField1.getText());
                Network.TC = Integer.parseInt(textField2.getText());
                String[] names = devs.getText().split("\n");
                String[] type = types.getText().split("\n");
                Semaphore sem = new Semaphore(Network.N);

                for (int i = 0; i < names.length; i++) {
                    Network.TC_lines.add(new Device(names[i], type[i], sem));
                }
                for (Device device : Network.TC_lines) {
                    device.start();
                }
            }
        });
        b.setBounds(400, 320, 150, 30);
        b.setVisible(true);
        add(b);
        b.setBackground(Color.white);
        b.requestFocus();
        setBounds(100, 100, 1050, 450);
        setLayout(null);
        setTitle("Router App");
        setVisible(true);

    }

}





