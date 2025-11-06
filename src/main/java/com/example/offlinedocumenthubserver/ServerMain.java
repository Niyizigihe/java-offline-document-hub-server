package com.example.offlinedocumenthubserver;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class ServerMain {
    public static void main(String[] args) {
        printHeader();
        displayNetworkInfo();
        createSharedFolder();
        startServer();
    }

    private static void printHeader() {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   🚀 OFFLINE DOCUMENT HUB SERVER            ║");
        System.out.println("║                   Starting on port 8080...                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    private static void displayNetworkInfo() {
        System.out.println("🌐 NETWORK CONFIGURATION");
        System.out.println("──────────────────────────────────────────────────────────");

        try {
            // Localhost
            String localhost = InetAddress.getLocalHost().getHostAddress();
            System.out.println("📍 Localhost Access:");
            System.out.println("   └─ http://localhost:8080");
            System.out.println("   └─ http://" + localhost + ":8080");
            System.out.println();

            // Network interfaces
            System.out.println("📡 Network Access (for other computers):");
            boolean foundExternalIP = false;

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (!address.isLoopbackAddress() && address.getHostAddress().contains(".")) {
                        String interfaceName = networkInterface.getDisplayName();
                        String ipAddress = address.getHostAddress();

                        // Check if it's a private IP (common LAN ranges)
                        boolean isPrivate = ipAddress.startsWith("192.168.") ||
                                ipAddress.startsWith("10.") ||
                                ipAddress.startsWith("172.");

                        if (isPrivate) {
                            System.out.println("   ├─ " + interfaceName);
                            System.out.println("   │  └─ IP: " + ipAddress);
                            System.out.println("   │  └─ URL: http://" + ipAddress + ":8080");
                            foundExternalIP = true;
                        }
                    }
                }
            }

            if (!foundExternalIP) {
                System.out.println("   └─ No network interfaces found for external access");
                System.out.println("      Make sure your computer is connected to the network");
            }

        } catch (Exception e) {
            System.out.println("   └─ ❌ Error reading network configuration: " + e.getMessage());
        }

        System.out.println();
        System.out.println("💡 INSTRUCTIONS FOR CLIENT CONNECTION:");
        System.out.println("   1. On client computers, run the client application");
        System.out.println("   2. When asked for server IP, use one of the NETWORK IPs above");
        System.out.println("   3. Do NOT use 'localhost' for network clients");
        System.out.println("──────────────────────────────────────────────────────────");
        System.out.println();
    }

    private static void createSharedFolder() {
        java.io.File sharedFolder = new java.io.File("shared_documents");
        if (!sharedFolder.exists()) {
            sharedFolder.mkdirs();
            System.out.println("📁 Created shared documents folder: " + sharedFolder.getAbsolutePath());
        }
    }

    private static void startServer() {
        try {
            RESTServer.startRESTServer();

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println();
                System.out.println("🛑 Server is shutting down...");
                RESTServer.stopServer();
            }));

        } catch (Exception e) {
            System.err.println("❌ Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}