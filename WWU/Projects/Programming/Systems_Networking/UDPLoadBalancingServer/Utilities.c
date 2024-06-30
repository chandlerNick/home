#include "Utilities.h"

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#define SO_REUSEPORT_OPTION_VALUE 1

//UDP
int CreateUdpServerSocket(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port)
{
  int socket_desc = socket(AF_INET, SOCK_DGRAM, 0);
  
  int optval = SO_REUSEPORT_OPTION_VALUE;
  setsockopt(socket_desc, SOL_SOCKET, SO_REUSEPORT_OPTION_VALUE, &optval, sizeof(int));

  InitIpSockAddrInfo(sock_addr, AF_INET, ip_addr, port);

  int result = bind(socket_desc, (struct sockaddr *)sock_addr, sizeof(struct sockaddr_in));
 
  return socket_desc;
}

int CreateUdpClientSocket(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port)
{
  int socket_desc = socket(AF_INET, SOCK_DGRAM, 0);
  int optval = SO_REUSEPORT_OPTION_VALUE;
  setsockopt(socket_desc, SOL_SOCKET, SO_REUSEPORT_OPTION_VALUE, &optval, sizeof(int));

  InitIpSockAddrInfo(sock_addr, AF_INET, ip_addr, port);
 
  return socket_desc;
}

int SendMessageOverUdp(int socket_desc, struct sockaddr_in* sock_addr,  char * message, int message_size)
{
  int byte_count = sendto(socket_desc, message, message_size, 0, (struct sockaddr *)sock_addr, sizeof(struct sockaddr_in)); 

  return byte_count;
}

int ReceiveMessageOverUdp(int socket_desc, struct sockaddr_in* sock_addr,  char * receive_buffer, int buffer_size)
{
  memset(receive_buffer, 0, buffer_size);
  socklen_t addr_len = sizeof(struct sockaddr_in);
  int byte_count = recvfrom(socket_desc, (char *)receive_buffer, buffer_size, 0, (struct sockaddr *)sock_addr, &addr_len); 

  receive_buffer[byte_count] = '\0'; 

  return byte_count;
}

//TCP
int CreateTcpServerSocket(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port)
{
  int socket_desc = socket(AF_INET, SOCK_STREAM, 0);
  int optval = SO_REUSEPORT_OPTION_VALUE;
  setsockopt(socket_desc, SOL_SOCKET, SO_REUSEPORT_OPTION_VALUE, &optval, sizeof(int));

  InitIpSockAddrInfo(sock_addr, AF_INET, ip_addr, port);

  int result = bind(socket_desc, (struct sockaddr *)sock_addr, sizeof(struct sockaddr_in));
  
  result = listen(socket_desc, 2);

  return socket_desc;
}

int AcceptTcpConnection(int sock_desc, struct sockaddr_in * client_endpoint)
{
    socklen_t sock_len = sizeof(struct sockaddr_in);

    int client_sock_desc = accept(sock_desc, (struct sockaddr *)client_endpoint, &sock_len);

    return client_sock_desc;
}

int ConnectToTcpServer(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port)
{
  int socket_desc = socket(AF_INET, SOCK_STREAM, 0);

  InitIpSockAddrInfo(sock_addr, AF_INET, ip_addr, port);

  int result = connect(socket_desc, (struct sockaddr *)sock_addr, sizeof(struct sockaddr_in));

  return socket_desc;
}

int SendMessageOverTcp(int socket_desc, char * message)
{
  int result = write(socket_desc, message, strlen(message));

  return result;
}

int ReceiveMessageOverTcp(int socket_desc, char * receive_buffer, int buffer_size)
{
  memset(receive_buffer, 0, buffer_size);

  int result = read(socket_desc, receive_buffer, buffer_size);

  return result;
}

//TCP & UDP
void InitIpSockAddrInfo(struct sockaddr_in* sock_addr, sa_family_t sa_family, const char * ip_addr, in_port_t port)
{
  memset(sock_addr, 0, sizeof(struct sockaddr_in));

  sock_addr->sin_family = sa_family;
  sock_addr->sin_addr.s_addr = inet_addr(ip_addr);
  sock_addr->sin_port = htons(port);
}

void ReadUserInput(char* prompt, char* read_buffer, int read_buffer_size)
{
  memset(read_buffer, 0, read_buffer_size);

  printf("%s", prompt);

  char* returnValue = fgets(read_buffer, read_buffer_size, stdin);

  read_buffer[strcspn(read_buffer, "\n")] = 0;
}

