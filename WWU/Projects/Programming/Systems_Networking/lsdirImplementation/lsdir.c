#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/types.h>
#include <string.h>
#include <ctype.h>



// Print usage and exit
void usage() {
  fprintf(stderr, "Usage: ./lsdir [-d] [-r] dir1 [dir2 ..]\n");
  fprintf(stderr, "-d print whether each element is a direcotry\n");
  fprintf(stderr, "-r prints all subdirectories using recursion\n");
  fprintf(stderr, "Square brackets, '[]' signify optional arguments\n");
  return;
}


void printerSimple(char* path, int dFlag) {
  struct dirent *dir;  // directory entry pointer
  char* newpath;
  DIR *dr = opendir(path);

  if (dr == NULL) {
    perror("Directory couldn't open");
    exit(3);
  }

  while ((dir = readdir(dr)) != NULL) {
    char* name = dir->d_name;

    // If the deep flag is set, print type then directory
    if (dFlag == 1) {
      if (dir->d_type == DT_DIR) {
        printf("DIR ");
      } else {
        printf("REG ");
      }
    }
    printf("%s/%s\n", path, name);
  }
  closedir(dr);
  return;
}

// recursive printer -- No r flag since always recursive
void recursivePrinter(char* path, int dFlag) {
  struct dirent *dir;  // directory entry pointer
  char* newpath;
  DIR *dr = opendir(path);

  if (dr == NULL) {
    return;  // Some back to this -- potential bug
  }
  while ((dir = readdir(dr)) != NULL) {
    char* name = dir->d_name;
    if (dir->d_type == DT_DIR && strcmp(name, ".")
      != 0 && strcmp(name, "..") != 0) {
      asprintf(&newpath, "%s/%s", path, name);
      printerSimple(newpath, dFlag);

      // Recurse:
      recursivePrinter(newpath, dFlag);

      free(newpath);
    }
  }
  closedir(dr);
  return;
}

int main(int argc, char* argv[]) {
  // Your code here
  int i, opt, j, numTargets;
  usage();

  // 0: not deep, 1: deep
  int deepFlag = 0;

  // 0: not recursive, 1: recursive
  int recursive = 0;

  if (argc < 2) {
    perror("Insufficent args");
    exit(1);
  }

  while ((opt = getopt(argc, argv, "dr")) != -1) {
    switch (opt) {
      case 'd':
        deepFlag = 1;
        break;
      case 'r':
        recursive = 1;
        break;
      default:
        perror("unexpected args");
        exit(1);
        break;
    }
  }


  numTargets = argc - optind;
  // Should be 1 on default case
  char* dirList[numTargets];

  // Put in arguments to the dirList
  for (i = optind; i < argc; i++) {
    dirList[i-optind] = argv[i];
    j++;
  }

  // Execute
  for (i = 0; i < numTargets; i++) {
    // printf("Dir: %s\n", dirList[i]);  // Print out the start directory

    // Detailed and recursive print
    if (deepFlag == 1 && recursive == 1) {
      // Recursive and detailed!
      printerSimple(dirList[i], 1);  // Modification
      recursivePrinter(dirList[i], 1);
    } else if (deepFlag == 1 && recursive == 0) {
      // detailed not recursive
      printerSimple(dirList[i], 1);
    } else if (recursive == 1 && deepFlag == 0) {
      // recursive not detailed
      recursivePrinter(dirList[i], 0);
    } else {
      // Neither deep nor recursive
      printerSimple(dirList[i], 0);
    }
    // printf("Depth %d\n", depth);
  }


  return 0;
}
