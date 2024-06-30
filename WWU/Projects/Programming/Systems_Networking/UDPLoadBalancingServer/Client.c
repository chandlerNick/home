/*
# Name: Nick Chandler
# Description: Client portion of a client-server application to record a message sent by the client with a relay server.
# Date: 5/8/2023
# Specification: See assignment 07 description
*/

#include <stdio.h>      // perror, printf
#include <stdlib.h>     // exit, atoi
#include <unistd.h>     // write, read, close
#include <arpa/inet.h>  // sockaddr_in, AF_INET, SOCK_STREAM, INADDR_ANY
#include <string.h>     // strlen, memset

#include "Utilities.h"

#define SERVER_IP_ADDRESS "127.0.0.1"
#define SERVER_PORT 50444
#define RECEIVE_BUFFER_SIZE 50
#define MESSAGE_BUFFER_SIZE 50

int main(int argc, char const *argv[]) 
{
  struct sockaddr_in server_endpoint;
  char message[MESSAGE_BUFFER_SIZE] = {0};
  char receive_buffer[RECEIVE_BUFFER_SIZE] = {0};

  int sock_desc = CreateUdpClientSocket(&server_endpoint, SERVER_IP_ADDRESS, SERVER_PORT);
  
  //Input user's message
  ReadUserInput("Enter message or quit to exit: ", message, MESSAGE_BUFFER_SIZE);
   
  int quit = strcmp(message, "quit");

  while (quit != 0)
  {
    //Send user's message to server
    int byte_count = SendMessageOverUdp(sock_desc, &server_endpoint, message, strlen(message));
            
    //Receive echoed message from server
    byte_count = ReceiveMessageOverUdp(sock_desc, &server_endpoint, receive_buffer, RECEIVE_BUFFER_SIZE);
    
    printf("Server status message: %s \n\n", receive_buffer);

    //Input user's message
    ReadUserInput("Enter message or quit to exit: ", message, MESSAGE_BUFFER_SIZE);
   
    quit = strcmp(message, "quit");
  }
  
  close(sock_desc);

  return 0;
}
