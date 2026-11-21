import java.net.*;
import java.io.*;
import java.util.Scanner;

public class UDPMestre{
    public static void main(String [] args){

        DatagramSocket asocket = null;
        DatagramSocket bsocket = null;

        try{

            InetAddress ip = null;

            if (args.length > 3 ){
                ip = InetAddress.getByName(args[3]); 
            }else{
                ip = InetAddress.getByName("127.0.0.1");
            }
            int myport = Integer.parseInt(args[0]) + 5000;
            int sendPort = myport + 1;
            if (sendPort == 5004){
                sendPort = 5000;
            }
            int operation = Integer.parseInt(args[1]);
            int turns =  Integer.parseInt(args[2]);
            int token = 2;

            asocket = new DatagramSocket();
            bsocket = new DatagramSocket(myport);

            for(int i = 0; i < turns ; i++){
                // envio de mensagem 
                byte[] msg = String.valueOf(token).getBytes();
                DatagramPacket request = new DatagramPacket (msg,msg.length,ip,sendPort);
                System.out.println("Enviando token, valor atual: " + token);
                asocket.send(request);

                // recebimento de mensagem
                byte[] buffer = new byte[1000];
                DatagramPacket request2 = new DatagramPacket(buffer,buffer.length);
                System.out.println("esperando mensagem");
                bsocket.receive(request2);
                String data = new String(request2.getData()).trim();
                token = Integer.parseInt(data);
                System.out.println("token recebido, valor atual: " + token);
                if (operation == 1){
                    token *= 2;
                }
                else{
                    token ++;
                }
            }
            System.out.println("Finalizado " +turns+" voltas, valor final do token: "+token);

        }catch(SocketException e){
            System.out.println("socket: " + e.getMessage());
        }catch(IOException e){
            System.out.println("io:" + e.getMessage());
        }finally{
            if(asocket != null){
                asocket.close();
            }
            if(bsocket != null){
                bsocket.close();
            }
        }
    }
}