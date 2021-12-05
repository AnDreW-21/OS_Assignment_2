import java.util.Scanner;

class Router extends Thread{
        
    }

    class Semaphore{
        private int signals = 0;
        private int bound = 0;

        Semaphore(){

        }
        public Semaphore(int bound){
         this.bound=bound;
        }

        public synchronized void use() throws InterruptedException {
            while (this.signals==bound) wait();
            this.signals++;
            this.notify();
        }

        public synchronized void release() throws InterruptedException {
            while (this.signals==0) wait();
            this.signals--;
            this.notify();
        }

        public synchronized int value() {
            return signals;
        }
    }

     class Device extends Thread{

    }

    public class Network{
        public static void main(String []args){
            Scanner inp = new Scanner(System.in);
            System.out.println("What is the number of Wi-Fi connections\n");
            int connNo = inp.nextInt();
            System.out.println("What is the number of Devices connecting\n");
            int devNo = inp.nextInt();

            Semaphore sem = new Semaphore(connNo);

        }

    }



