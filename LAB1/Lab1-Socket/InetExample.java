import java.net.*;

/**
 * InetExample demonstrates basic InetAddress functionality
 * including hostname resolution and IP address manipulation
 */
public class InetExample {
    public static void main(String[] args) {
        try {
            System.out.println("=== InetAddress Examples ===\n");
            
            // Get hostname by textual representation of IP address
            System.out.println("1. Getting hostname by IP address:");
            InetAddress addr = InetAddress.getByName("127.0.0.1");
            System.out.println("IP Address: " + addr.getHostAddress());
            System.out.println("Host Name: " + addr.getHostName());
            System.out.println("Canonical Host Name: " + addr.getCanonicalHostName());
            System.out.println();
            
            // Get hostname by a byte array containing the IP address
            System.out.println("2. Getting hostname by byte array:");
            byte[] ipAddr = new byte[]{127, 0, 0, 1};
            addr = InetAddress.getByAddress(ipAddr);
            System.out.println("IP Address: " + addr.getHostAddress());
            System.out.println("Host Name: " + addr.getHostName());
            System.out.println("Canonical Host Name: " + addr.getCanonicalHostName());
            System.out.println();
            
            // Additional examples with real domain names
            System.out.println("3. Getting IP address by domain name:");
            addr = InetAddress.getByName("www.google.com");
            System.out.println("Domain: www.google.com");
            System.out.println("IP Address: " + addr.getHostAddress());
            System.out.println("Host Name: " + addr.getHostName());
            System.out.println("Canonical Host Name: " + addr.getCanonicalHostName());
            System.out.println();
            
            // Get local host information
            System.out.println("4. Local host information:");
            addr = InetAddress.getLocalHost();
            System.out.println("Local Host Name: " + addr.getHostName());
            System.out.println("Local IP Address: " + addr.getHostAddress());
            System.out.println("Local Canonical Host Name: " + addr.getCanonicalHostName());
            
        } catch (UnknownHostException e) {
            System.err.println("Unknown host exception: " + e.getMessage());
        }
    }
}