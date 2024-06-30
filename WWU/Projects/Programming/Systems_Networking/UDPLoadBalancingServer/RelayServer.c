/*
# Name: Nick Chandler
# Description: Relay portion of a client-server application to record a message sent by the client.
# Date: 5/8/2023
# Specification: See assignment 07 description
*/


#include <stdio.h>      //perror, printf
#include <stdlib.h>     // exit, atoi
#include <unistd.h>     // read, write, close
#include <arpa/inet.h>  // sockaddr_in, AF_INET, SOCK_STREAM, INADDR_ANY
#include <string.h>     // memset
#include <time.h>
#include <fcntl.h>
#include <unistd.h>

#include "Utilities.h"

// Recorder 1 stuff
#define RECORDER_IP_ADDRESS "127.0.0.1"  // Destination server's IP
#define IP_ADDR_SIZE 4
// #define RECORDER_PORT 50445  // Destination Server's port




// Recorder 2 stuff
#define RECORDER2_IP_ADDRESS "127.0.0.1"
// #define RECORDER2_PORT 50446


#define TRUE 1
#define SERVER_IP_ADDRESS "0.0.0.0"  // This server's addr
#define SERVER_PORT 50444  // This server's port
#define RECEIVE_BUFFER_SIZE 50

int main(int argc, char const *argv[]) 
{


  if (argc != 3) {
    printf("Wrong args, need <Message Server 1 Port> <Message Server 2 Port>");
    exit(1);
  }

  int RECORDER_PORT = atoi(argv[1]);
  int RECORDER2_PORT = atoi(argv[2]);


  // Set up the echo server socket -- client to relay
  struct sockaddr_in server_endpoint;  // This program
  struct sockaddr_in client_endpoint;  // The program which sends the messages to this one
  char receive_buffer[RECEIVE_BUFFER_SIZE] = {0};
  int sock_desc = CreateUdpServerSocket(&server_endpoint, SERVER_IP_ADDRESS, SERVER_PORT);


  // Set up the relay to message socket
  struct sockaddr_in recorder_endpoint;
  char toRecorder[IP_ADDR_SIZE + RECEIVE_BUFFER_SIZE] = {0};
  int relay_to_record_sock = CreateUdpServerSocket(&recorder_endpoint, RECORDER_IP_ADDRESS, RECORDER_PORT);


  // Set up relay to message server 2 socket
  struct sockaddr_in recorder2_endpoint;
  int relay_to_recorder_sock2 = CreateUdpServerSocket(&recorder2_endpoint, RECORDER2_IP_ADDRESS, RECORDER2_PORT);




  // Success message from recorder server
  char successBuffer[RECEIVE_BUFFER_SIZE] = {0};


  printf("Listening on Network Interface: %s Network Port: %d \n\n", SERVER_IP_ADDRESS, SERVER_PORT);
  int useRecorder2 = 0;

  while (TRUE) 
  {

    // Client Stuff
    printf("Waiting for client connection...\n");
    memset(&client_endpoint, 0, sizeof(client_endpoint)); 
    int byte_count = ReceiveMessageOverUdp(sock_desc, &client_endpoint, receive_buffer, RECEIVE_BUFFER_SIZE);
    char *client_ip_addr = inet_ntoa(client_endpoint.sin_addr);
    printf("Accepted connection: %s:%d\n", client_ip_addr, ntohs(client_endpoint.sin_port));
    printf("Client message received: %s\n", receive_buffer);
    char* status_message = "STATUS_MESSAGE_SUCCESS";
    // Echo message back to client
    byte_count = SendMessageOverUdp(sock_desc, &client_endpoint, status_message, strlen(status_message));


    if (useRecorder2) {
      int relay_to_recorder_byte_count2 = SendMessageOverUdp(relay_to_recorder_sock2, &recorder2_endpoint,
        receive_buffer, strlen(receive_buffer));
      relay_to_recorder_byte_count2 = ReceiveMessageOverUdp(relay_to_recorder_sock2, &recorder2_endpoint, successBuffer, RECEIVE_BUFFER_SIZE);
      useRecorder2 = 0;
    } else {
      // Send the Message
      int relay_to_recorder_byte_count = SendMessageOverUdp(relay_to_record_sock, 
        &recorder_endpoint, receive_buffer, strlen(receive_buffer));
      relay_to_recorder_byte_count = ReceiveMessageOverUdp(relay_to_record_sock, &recorder_endpoint, 
        successBuffer, RECEIVE_BUFFER_SIZE);
      useRecorder2 = 1;
    }
    printf("Data store status message: %s\n\n", successBuffer);


  }

  // Close the file descriptors
  close(sock_desc);
  close(relay_to_record_sock);
  close(relay_to_recorder_sock2);

  return 0;
}