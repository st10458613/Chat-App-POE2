package com.mycompany.chatapp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Chat Application main class
 * 
 * @author Thando M
 */
public class ChatApp {
    public static void main(String[] args) {
        Login login = new Login();
        boolean isRunning = true;

        // Welcome message for the chat application
        JOptionPane.showMessageDialog(null, "Welcome to the Chat Application",
                "Welcome", JOptionPane.INFORMATION_MESSAGE);

        // Login/Registration loop
        boolean isAuthenticated = false;
        while (isRunning && !isAuthenticated) {
            String[] options = {"Register", "Login", "Exit"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Please select an option",
                    "Chat Application",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (choice) {
                case 0: // Register
                    String username = JOptionPane.showInputDialog(null,
                            "Enter username (must contain _ and be <=5 characters):",
                            "Registration", JOptionPane.PLAIN_MESSAGE);
                    if (username == null) continue;

                    String password = JOptionPane.showInputDialog(null,
                            "Enter password (min 8 characters, with 1 capital letter, number, special character):",
                            "Registration", JOptionPane.PLAIN_MESSAGE);
                    if (password == null) continue;

                    String cellPhone = JOptionPane.showInputDialog(null,
                            "Enter cell phone number (e.g., ‪+27123456789‬):",
                            "Registration", JOptionPane.PLAIN_MESSAGE);
                    if (cellPhone == null) continue;

                    String firstName = JOptionPane.showInputDialog(null,
                            "Enter first name:",
                            "Registration", JOptionPane.PLAIN_MESSAGE);
                    if (firstName == null) continue;

                    String lastName = JOptionPane.showInputDialog(null,
                            "Enter last name:",
                            "Registration", JOptionPane.PLAIN_MESSAGE);
                    if (lastName == null) continue;

                    String registrationResult = login.registerUser(username, password, cellPhone, firstName, lastName);
                    JOptionPane.showMessageDialog(null, registrationResult,
                            "Registration Result", JOptionPane.INFORMATION_MESSAGE);
                    break;

                case 1: // Login
                    String loginUsername = JOptionPane.showInputDialog(null,
                            "Enter username:",
                            "Login", JOptionPane.PLAIN_MESSAGE);
                    if (loginUsername == null) continue;

                    String loginPassword = JOptionPane.showInputDialog(null,
                            "Enter password:",
                            "Login", JOptionPane.PLAIN_MESSAGE);
                    if (loginPassword == null) continue;

                    String loginResult = login.returnLoginStatus(loginUsername, loginPassword);
                    JOptionPane.showMessageDialog(null, loginResult,
                            "Login Result", JOptionPane.INFORMATION_MESSAGE);

                    if (loginResult.contains("Welcome")) {
                        isAuthenticated = true; // Proceed to messaging
                    }
                    break;

                case 2: // Exit
                    isRunning = false;
                    JOptionPane.showMessageDialog(null,
                            "Thank you for using the Chat Application. Goodbye!",
                            "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                    break;

                default:
                    // Handle dialog close (e.g., clicking 'X')
                    if (choice == -1) {
                        isRunning = false;
                        JOptionPane.showMessageDialog(null,
                                "Thank you for using the Chat Application. Goodbye!",
                                "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                    }
                    break;
            }
        }

        // If authenticated, proceed to messaging functionality
        if (isAuthenticated) {
            boolean continueMessaging = true;
            while (continueMessaging) {
                // Prompt for recipient number
                String recipient = JOptionPane.showInputDialog(null,
                        "Enter recipient number (e.g., +27721384462):",
                        "Input Recipient", JOptionPane.QUESTION_MESSAGE);

                // Check if user canceled or entered empty input
                if (recipient == null || recipient.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Recipient number is required.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Prompt for message
                String message = JOptionPane.showInputDialog(null,
                        "Enter message (max 250 characters):",
                        "Input Message", JOptionPane.QUESTION_MESSAGE);

                // Check if user canceled or entered empty input
                if (message == null || message.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Message is required.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Create and process message
                Message msg = new Message(recipient, message);

                // Check if message is valid
                if (msg.checkMessage() && msg.checkRecipientCell() && msg.checkMessageId()) {
                    // Display message details
                    JOptionPane.showMessageDialog(null,
                            "Message ready to send:\n" + msg.printMessages(),
                            "Message Details", JOptionPane.INFORMATION_MESSAGE);

                    // Prompt user to choose an action
                    String[] options = {"Send Now", "Store for Later", "Disregard", "Exit"};
                    int choice = JOptionPane.showOptionDialog(null, "Choose an action for the message:",
                            "Message Options", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);

                    switch (choice) {
                        case 0: // Send now
                            String sendResult = msg.sendMessage();
                            JOptionPane.showMessageDialog(null, sendResult,
                                    "Send Result", JOptionPane.INFORMATION_MESSAGE);

                            // Ask user if they want to exit after sending
                            int exitChoice = JOptionPane.showConfirmDialog(null,
                                    "Do you want to exit the chat application now?",
                                    "Exit Confirmation",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);

                            if (exitChoice == JOptionPane.YES_OPTION) {
                                continueMessaging = false; // Exit messaging loop
                            }
                            break;

                        case 1: // Store for later
                            boolean storeSuccess = msg.storeMessage();
                            if (storeSuccess) {
                                // Offer to delete the stored message
                                String[] deleteOptions = {"Keep Message", "Delete Message"};
                                int deleteChoice = JOptionPane.showOptionDialog(null,
                                        "Message stored. Do you want to delete it?",
                                        "Delete Option", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                                        null, deleteOptions, deleteOptions[0]);
                                if (deleteChoice == 1) { // Delete message
                                    msg.deleteMessage();
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Failed to store message",
                                        "Storage Error", JOptionPane.ERROR_MESSAGE);
                            }
                            break;

                        case 2: // Disregard
                            JOptionPane.showMessageDialog(null, "Message disregarded.",
                                    "Disregard", JOptionPane.INFORMATION_MESSAGE);
                            break;

                        case 3: // Exit messaging
                            continueMessaging = false;
                            break;

                        default:
                            JOptionPane.showMessageDialog(null, "Action cancelled.",
                                    "Cancelled", JOptionPane.WARNING_MESSAGE);
                            break;
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Invalid input. Ensure recipient is a valid South African number (+27 followed by 9 digits) and message is <= 250 characters.",
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Display total messages sent and stored
                int totalSent = Message.returnTotalMessages();
                int totalStored = getStoredMessageCount();
                String statsMessage = "Statistics:\nTotal messages sent: " + totalSent +
                        "\nTotal messages stored in JSON: " + (totalStored >= 0 ? totalStored : "Error reading file");
                JOptionPane.showMessageDialog(null,
                        statsMessage,
                        "Statistics", JOptionPane.INFORMATION_MESSAGE);

                // If user chose to exit messaging, break the loop
                if (!continueMessaging) {
                    JOptionPane.showMessageDialog(null,
                            "Thank you for using the Chat Application. Goodbye!",
                            "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }

    // Helper method to count messages in test_messages.json
    private static int getStoredMessageCount() {
        try {
            File file = new File(System.getProperty("java.io.tmpdir"), "test_messages.json");
            if (!file.exists()) {
                return 0;
            }
            ObjectMapper mapper = new ObjectMapper();
            List<Message> messages = mapper.readValue(file,
                    mapper.getTypeFactory().constructCollectionType(List.class, Message.class));
            return messages.size();
        } catch (IOException e) {
            return -1; // Indicate error
        }
    }
}
