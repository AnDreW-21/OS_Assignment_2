import java.util.ArrayList;
import java.util.Scanner;

class Router {
    private final ArrayList<Device> devicesLoggedIn;
    private final Semaphore semaphore;


    Router() {
        devicesLoggedIn = null;
        semaphore = new Semaphore(0);
    }

    Router(Semaphore sem) {
        this.semaphore = sem;
        this.devicesLoggedIn = new ArrayList<Device>(sem.getBound());
    }

    public boolean LogIn(Device dev) throws InterruptedException {
        semaphore.use(dev.getDeviceName());
        devicesLoggedIn.add(dev);
        System.out.println(Thread.currentThread().getName() + dev + "Logged In");
        return true;
    }

    public void LogOut(Device dev) throws InterruptedException {
        devicesLoggedIn.removeIf(d -> d == dev);
        semaphore.release(dev.getDeviceName());
    }

    public void performActivity(Device dev) {
        for (Device d : devicesLoggedIn) {
            if (d == dev) {
                System.out.println(Thread.currentThread().getName() + dev + "performing online activity");
            } else System.out.println(Thread.currentThread().getName() + dev + "not logged in yet");
        }

    }
}

class Semaphore {
    private int bound;


    public Semaphore(int bound) {
        this.bound = bound;
    }

    public synchronized void use(String devName) throws InterruptedException {
        if (bound > 0) {
            bound--;
            System.out.println(devName + " Occupied");
        } else {
            System.out.println(Thread.currentThread().getName() + " arrived and waiting");
            wait();
        }
    }

    public synchronized void release(String devName) throws InterruptedException {
        this.bound++;
        if (bound > 0)
            this.notify();
        System.out.println(Thread.currentThread().getName() +" Name: "+devName + " Logged Out");
    }

    public  int getBound() {
        return bound;
    }
    public void  setBound(int bound) {
        this.bound=bound;
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
            router.LogIn(this);
            router.performActivity(this);
            router.LogOut(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return " Name: " + deviceName + " type: " + type + " ";
    }
}

public class Network {

    public static void main(String[] args) throws InterruptedException {
        Scanner inp = new Scanner(System.in);
        System.out.println("What is the number of Wi-Fi connections");
        int N = inp.nextInt();
        System.out.println("What is the number of Devices connecting");
        int TC = inp.nextInt();
        inp.nextLine();
        Semaphore sem = new Semaphore(N);
        ArrayList<Device> devQue = new ArrayList<>();
        for (int i = 0; i < TC; i++) {
            String dev;
            dev = inp.nextLine();
            String[] devData = dev.split(" ", 2);
            if (devData.length != 2) {
                System.out.println("Wrong input terminating!");
                System.exit(0);
            }
            Device device = new Device(devData[0], devData[1], sem);

            devQue.add(device);
        }
        for (Device device : devQue) {
            device.start();
        }
    }
}



