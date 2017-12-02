CC=gcc
CFLAGS=-Wall -g

all: generator 
	@echo "Ready for use."
	
generator: CSE353_Fall2017_Project3_FileGenerator.c
	${CC} ${CFLAGS} CSE353_Fall2017_Project3_FileGenerator.c -o P3_generator
	
clean:
	rm -f P3_generator node*.txt
