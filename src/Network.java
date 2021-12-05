import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class Router {
    ArrayList<Device> devicesLoggedIn;
    Semaphore semaphore;

    Router() {
        devicesLoggedIn = null;
        semaphore = null;
    }

    Router(Semaphore sem) {
        this.semaphore = sem;
        this.devicesLoggedIn = new ArrayList<Device>(sem.getBound());
    }

    public boolean LogIn(Device dev) throws InterruptedException {
        System.out.println(dev + "arrived");
        semaphore.use();
        System.out.println(Thread.currentThread().getName() + dev + "Occupied");
        devicesLoggedIn.add(dev);
        System.out.println(Thread.currentThread().getName() + dev + "Logged In");
        return true;
    }

    public boolean LogOut(Device dev) throws InterruptedException {
        devicesLoggedIn.removeIf(d -> d == dev);
        semaphore.release();
        System.out.println(Thread.currentThread().getName() + dev + "Logged Out");
        return false;
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
    private int signals;
    private int bound;

    Semaphore() {
        signals = 0;
        bound = 0;
    }

    public Semaphore(int bound) {
        this.bound = bound;
    }

    public synchronized void use() throws InterruptedException {
        if (this.signals == bound) wait();
        this.signals++;

    }

    public synchronized void release() throws InterruptedException {
        if (this.signals == 0) wait();
        this.signals--;
        this.notify();
    }

    public synchronized int getBound() {
        return bound;
    }

    public synchronized int value() {
        return signals;
    }
}

class Device extends Thread {
    String deviceName, type;
    Router router;
    Semaphore semaphore;

    Device() {
        deviceName = "";
        type = "";
        semaphore = new Semaphore();
        router = new Router(semaphore);

    }

    Device(String deviceName, String type, Semaphore sem) {
        this.deviceName = deviceName;
        this.type = type;
        this.router = new Router(sem);
        semaphore = sem;
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
    public String toString(){
        return  " Name: "+deviceName +" type: " + type + " ";
    }
}

public class Network {


    public static void main(String[] args) {
        Scanner inp = new Scanner(System.in);
        System.out.println("What is the number of Wi-Fi connections");
        int connNo = inp.nextInt();
        System.out.println("What is the number of Devices connecting");
        int devNo = inp.nextInt();
        inp.nextLine();
        Semaphore sem = new Semaphore(connNo);


        ArrayList<Device> devQue = new ArrayList<>();

        for (int i = 0; i < devNo; i++) {
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
        for (int i = 0; i < devQue.size(); i++) {
            devQue.get(i).start();

        }
    }

}



