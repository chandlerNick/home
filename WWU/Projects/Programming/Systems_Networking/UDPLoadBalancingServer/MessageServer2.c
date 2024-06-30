/*
# Name: Nick Chandler
# Description: Second recorder portion of a client-server application to record a message sent by the client.
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



#define IP_ADDRESS_SIZE 4
#define TIME_SERVER_IP_ADDRESS "132.163.96.6"
#define TIME_SERVER_PORT 37
#define TRUE 1
#define SERVER_IP_ADDRESS "0.0.0.0"  // This server's IP addr
// #define SERVER_PORT 50446  // This server's port
#define RECEIVE_BUFFER_SIZE 50
#define NTP_UNIX_EPOCH_TIMESTAMP_DELTA 2208988800 //Seconds between 1/1/1900 00:00 and 1/1/1970 00:00

int main(int argc, char const *argv[]) 
{

  if (argc != 2) {
    printf("Wrong args. Need a port as an arg\n");
    exit(1);
  }

  int SERVER_PORT = atoi(argv[1]);


  // Set up the echo server socket -- client to relay
  struct sockaddr_in server_endpoint;  // This program is the client here
  struct sockaddr_in client_endpoint;  // The program which sends the messages to this one
  char receive_buffer[RECEIVE_BUFFER_SIZE] = {0};
  int sock_desc = CreateUdpServerSocket(&server_endpoint, SERVER_IP_ADDRESS, SERVER_PORT);


  // Set up the time server stuff
  struct sockaddr_in time_server_endpoint;  // This is the port 37 endpoint -- "thing that sends time"
  struct sockaddr_in time_client_endpoint;  // This program is the client here
  char recieve_time[RECEIVE_BUFFER_SIZE];  // Take the time here
  memset(recieve_time, 0, RECEIVE_BUFFER_SIZE);
  int time_sock = CreateUdpServerSocket(&time_client_endpoint, TIME_SERVER_IP_ADDRESS, TIME_SERVER_PORT);




  // Open the output file
  int writeFile;
  writeFile = open("Data.txt", O_WRONLY | O_CREAT | O_TRUNC, 0666);
  if (writeFile == -1) {
    perror("Open Error");
    return 1;
  }


  printf("Listening on Network Interface: %s Network Port: %d \n\n", SERVER_IP_ADDRESS, SERVER_PORT);
 
  while (TRUE) 
  {
    printf("Waiting for relay server connection...\n");

    //Receive message from client
    memset(&client_endpoint, 0, sizeof(client_endpoint)); 
    int byte_count = ReceiveMessageOverUdp(sock_desc, &client_endpoint, receive_buffer, RECEIVE_BUFFER_SIZE);

    char *client_ip_addr = inet_ntoa(client_endpoint.sin_addr);
    printf("Accepted connection: %s:%d\n", client_ip_addr, ntohs(client_endpoint.sin_port));


    printf("Message forwarded from relay server: %s \n\n", receive_buffer);

    // Contact time server here
    int time_byte_count = SendMessageOverUdp(time_sock, &time_client_endpoint, recieve_time, RECEIVE_BUFFER_SIZE);
    time_byte_count = ReceiveMessageOverUdp(time_sock, &time_server_endpoint, recieve_time, RECEIVE_BUFFER_SIZE);
    uint32_t network_byte_order = *((uint32_t*)(recieve_time));
    uint32_t host_byte_order = ntohl(network_byte_order); 
    uint32_t seconds_since_1900 = host_byte_order; 
    uint32_t seconds_since_1970 = seconds_since_1900 - NTP_UNIX_EPOCH_TIMESTAMP_DELTA;
    time_t seconds_since_1970_time_t = (time_t)seconds_since_1970;
    char* timeString = ctime(&seconds_since_1970_time_t);  // Time string

    lseek(writeFile, 0, SEEK_END);
    dprintf(writeFile, "%s%s\n%s\n\n", timeString, client_ip_addr, receive_buffer);
    lseek(writeFile, 0, SEEK_END);


    char* status_message = "STATUS_MESSAGE_SUCCESS";
    
    // Echo message back to client
    byte_count = SendMessageOverUdp(sock_desc, &client_endpoint, status_message, strlen(status_message));
  }

  // Close the file descriptors
  close(sock_desc);
  close(time_sock);
  close(writeFile);

  return 0;
}