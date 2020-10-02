import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;

public class httpc {

    public boolean is_verbose = false;
    public boolean has_headers = false;
    public boolean has_file_data = false;
    public boolean has_inline_data = false;
    public String host = "";
    public int portNumber;
    public int responseStatusCode;
    public String responseStatus;
    Hashtable<String, String> post_data = new Hashtable<String, String>(); //will be used for post inline data key:value pairs

    public static void main(String[] args) {

        //Test this commands from the console:
        //  get -v -h Content-Type:application/json -h User-Agent:Ivan -H Accept-Language:en-US http://httpbin.org/get?course=networking&assignment=1
        if (args.length > 0)
            new httpc().initApp(args);

    }

    public void initApp(String[] args){
        this.is_verbose = Arrays.asList(args).contains("-v");
        this.has_headers = Arrays.asList(args).contains("-h");
        this.has_inline_data = Arrays.asList(args).contains("-d");
        this.has_file_data = Arrays.asList(args).contains("-f");

        String first_arg = args[0];  //For now it's the first arg, will need to change when we use help arg

        if(first_arg.equalsIgnoreCase("get")) {
            try {
                get_request(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(first_arg.equalsIgnoreCase("post"))
            post_request(args);

        else if(first_arg.equalsIgnoreCase("help"))
            System.out.println("Print help options to console");


    }
    
    public void get_request(String[] args) throws IOException {

        try{
            URL request_url = new URL(args[args.length-1]);  //I checked in curl documentation, the url is always the last argument
            this.host = request_url.getHost();
            this.portNumber = (request_url.getPort() == -1) ? request_url.getDefaultPort() : request_url.getPort(); //If the port number is not specified it returns -1
            Socket socket = new Socket(this.host, this.portNumber);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request_URI = request_url.getFile();  //gets the path + query if there is one
            writer.println("GET " + request_URI + " HTTP/1.1");
            writer.println("Host: " + this.host);
            writer.println("Connection: close");  //important to close the connection with server after receiving the response

            //Check for all the headers passed from console
            if(this.has_headers)
                printRequestHeaders(args, writer);

            writer.println();

            //Print response from Server
            printHttpResponse(reader);

            socket.close();


        }catch(Exception e){
            e.printStackTrace();
        }


    }

    private void printRequestHeaders(String[] args, PrintWriter writer){

        boolean header_line = false;

        //You can pass multiple headers
        for (String arg : args) {
            if(arg.equalsIgnoreCase("-h")){
                header_line = true;
                continue;
            }

            if(header_line){
                writer.println(arg);
                header_line = false;
            }
        }

    }

    private void printHttpResponse(BufferedReader reader) throws IOException{

        String responseLine; //response from server
        boolean response_content = false;  //body from response

        System.out.println("\n---------------------- Http Server Response ----------------------------\n");

        try{
            while ((responseLine = reader.readLine()) != null) {

                if(responseLine.startsWith("HTTP")){
                    this.responseStatusCode = Integer.parseInt(responseLine.split("\\s+")[1]);
                    this.responseStatus = responseLine.substring(responseLine.indexOf(Integer.toString(this.responseStatusCode))+4);
                    checkResponseError();
                }

                if(responseLine.isEmpty())  //empty line separates the content from the headers
                    response_content = true;

                if(!this.is_verbose && response_content)
                    System.out.println(responseLine);
                else if(this.is_verbose)
                    System.out.println(responseLine);

            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void checkResponseError(){
        if(this.responseStatusCode >= 400 && this.responseStatusCode < 500)
            System.out.println("\nClient side error\nStatus Code: " + this.responseStatusCode + "\nStatus: " + this.responseStatus + "\n");
        else if(this.responseStatusCode > 500)
            System.out.println("\nServer side error\nStatus Code: " + this.responseStatusCode + "\nStatus: " + this.responseStatus + "\n");
    }

    public static void post_request(String[] args){

    }

}
