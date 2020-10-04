import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class httpc {

    private boolean is_verbose = false;
    private boolean has_headers = false;
    private boolean has_file_data = false;
    private boolean has_inline_data = false;
    private String host = "";
    private int portNumber;
    private int responseStatusCode;
    private String responseStatus;
    private int count_v = 0;
    private int count_d = 0;
    private int count_f = 0;
    private List<String> requestHeaders = new ArrayList<String>();

    public static void main(String[] args) {

        //Test this commands from the console:
        //  get -v -h Content-Type:application/json -h User-Agent:Ivan -H Accept-Language:en-US http://httpbin.org/get?course=networking&assignment=1
        if (args.length > 0)
            new httpc().initApp(args);

    }

    private void initApp(String[] args){
        this.is_verbose = Arrays.asList(args).contains("-v");
        this.has_headers = Arrays.asList(args).contains("-h");
        this.has_inline_data = Arrays.asList(args).contains("-d");
        this.has_file_data = Arrays.asList(args).contains("-f");
        this.count_v = count(args, "-v");
        this.count_d = count(args, "-d");
        this.count_f = count(args, "-f");

        String first_arg = args[0];  //For now it's the first arg, will need to change when we use help arg

        // Validate the command
        wrong_cmd(args);

        if(first_arg.equalsIgnoreCase("get")) {
            try {
                get_request(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(first_arg.equalsIgnoreCase("post")){
            try {
                post_request(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if(first_arg.equalsIgnoreCase("help")){
            // httpc help
            if(args.length == 1){
                help_msg();
            }
            // httpc help get
            else if(args[1].equalsIgnoreCase("get")) {
                help_get();
            }
            // httpc help post
            else if(args[1].equalsIgnoreCase("post")){
                help_post();
            }
            System.exit(0);
        }


    }

    // Need to include :
    // When there is more than one -v/-d/-f ALREADY DONE
    // Check -h has key and value pair ALREADY DONE
    // Check consecutive -h -h /.... ALREADY DONE
    // When there is no url
    private void wrong_cmd(String[] args){

        // print httpc help
        if(!(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("post"))){
            help_msg();
            System.exit(0);
        }
        // print httpc help get
        if(args[0].equalsIgnoreCase("get")){
            if((has_file_data || has_inline_data) || count_v > 1){
                help_get();
                System.exit(0);
            }
        }
        // print httpc help post
        if(args[0].equalsIgnoreCase("post")){
            if((has_file_data && has_inline_data) || count_v > 1 || count_d > 1 || count_f > 1){
                help_post();
                System.exit(0);
            }
        }

        if(!validateHeaders(args))
        {
            System.out.println("\nERROR \nInvalid httpc Request header syntax, please check the documentation\n\n");
            help_msg();
            System.exit(0);
        }

    }

    private void help_msg(){
        System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
        System.out.println("Usage:");
        System.out.println("    httpc command [arguments]");
        System.out.println("The commands are:");
        System.out.println("    get     executes a HTTP GET request and prints the response.");
        System.out.println("    post    executes a HTTP POST request and prints the response.");
        System.out.println("    help    prints this screen.");
        System.out.println("Use \"httpc help [command]\" for more information about a command.");
    }
    private void help_get(){
        System.out.println("usage: httpc get [-v] [-h key:value] URL");
        System.out.println("Get executes a HTTP GET request for a given URL.");
        System.out.println("    -v             Prints the detail of the response such as protocol, status, and headers.");
        System.out.println("    -h key:value   Associates headers to HTTP Request with the format 'key:value'.");
    }
    private void help_post(){
        System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL");
        System.out.println("Post executes a HTTP POST request for a given URL with inline data or from file.");
        System.out.println("    -v             Prints the detail of the response such as protocol, status, and headers.");
        System.out.println("    -h key:value   Associates headers to HTTP Request with the format 'key:value'.");
        System.out.println("    -d string      Associates an inline data to the body HTTP POST request.");
        System.out.println("    -f file        Associates the content of a file to the body HTTP POST request.");
        System.out.println("Either [-d] or [-f] can be used but not both.");
    }

    private void get_request(String[] args) throws IOException {

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
                printRequestHeaders(writer);
            else {
                writer.println("User-Agent:COMP445");
                writer.println("Accept-Language:en-US");
            }

            writer.println();

            //Print response from Server
            printHttpResponse(reader);

            socket.close();


        }catch(Exception e){
            e.printStackTrace();
        }


    }

    private void post_request(String[] args) throws IOException{

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
                printRequestHeaders(writer);
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
            printHttpResponse(reader);

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


    private void printRequestHeaders(PrintWriter writer){

        for (String header : this.requestHeaders) {
            writer.println(header);
        }

    }

    // Count how many how many -v/-h/-d are in the command.
    private int count(String[] args, String command){

        int total = 0;

        for(String arg: args){
            if(arg.equalsIgnoreCase(command)) {
                total++;
            }
        }

        return total;
    }

    private boolean validateHeaders(String[] args){

        boolean header_line = false;

        for (String arg : args) {
            //check it's -h and we don't have two or more -h consecutive
            if(arg.equalsIgnoreCase("-h") && !header_line){
                header_line = true;
                continue;
            }

            if(header_line){
                if(!arg.contains(":"))
                    return false;

                String[] keyvalues = arg.split(":");

                if(keyvalues.length != 2)
                    return false;

                if(keyvalues.length == 2)
                    if(keyvalues[0].isEmpty() || keyvalues[1].isEmpty())
                        return false;

                //adding header to array list
                this.requestHeaders.add(arg);
                header_line = false;
            }
        }

        return true;
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

    private void checkResponseError(){
        if(this.responseStatusCode >= 400 && this.responseStatusCode < 500)
            System.out.println("\nClient side error\nStatus Code: " + this.responseStatusCode + "\nStatus: " + this.responseStatus + "\n");
        else if(this.responseStatusCode > 500)
            System.out.println("\nServer side error\nStatus Code: " + this.responseStatusCode + "\nStatus: " + this.responseStatus + "\n");
    }

}
