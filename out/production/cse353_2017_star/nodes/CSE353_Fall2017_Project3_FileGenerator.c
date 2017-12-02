#include <stdio.h>
#include <stdlib.h>
#include <time.h>

void printHelp(char *);

int main(int argc, char ** argv)
{
	int numNets,numNodes, numLines, i, j, k, n,length, destnet,destnode;//, trafficType;
	char fname[15];

	if (argc != 4)
	{
		printHelp(argv[0]);
		exit(1);
	}
	srand(time(NULL));
	numNets = atoi(argv[1]);
	numNodes = atoi(argv[2]);
	numLines = atoi(argv[3]);

	if ((numNodes < 2) || (numNodes > 16) || (numNets > 16) ||(numNets < 2) )
	{
		printf("Number of nodes or nets are out of range (given %d or %d, should be 2-255)\n",numNodes,numNets);
		printHelp(argv[0]);
		exit(1);
	}

	printf("Generating data for %d nodes, %d lines each.\n",numNodes,numLines);
	for (n=1;n<=numNets;n++){
		for (i=1;i<=numNodes;i++){
			sprintf(fname,"node%d_%d.txt",n,i);
			FILE * f = fopen(fname,"w");
			if (f == NULL)
			{
				perror("Cannot open output file");
				exit(2);
			}
			// printf("writing %s\n",fname);
			for (j=0;j<numLines;j++)
			{
				length = (rand() % 100);
				//trafficType = (rand() % 2);
				// printf("\tlen %d, type %d, ",length,trafficType);
				do{
					destnet = (rand() % numNets+1);
					destnode = (rand() % numNodes+1);
				}while (destnode == i && destnet == n);
				// printf("dest: %d,",dest);

				fprintf(f,"%d_%d,",destnet,destnode );
				for (k=0;k<length;k++)
				{
					fputc((char)(rand() % 93) + 32,f);
				}
				fputc('\n',f);
				// printf("data\n");
			}
			fclose(f);
			f = NULL;

		}
	}
	printf("Done.\n");
	return 0;
}

void printHelp(char * progName)
{
	printf("CSE353-Fall2017 Project 3 File Generator\n");
	printf("Usage: %s [Number of CAS's][Number of Nodes] [Number of Lines]\n",progName);
	//printf("Usage: %s [Number of Nodes] [Number of Lines]\n",progName);
	printf("\tNumber of CAS's: Number of CAS switches to connect the to CCS\n");
	printf("\tNumber of Nodes: Number of nodes to generate data for\n");
	printf("\tNumber of Lines: Number of lines to generate for each file.\n");
}
