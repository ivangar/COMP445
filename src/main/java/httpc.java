import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;

public class httpc {

    private boolean has_headers = false;
    private boolean has_file_data = false;
    private boolean has_inline_data = false;
    private String host = "";
    private int portNumber;

    public static void main(String[] args) {

        //Test this commands from the console:
        //  get -v -h Content-Type:application/json -h User-Agent:Ivan -H Accept-Language:en-US http://httpbin.org/get?course=networking&assignment=1
        if (args.length > 0)
            new httpc().initApp(args);

    }

    private void initApp(String[] args){
        this.has_headers = Arrays.asList(args).contains("-h");

        CmdValidation cmd_validation = new CmdValidation(args);
        has_inline_data = cmd_validation.has_inline_data;
        has_file_data = cmd_validation.has_file_data;

        String first_arg = args[0];  //For now it's the first arg, will need to change when we use help arg

        // Validate the command
        cmd_validation.wrong_cmd(args);

        if(first_arg.equalsIgnoreCase("get") || first_arg.equalsIgnoreCase("post")) {
            httpRequest request = new httpRequest(args);
        }

        else if(first_arg.equalsIgnoreCase("help")){
            // httpc help
            if(args.length == 1){
                cmd_validation.help_msg();
            }
            // httpc help get
            else if(args[1].equalsIgnoreCase("get")) {
                cmd_validation.help_get();
            }
            // httpc help post
            else if(args[1].equalsIgnoreCase("post")){
                cmd_validation.help_post();
            }
            System.exit(0);
        }


    }

    private void get_request(String[] args) throws IOException {
        httpResponse print_response = new httpResponse(args);

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
                print_response.printRequestHeaders(writer);
            else {
                writer.println("User-Agent:COMP445");
                writer.println("Accept-Language:en-US");
            }

            writer.println();

            //Print response from Server
            print_response.printHttpResponse(reader);

            socket.close();


        }catch(Exception e){
            e.printStackTrace();
        }


    }

    private void post_request(String[] args) throws IOException{
        httpResponse print_response = new httpResponse(args);

        try{
            URL request_url = new URL(args[args.length-1]);  //I checked in curl documentation, the url is always the last argument
            this.host = request_url.getHost();
            this.portNumber = (request_url.getPort() == -1) ? request_url.getDefaultPort() : request_url.getPort(); //If the port number is not specified it returns -1
            Socket socket = new Socket(this.host, this.portNumber);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request_URI = request_url.getFile();  //gets the path + query if there is one
            String data = getData(args);
            writer.println("POST " + request_URI + " HTTP/1.1");
            writer.println("Host: " + this.host);
            writer.println("Content-Length: " + data.length());
            writer.println("Connection: close");  //important to close the connection with server after receiving the response

            //Check for all the headers passed from console
            if(this.has_headers)
                print_response.printRequestHeaders(writer);
            else {
                writer.println("User-Agent:COMP445");
                writer.println("Accept-Language:en-US");
            }

            writer.println();

            //print data from the command
            if(this.has_inline_data || this.has_file_data){
                writer.println(data);
                writer.println();
            }

            //Print response from Server
            print_response.printHttpResponse(reader);

            socket.close();


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getData(String[] args){

        boolean data_line = false;
        String data = "";

        // Only one data
        for (String arg : args) {
            if(arg.equalsIgnoreCase("-d") || arg.equalsIgnoreCase("-f")){
                data_line = true;
                continue;
            }
            if(data_line){
                data = arg;
                break;
            }
        }

        // If data is the path of a file, then read the file.
        if(this.has_file_data){
            BufferedReader readFile;
            try{
                readFile = new BufferedReader(new FileReader(data));
                String line = readFile.readLine();
                if(line == null){
                    data ="";
                }else{
                    data = line;
                }

                while(line != null){
                    line = readFile.readLine();
                    if(line != null){
                        data = data + "\n" + line;
                    }
                }
                readFile.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return data;
    }



}
