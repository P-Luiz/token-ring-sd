import java.net.*;
import java.io.*;

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
            int id = Integer.parseInt(args[0]);
            int sendPort = myport + 1;
            if (sendPort == 5004){
                sendPort = 5000;
            }
            int operation = Integer.parseInt(args[1]);
            int turns =  Integer.parseInt(args[2]);
            int token = 2;
            int i = 0;
            String tokenReceived = "-";

            asocket = new DatagramSocket();
            bsocket = new DatagramSocket(myport);

            System.out.println("+-------------------------------------------------------------------------+");
            System.out.println("|                               TOKEN RING                                |");
            System.out.println("+--------------+--------------------+-------------------+-----------------+");
            System.out.printf("| %-12s | %-18s | %-17s | %-15s |%n", "Nó", "Token Recebido", "Token Enviado", "Volta");
            System.out.println("+--------------+--------------------+-------------------+-----------------+");
            System.out.printf("| %-12s | %-18s | %-17d | %-15d |%n", "Mestre " + id, tokenReceived , token, (i + 1));

            for( i = 0; i < turns ; i++){

                // envio de mensagem 
                byte[] msg = String.valueOf(token).getBytes();
                DatagramPacket request = new DatagramPacket (msg,msg.length,ip,sendPort);
                asocket.send(request);

                // recebimento de mensagem
                byte[] buffer = new byte[1000];
                DatagramPacket request2 = new DatagramPacket(buffer,buffer.length);
                bsocket.receive(request2);
                String data = new String(request2.getData()).trim();
                token = Integer.parseInt(data);
                tokenReceived = String.valueOf(token);
                if (operation == 1){
                    token *= 2;
                }
                else{
                    token ++;
                }

                System.out.printf("| %-12s | %-18s | %-17d | %-15d |%n", "Mestre " + id, tokenReceived , token, (i + 1));

            }
            
            System.out.println("+--------------+--------------------+-------------------+-----------------+");
            System.out.println("Finalizado " + turns + " voltas | Último recebido: " + tokenReceived + " | Valor final do token: " + token);

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