#ifndef UTILITIES_H
#define UTILITIES_H

#include <arpa/inet.h>

//UDP
int CreateUdpServerSocket(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port);
int CreateUdpClientSocket(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port);
int SendMessageOverUdp(int socket_desc, struct sockaddr_in* sock_addr,  char * message, int message_size);
int ReceiveMessageOverUdp(int socket_desc, struct sockaddr_in* sock_addr, char * receive_buffer, int buffer_size);

//TCP
int CreateTcpServerSocket(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port);
int AcceptTcpConnection(int sock_desc, struct sockaddr_in * client_endpoint);
int ConnectToTcpServer(struct sockaddr_in* sock_addr, const char * ip_addr, in_port_t port);
int SendMessageOverTcp(int socket_desc, char * message);
int ReceiveMessageOverTcp(int socket_desc, char * receive_buffer, int buffer_size);

//TCP & UDP
void InitIpSockAddrInfo(struct sockaddr_in* sock_addr, sa_family_t sa_family, const char * ip_addr, in_port_t port);

//Misc
void ReadUserInput(char* prompt, char* read_buffer, int read_buffer_size);

#endif
