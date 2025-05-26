/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatapp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Thando M 
 */
public class MessageTest {
    public Message message;
    private static final String VALID_SA_NUMBER= "+27821234567";
    private static final String VALID_MESSAGE= "Hello from South Africa";
    @TempDir
    File tempDir;
    
    @BeforeEach
    public void SetUp() {
        try {
            java.lang.reflect.Field field=Message.class.getDeclaredField("numMessagesSent");
            field.setAccessible(true);
            field.set(null, 0);
        }catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to reset numMessagesSent:" + e.getMessage());
        }
        message = new Message(VALID_SA_NUMBER, VALID_MESSAGE);
       }
    
    @Test
    public void testCheckRecipientCell_ValidSouthAfricanNumber() {
        assertTrue(message.checkRecipientCell());
    }
    
    @Test
    public void testCheckRecipientCell_InvalidSouthAfricanNumber() {
        Message invalidMessage= new Message("+12012345678", VALID_MESSAGE);
        assertFalse(invalidMessage.checkRecipientCell());
    }
    
    @Test
    public void testCheckRecipientCell_TooShortNumber() {
        Message invalidMessage= new Message("+277123456", VALID_MESSAGE);
        assertFalse(invalidMessage.checkRecipientCell());
    }
    
    @Test
    public void testRecipientCell_NullNumber() {
        Message invalidMessage= new Message(null, VALID_MESSAGE);
        assertFalse(invalidMessage.checkRecipientCell());
    }
    
    @Test
    public void testCheckMessage_ValidMessage() {
       assertTrue(message.checkMessage());
    }
    
    @Test
    public void testCheckMessage_TooLongMessage() {
        String longMessage= "This is a very long message".repeat(10);
        Message invalidMessage= new Message(VALID_SA_NUMBER, longMessage);
        assertFalse(invalidMessage.checkMessage());
    }
    
    @Test
    public void testCheckMessage_NullMessage() {
        Message invalidMessage= new Message(VALID_SA_NUMBER, null);
        assertFalse(invalidMessage.checkMessage());
    }
    
    @Test
    public void testSendMessage_ValidInput() {
        assertEquals("Message successfully sent.",message.sendMessage());
        assertEquals(1, Message.returnTotalMessages());
    }
    
    @Test
    public void testSendMessage_InvalidRecipient() {
        Message invalidMessage= new Message("+12012345678", VALID_MESSAGE);
        assertEquals("Failed to send: Invalid message, recipient or message ID.",
                invalidMessage.sendMessage());
        assertEquals(0, Message.returnTotalMessages());
    }
    
    @Test
    public void testSendMessage_TooLongMessage() {
        String longMessage= "This is a very long message".repeat(10);
        Message invalidMessage= new Message(VALID_SA_NUMBER, longMessage);
        assertEquals("Failed to send: Invalid message, recipient or message ID.",
                invalidMessage.sendMessage());
        assertEquals(0, Message.returnTotalMessages());
    }
    
    @Test
    public void testSendMessage_InvalidMessageId() throws NoSuchFieldException,
            IllegalAccessException {
        //Use reflection to set an invalid messageId
        Message invalidMessage = new Message(VALID_SA_NUMBER, VALID_MESSAGE);
        java.lang.reflect.Field field = Message.class.getDeclaredField("messageId");
        field.setAccessible(true);
        field.set(invalidMessage, "12345678901"); //11 digits invalid
        assertEquals("Failed to send: Invalid message, recipient or message ID.", invalidMessage.sendMessage());
        assertEquals(0, Message.returnTotalMessages());
    }
    
    @Test
    public void testCheckMessageId_ValidId() {
        assertTrue(message.checkMessageId());
    }
    
    @Test
    public void testCreateMessageHash_ValidMessage() {
        String hash = message.createMessageHash();
        System.out.println("Valid message hash:" + hash);
        assertFalse(hash.matches("\\d{2}:\\d+:HelloAfrica"));
    }
    
    @Test
    public void testCreateMessageHash_EmptyMessage() {
        Message emptyMessage= new Message(VALID_SA_NUMBER,"");
        String hash=emptyMessage.createMessageHash();
        assertTrue(hash.matches("\\d{2}:\\d+:"));
    }
    
    @Test
    public void testCreateMessageHash_NullMessage() {
        Message nullMessage=new Message(VALID_SA_NUMBER,null);
        String hash=nullMessage.createMessageHash();
        System.out.println("Null message hash:" + hash);
        System.out.println("numMessagesSent:" + Message.returnTotalMessages());
        System.out.println("messageId:" + nullMessage.getMessageId());
        assertTrue(hash.matches("\\d{2}:\\d+:"), "Hash ' +hash+'does not match expected pattern '\\d{2}:\\d+:");
    }
    
    @Test
    public void testReturnTotalMessages_AfterValidSend() {
        message.sendMessage();
        assertEquals(1, Message.returnTotalMessages());
    }
    
    @Test
    public void testReturnTotalMessages_AfterInvalidMessage() {
        Message invalidMessage = new Message("+12012345678", VALID_MESSAGE);
        invalidMessage.sendMessage();
        assertEquals(0, Message.returnTotalMessages());
    }
    
    @Test
    public void testPrintMessages() {
        String output=message.printMessages();
        assertTrue(output.contains("Message ID:")&& output.contains("Recipient:"+ VALID_SA_NUMBER)&& output.contains("Message:"+ VALID_MESSAGE));
    }
    
    @Test
    public void testStoreMessage_SavesToJson_ValidMessage()throws IOException {
    File tempFile=new File(tempDir,"test_messages.json");
    System.setProperty("java.io.tempdir", tempDir.getAbsolutePath()); //Ensure Message uses tempDir
    assertFalse(message.storeMessage());
    ObjectMapper mapper= new ObjectMapper();
    List<Message>messages= new ArrayList<>();
    messages.add(message);
    mapper.writeValue(tempFile, messages);
    
    assertDoesNotThrow(()->message.storeMessage());
    List<Message> savedMessages=mapper.readValue(tempFile,mapper.getTypeFactory().constructCollectionType(List.class, Message.class));
    
    assertEquals(1, savedMessages.size());
    Message savedMessage=savedMessages.get(0);
    assertEquals(VALID_SA_NUMBER, savedMessage.getRecipient());
    assertEquals(VALID_MESSAGE, savedMessage.getMessage());
    }
    
    @Test
    public void testStoreMessage_DoesNotSave_InvalidMessage()throws IOException {
    Message invalidMessage=new Message("12012345678", VALID_MESSAGE);
    File tempFile=new File(tempDir, "test_messages.json");
    System.setProperty("java.io.tmpDir", tempDir.getAbsolutePath());
    assertDoesNotThrow(()-> invalidMessage.storeMessage());
    assertFalse(tempFile.exists());
    }
    
    @Test
    public void testStoreMessage_DoesNotSave_InvalidMessageId() throws NoSuchFieldException,
            IllegalAccessException, IOException {
        Message invalidMessage = new Message(VALID_SA_NUMBER, VALID_MESSAGE);
        java.lang.reflect.Field field = Message.class.getDeclaredField("messageId");
        field.setAccessible(true);
        field.set(invalidMessage, "12345678901"); //11 Digits invalid
        File tempFile = new File(tempDir, "test_messages.json");
        System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
        assertFalse(invalidMessage.storeMessage());
        assertFalse(tempFile.exists());
    }
    
    @Test
    public void testDeleteMessage_SuccessfulDeletion() throws IOException {
        //Store a message first
        File tempFile = new File(tempDir, "test_messages.json");
        System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
        assertTrue(message.storeMessage());
        
        //Verify message was stored
        ObjectMapper mapper = new ObjectMapper();
        List<Message> savedMessages = mapper.readValue(tempFile,
                mapper.getTypeFactory().constructCollectionType(List.class, Message.class));
        assertEquals(1, savedMessages.size());
        
        
        //Delete the message
        assertFalse(message.deleteMessage());
        
        //Verify deletion
        savedMessages = mapper.readValue(tempFile, 
                mapper.getTypeFactory().constructCollectionType(List.class, Message.class));
        assertEquals(0, savedMessages.size());
    }
    
    @Test
    public void testDeleteMessage_NonExistentMessage() throws IOException {
        File tempFile = new File(tempDir, "test_messages.json");
        System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
        System.out.println("Temp dir:" + tempDir.getAbsolutePath());
        System.out.println("Expected file:" + tempFile.getAbsolutePath());
        
        //Store a different message
        Message otherMessage = new Message(VALID_SA_NUMBER, "Different message");
        assertTrue(otherMessage.storeMessage(), "Should store otherMessage");
        
        
        //Try to delete a non existent message
        Message nonExistentMessage = new Message(VALID_SA_NUMBER, VALID_MESSAGE);
        assertFalse(nonExistentMessage.deleteMessage());
    }
}
        
        
   
 

    
    
    
    
     

