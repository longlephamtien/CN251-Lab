import java.io.*;
import java.net.*;

public class WebDownloader {
    private static final int HTTP_PORT = 80;
    
    public static void main(String[] args) {
        String website = "www.google.com";
        
        if (args.length > 0) {
            website = args[0];
        }
        
        WebDownloader downloader = new WebDownloader();
        downloader.downloadHomepage(website);
    }
    
    /**
     * Downloads the homepage of the specified website
     * @param website the website to download from (e.g., "www.google.com")
     */
    public void downloadHomepage(String website) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        OutputStreamWriter fileWriter = null;
        
        try {
            System.out.println("Connecting to " + website + "...");
            
            InetAddress address = InetAddress.getByName(website);
            System.out.println("IP Address: " + address.getHostAddress());
            
            socket = new Socket(address, HTTP_PORT);
            System.out.println("Connected to " + website);

            out = new PrintWriter(socket.getOutputStream(), true);
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO-8859-1"));
            
            System.out.println("Sending HTTP GET request...");
            out.println("GET / HTTP/1.1");
            out.println("Host: " + website);
            out.println("Connection: close");
            out.println();
            
            String filename = website.replace(".", "_") + "_homepage.html";
            fileWriter = new OutputStreamWriter(new FileOutputStream(filename), "UTF-8");

            System.out.println("Receiving response and saving to " + filename + "...");
            String line;
            boolean isHeader = true;
            boolean isChunked = false;
            String charset = "UTF-8";
            
            while ((line = in.readLine()) != null) {
                
                if (isHeader) {
                    System.out.println("Header: " + line);
                    
                    if (line.toLowerCase().contains("transfer-encoding") && 
                        line.toLowerCase().contains("chunked")) {
                        isChunked = true;
                        System.out.println("Detected chunked encoding");
                    }
                    
                    if (line.toLowerCase().startsWith("content-type:")) {
                        if (line.toLowerCase().contains("charset=")) {
                            String[] parts = line.split("charset=");
                            if (parts.length > 1) {
                                charset = parts[1].trim().replace(";", "");
                                System.out.println("Detected charset: " + charset);
                            }
                        }
                    }
                    
                    if (line.trim().isEmpty()) {
                        isHeader = false;
                        System.out.println("\n--- HTML Content (charset: " + charset + ") ---");
                        continue;
                    }
                    continue;
                }
                
                if (isChunked) {
                    if (line.matches("^[0-9a-fA-F]+$")) {
                        System.out.println("Skipping chunk size: " + line);
                        continue;
                    }
                    if (line.equals("0")) {
                        System.out.println("End of chunked data");
                        break;
                    }
                }
                
                fileWriter.write(line + "\n");
                
            }
            
            System.out.println("\nDownload completed successfully!");
            System.out.println("Homepage saved as: " + filename);
            System.out.println("HTTP headers were displayed but not saved to file");
        } catch (UnknownHostException e) {
            System.err.println("Error: Could not resolve hostname " + website);
            System.err.println("Details: " + e.getMessage());
        } catch (ConnectException e) {
            System.err.println("Error: Could not connect to " + website);
            System.err.println("Details: " + e.getMessage());
            System.err.println("Note: Some websites may block direct socket connections or require HTTPS");
        } catch (IOException e) {
            System.err.println("Error during download: " + e.getMessage());
        } finally {
            // Clean up resources
            try {
                if (fileWriter != null) fileWriter.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}