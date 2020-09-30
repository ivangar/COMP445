import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class httpc {

    public int url_index;

    public static void main(String[] args) {

        String request_method = "";
        int arguments = args.length;

        if (arguments > 0) {

            request_method = args[0];
            System.out.println(request_method);
            if(request_method.equalsIgnoreCase("get")) {
                try {
                    get_request(args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(request_method.equalsIgnoreCase("post"))
                post_request(args);
        }


    }

    public static void get_request(String[] args) throws IOException {
        boolean is_verbose = Arrays.stream(args).anyMatch("-v"::equals);
        boolean has_headers = Arrays.stream(args).anyMatch("-h"::equals);
        String host;
        int portNumber;

        if (is_verbose) {
            System.out.println("get method is verbose");
        }
        if (has_headers) {
            System.out.println("get method has headers");
        }

        try{
            String url = args[args.length-1];
            URL request_url = new URL(args[args.length-1]);  //I checked in curl documentation, the url is always the last argument
            host = request_url.getHost();
            portNumber = (request_url.getPort() == -1) ? 80 : request_url.getPort(); //If the port number is not specified it returns -1

            System.out.println("Host: " + host);

            Socket socket = new Socket(host, portNumber);
            DataInputStream input_stream = new DataInputStream(socket.getInputStream());
            DataOutputStream output_stream = new DataOutputStream(socket.getOutputStream());

        }catch(MalformedURLException ex){
            System.out.println("URL exception " + ex);
        }


    }

    public static void post_request(String[] args){

    }

    //We don't know which index has the url, so we test every argument in args[] and get the index of the url
    private boolean validateURL(String[] args){

        //Pattern to test if argument passed is a valid URL
        Pattern pattern = Pattern.compile("(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?", Pattern.CASE_INSENSITIVE);

        boolean isURL = false;

        //Loop through the last element, that's the URL
        for(int i = 0; i < args.length; i++){
            String arg = args[i];

            Matcher matcher = pattern.matcher(arg);
            boolean matchFound = matcher.find();
            if(matchFound) {
                System.out.println("get method has a url at index " + i + " url is " + args[i]);
                isURL = true;
                url_index = i;
            }
        }

        return isURL;
    }
}
