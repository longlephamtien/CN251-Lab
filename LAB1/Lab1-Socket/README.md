# Lab 1c — Socket Programming in Java (Chat Application)

Faculty of Computer Science and Engineering — HCMC University of Technology
Course: Computer Networks (Lab) - CO3094

This folder contains source code and assets for Lab 1c (Socket Programming) exercises: downloading a webpage via sockets, building a simple client-server chat application, and experimenting with multithreading in Java.

## Objectives

1. Practice Java socket programming (TCP).
2. Build a simple chat application using the client-server model.
3. Implement multithreaded Java programs (server and client concurrency).

## Exercises & Description

### Exercise 1 — Download a web page using sockets

- Implemented in: `WebDownloader.java`
- Goal: Connect to a web server (for example, `www.google.com`) using a TCP socket, request the homepage (HTTP GET), and save the response body to a local file.

Quick notes:
- The program demonstrates how to build a simple HTTP request, open a socket to the server on port 80 (or 443 for TLS — not covered here), read the response stream, and write the response to disk.
- `InetExample.java` contains examples of using `InetAddress` to resolve hostnames and IPs.

### Exercise 2 — Chat application (client-server)

This folder contains a set of chat application examples demonstrating basic and multithreaded server/client implementations.

Files:
- `ChatServer.java` — Simple single-threaded server that accepts one client connection and exchanges messages.
- `ChatClient.java` — Simple client that connects to `ChatServer` and can send/receive messages.
- `ChatClientGUI.java` — GUI-based client (Swing) for interacting with the chat server.
- `MultithreadChatServer.java` — Server that accepts multiple client connections and handles each client on a separate thread.
- `MultithreadChatClientGUI.java` — GUI client designed to work with the multithreaded server.
- `MultithreadedServerExample.java` — Example showing a server that spawns threads to handle clients.

Design notes:
- Server responsibilities: create a `ServerSocket`, listen on a port, accept incoming connections, and for each connected client spawn a handler thread that performs read/write operations.
- Client responsibilities: create a `Socket`, connect to server IP and port, send and receive messages, and close the socket when done.

### Exercise 3 — Multithreading in Java

- `PrimeRun.java` — Example `Runnable` that computes primes larger than a given start value. Shows how to create threads with `new Thread(new PrimeRun(...)).start()`.
- `MultithreadingDemo.java`, `ThreadInterruptionExample.java` — Demos for thread lifecycle and interruption using `Thread.interrupt()` and a shared `running` flag.

## How to compile

Open a terminal in this folder (`LAB1/Lab1-Socket`) and run:

```bash
# compile all Java files in the folder
javac *.java
```

If you prefer to compile a single file, use `javac` with the specific file name, for example:

```bash
javac WebDownloader.java
javac ChatServer.java ChatClient.java
```

## How to run

Below are example commands. Replace `PORT` and `HOST` where relevant.

1. Web downloader (Exercise 1)

```bash
# after compilation
java WebDownloader
# The program typically prompts for or uses a hard-coded host like www.google.com and writes the response to a file.
```

2. Simple chat (single client)

```bash
# start server (default port used by the sample, e.g. 12345)
java ChatServer
# in another terminal, run client
java ChatClient
```

3. Multithreaded chat (multiple clients)

```bash
# start multithreaded server
java MultithreadChatServer
# open several terminals and start a GUI client or text client
java MultithreadChatClientGUI
# or
java ChatClient
```

4. Prime thread demo

```bash
javac PrimeRun.java
java PrimeRun
# or run MultithreadingDemo and ThreadInterruptionExample to see thread interruption behavior
```

## Notes and assumptions

- These examples use plain TCP sockets. TLS/HTTPS is not implemented in `WebDownloader.java` — requesting `https://` resources will require additional libraries or `SSLSocket`.
- GUI classes use Swing — run them on systems with a graphical environment.
- The sample code may use blocking I/O. For production use consider proper thread pools and non-blocking I/O (NIO).

## Suggested experiments

- Extend `MultithreadChatServer` to broadcast messages from one client to all connected clients.
- Add authentication and simple commands (e.g., /nick, /quit).
- Convert `WebDownloader` to use `SSLSocketFactory` and download HTTPS pages.

## Files in this folder

- ChatClient.java — small console client
- ChatClientGUI.java — Swing-based client UI
- ChatServer.java — simple single-client server
- MultithreadChatServer.java — server that handles each client in a separate thread
- MultithreadChatClientGUI.java — GUI client for multithreaded server
- MultithreadedServerExample.java — example multithread server
- MultithreadingDemo.java — demo for multithreading
- PrimeRun.java — prime computation Runnable example
- ThreadInterruptionExample.java — demonstrates interrupting threads
- WebDownloader.java — HTTP GET via raw sockets
- www_google_com_homepage.html — example saved HTML