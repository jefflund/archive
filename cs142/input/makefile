bin = input
objs = input.o

$(bin): $(objs)
	g++ -Wall -o $@ $(objs)

deps = $(objs:.o=.d)

-include $(deps)

%.o: %.cpp
	g++ -c -Wall -MMD -o $@ $<

all: $(bin)

run: $(bin)
	./$(bin)

clean:
	rm -f $(bin) $(objs) $(deps)

memcheck: $(bin)
	valgrind --tool=memcheck ./$(bin)

.PHONY : all run clean memcheck
