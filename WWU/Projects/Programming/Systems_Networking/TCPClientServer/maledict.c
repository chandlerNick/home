#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/select.h>
#include "curses.h"



// BIG QUESTION: How do I take care of the payloads for both the req and resp?
// Just send them after!

// Question: Why des my request header have a garbage value?

// Potentially only a few bugs left!

#define MAXCURSE 13

// Sets up the fixed number of ports
// Inputs: empty socket array of length 4
// empty sock addr array of length 4
void set_up_ports(int sockets[], struct sockaddr_in saddr[]) {
  // Create socket 0
  sockets[0] = socket(AF_INET, SOCK_STREAM, 0);

  // Socket address 0
  saddr[0].sin_family = AF_INET;
  saddr[0].sin_port = htons(CURSES_PORT0);
  saddr[0].sin_addr.s_addr = htonl(INADDR_ANY);

  // Bind socket and address
  if (bind(sockets[0], (struct sockaddr*) &(saddr[0]),
    sizeof(struct sockaddr_in)) != 0) {
    perror("Could not bind");
    exit(3);
  }

  // Avoid bind error for port already in use
  int tr0 = 1;
  if (setsockopt(sockets[0], SOL_SOCKET, SO_REUSEADDR,
    &tr0, sizeof(int)) == -1) {
    perror("setsockopt");
    exit(3);
  }

  // Set up port for listening
  if (listen(sockets[0], 4) == -1) {
    perror("Issue settin up listen");
    exit(3);
  }


  // Socket 1
  sockets[1] = socket(AF_INET, SOCK_STREAM, 0);

  saddr[1].sin_family = AF_INET;
  saddr[1].sin_port = htons(CURSES_PORT1);
  saddr[1].sin_addr.s_addr = htonl(INADDR_ANY);

  // Bind socket and address
  if (bind(sockets[1], (struct sockaddr*) &(saddr[1]),
    sizeof(struct sockaddr_in)) != 0) {
    perror("Could not bind");
    exit(3);
  }

  // Avoid bind error for port already in use
  int tr1 = 1;
  if (setsockopt(sockets[1], SOL_SOCKET, SO_REUSEADDR,
    &tr1, sizeof(int)) == -1) {
    perror("setsockopt");
    exit(3);
  }

  // Set up port for listening
  if (listen(sockets[1], 4) == -1) {
    perror("Issue settin up listen");
    exit(3);
  }

  // Socket 2
  sockets[2] = socket(AF_INET, SOCK_STREAM, 0);

  saddr[2].sin_family = AF_INET;
  saddr[2].sin_port = htons(CURSES_PORT2);
  saddr[2].sin_addr.s_addr = htonl(INADDR_ANY);

  // Bind socket and address
  if (bind(sockets[2], (struct sockaddr*) &(saddr[2]),
    sizeof(struct sockaddr_in)) != 0) {
    perror("Could not bind");
    exit(3);
  }

  // Avoid bind error for port already in use
  int tr2 = 1;
  if (setsockopt(sockets[2], SOL_SOCKET, SO_REUSEADDR,
    &tr2, sizeof(int)) == -1) {
    perror("setsockopt");
    exit(3);
  }

  // Set up port for listening
  if (listen(sockets[2], 4) == -1) {
    perror("Issue settin up listen");
    exit(3);
  }


  // Socket 3
  sockets[3] = socket(AF_INET, SOCK_STREAM, 0);

  saddr[3].sin_family = AF_INET;
  saddr[3].sin_port = htons(CURSES_PORT3);
  saddr[3].sin_addr.s_addr = htonl(INADDR_ANY);

  // Bind socket and address
  if (bind(sockets[3], (struct sockaddr*) &(saddr[3]),
    sizeof(struct sockaddr_in)) != 0) {
    perror("Could not bind");
    exit(3);
  }

  // Avoid bind error for port already in use
  int tr3 = 1;
  if (setsockopt(sockets[3], SOL_SOCKET, SO_REUSEADDR,
    &tr3, sizeof(int)) == -1) {
    perror("setsockopt");
    exit(3);
  }

  // Set up port for listening
  if (listen(sockets[3], 4) == -1) {
    perror("Issue settin up listen");
    exit(3);
  }

  return;
}


// Retrieves the proper number of curses
// NOTE: In addition to freeing the 2D array, you must
// also free the individual strings, each one is malloced
// with strndup
char** get_curses(void) {
  int i, leng;
  FILE* in;

  // Allocate memory for the curses
  char** lineArr = (char **)calloc(13, sizeof(char *));
  char buf[BUFSIZ];


  // Open the curses file
  if ((in = fopen("./curses.txt", "r")) == NULL) {
    perror("Error reading curses file");
    exit(2);
  }

  for (i = 0; i < 13; i++) {
    fgets(buf, BUFSIZ, in);
    leng = (int) strlen(buf);
    buf[leng-1] = '\0';
    buf[leng] = '\0';
    lineArr[i] = strndup(buf, strlen(buf));  // a[i] == *(a + i)
  }
  fclose(in);
  return lineArr;
}


// Helper function to print curses -- dubugging!
void print_curse_array(char** curses) {
  for (int i = 0; i < 13; i++){
    printf("Line %d - %s", i, curses[i]);
  }
  return;
}


// Calls accpet and does error checking
int my_accept(int currentSocket, struct sockaddr_in saddr) {
  unsigned int addrlen = sizeof(struct sockaddr_in);
  int readfd;

  if ((readfd = accept(currentSocket, (struct sockaddr *) &saddr,
    &addrlen)) == -1) {
    perror("Couldn't Accept");
    exit(3);
  }

  return readfd;
}

// Look at the arguments
int get_request_arg(int clientfd) {
  // Declare Vars
  int result;

  // Read the arg
  recv(clientfd, &result, sizeof(int), 0);

  // Convert to host data formatting
  result = ntohl(result);
  fprintf(stderr, "%d\n", result);
  // fprintf(stderr, "Made it to the end of get_request_arg\n");
  // return an int number of curses
  return result;
}



// Take the request header out with first read
// EXIT 4 on request error
void process_request_header(int clientfd, int* num_curses,
  ResponseHeader* respH) {
  // Declare variables
  RequestHeader req;

  // Read the Request header from the fd
  if (recv(clientfd, &req, sizeof(RequestHeader), 0) == 0){
    perror("No bytes read");
    exit(3);
  }

  // ntoh on all the fields of reqheader
  req.protocol_id = ntohl(req.protocol_id);
  req.numargs = ntohs(req.numargs);
  req.op = ntohl(req.op);
  // Do control flow.

  // Examine protocol_id
  if (req.protocol_id == CURSES_PROTOCOL_ID) {
    // Protocol_Id is good, finish
    // Deal with args
    if (req.numargs > (int16_t) 0) {  // YES ARGS
      if (req.op == (int32_t) PING) {
        // Should have no args
        respH->status = BAD_REQUEST;
      } else if (req.op == (int32_t) GET_SINGLE) {
        // Should have no args
        respH->status = BAD_REQUEST;
      } else if (req.op == (int32_t) GET_MULTI) {
        // Should have 1 arg
        // Get args
        *num_curses = (int) req.numargs;
        // Set status
        respH->status = OK;
      } else {
        // Invalid Operation
        respH->status = BAD_REQUEST;
      }
    } else if (req.numargs == (int16_t) 0) {  // NO ARGS
      if (req.op == (int32_t) PING) {
        // Set status
        respH->status = OK;
      } else if (req.op == (int32_t) GET_SINGLE) {
        // Set status and num_curses
        respH->status = OK;
        *num_curses = 1;
      } else if (req.op == (int32_t) GET_MULTI) {
        // No args for a multi
        respH->status = INSUFFICIENT_ARGS;
      } else {
        // Invalid Operation
        respH->status = BAD_REQUEST;
      }
    } else {  // INVALID ARGS
      respH->status = BAD_REQUEST;
    }
  } else {
    // ProtocolID != 13 -- INVALID!
    respH->status = INVALID_PROTOCOL;
  }


  return;
}


// Write response
void form_response(int clientfd, int* num_curses,
  ResponseHeader* respH, char** curses) {
  char* Malediction = NULL, buf[BUFSIZ];
  int curseNum, i;

  memset(buf, '\0', strlen(buf));
  // Check the status
    // If shit -- leave early
    // If Multi
      // Make curse String
    // If Single
      // Make curse String
    // If Ping
      // Do nothing
  if (respH->status == OK) {
    // Good Status
    if (*num_curses == 0) {
      // PING
      // Alles gut, weiter
    } else if (*num_curses == 1) {
      // GET_SINGLE
      // Use simple rand to get a curse from curses
      curseNum = random() % MAXCURSE;
      Malediction = curses[curseNum];
      respH->length = (int) strlen(Malediction);
    } else if (*num_curses > 1) {
      // GET_MULTI
      for (i = 0; i < (*num_curses); i++) {
        // Add a curse to each line
        curseNum = random() % MAXCURSE;
        if (i > 0) {
          strcat(buf, "\n");
        }
        // fprintf(stderr, "Curse %d: %s\n", i, curses[curseNum]);
        strcat(buf, curses[curseNum]);
      }
      Malediction = buf;
      respH->length = (int) strlen(Malediction);
    }
  }

  // Format for network -- everything should be set by here
  respH->protocol_id = htonl(respH->protocol_id);
  respH->status = htonl(respH->status);
  respH->length = htonl(respH->length);

  // Send it!
  // Header
  if (send(clientfd, respH, sizeof(ResponseHeader), 0) == 0) {
    perror("No bytes written");
    exit(3);
  }

  // Curses
  if (Malediction != NULL) {
    // fprintf(stderr, "Malediction: %s\n", Malediction);
    if (send(clientfd, Malediction, strlen(Malediction), 0) == 0) {
      perror("No bytes written");
      exit(3);
    }
  }
  return;
}


int main(int argc, char *argv[]) {
  int opt, seed = 1, i;
  unsigned int addrlen = sizeof(struct sockaddr_in);


  // Proccess command line arguments
  if (argc > 3) {
    perror("Wrong number of args");
    exit(1);
  }

  while ((opt = getopt(argc, argv, "s:")) != -1) {
    switch (opt) {
      case 's':
        // Initialize the random number generator
        seed = atoi(optarg);
        srandom((unsigned int) seed);
        break;
      case '?':  // Unrecognized arg
        perror("Unrecognized args");
        exit(1);
        break;
      default:  // No args?
        break;
    }
  }

  // Read in curses to a char**
  char** curses = get_curses();
  // print_curse_array(curses);  // Works Properly!

  // Open the 4 TCP ports
  int sockets[4];  // Socket fds
  struct sockaddr_in saddrArr[4];  // socket addresses
  set_up_ports(sockets, saddrArr);


  // Make fd set -- will alter the set passed in
  fd_set read_sockets;
  FD_ZERO(&read_sockets);
  for (i = 0; i < 4; i++) {
    FD_SET(sockets[i], &read_sockets);
  }

  // Set up some I/O objects
  int clientfd, num_curses = 0;  // num_curses = 0 by default (no curses)
  ResponseHeader* respH = (ResponseHeader *) malloc(sizeof(ResponseHeader));
  respH->protocol_id = CURSES_PROTOCOL_ID;
  respH->length = 0;


  // Select a readable socket
  if (select(FD_SETSIZE, &read_sockets, NULL, NULL, NULL) < 0) {
    perror("Error on reading select");
    exit(3);
  }

  if (FD_ISSET(sockets[0], &read_sockets)) {
    // Socket 0 is available
    clientfd = my_accept(sockets[0], saddrArr[0]);
  }

  if (FD_ISSET(sockets[1], &read_sockets)) {
    // Socket 1 is available
    clientfd = my_accept(sockets[1], saddrArr[1]);
  }

  if (FD_ISSET(sockets[2], &read_sockets)) {
    // Socket 2 is available
    clientfd = my_accept(sockets[2], saddrArr[2]);
  }

  if (FD_ISSET(sockets[3], &read_sockets)) {
    // Socket 3 is available
    clientfd = my_accept(sockets[3], saddrArr[3]);
  }

  // Lesung machen
  process_request_header(clientfd, &num_curses, respH);
  // das Schreiben erledigen
  form_response(clientfd, &num_curses, respH, curses);
  // Beenden nach dem Senden der Antwort
  close(clientfd);
  for (i = 0; i < 4; i++) {
    close(sockets[i]);
  }


  // Free the curses
  for (i = 0; i < 13; i++){
    free(curses[i]);
  }
  free(curses);
  free(respH);
  return 0;
}
