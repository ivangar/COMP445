import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class httpc {

    public static void main(String[] args) {


        String request_method = args[ 0 ];
        String param1 = args[ 1 ];
        String param2 = args[ 2 ];

        try{
            URL url = new URL(param2);
            System.out.println("query = " + url.getQuery());
            //more code goes here
        }catch(MalformedURLException ex){
            //do exception handling here
        }

        System.out.println( "Args length: " + args.length );
        System.out.println( "request_method: " + request_method );
        System.out.println( "param1: " + param1 );

    }

}
