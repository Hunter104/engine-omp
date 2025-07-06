CC = mpicc
CFLAGS = -O2 -Wall -Wextra -fopenmp
EXECUTABLE = gol-hybrid

SRC = jogodavida-hybrid.c
BUILD_DIR = build

all: $(EXECUTABLE) $(BUILD_DIR)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)


$(BUILD_DIR)/$(EXECUTABLE): $(SRC) | $(BUILD_DIR)
	$(CC) $(CFLAGS) -o $@ $<

$(EXECUTABLE): $(BUILD_DIR)/$(EXECUTABLE)

clean:
	rm -rf $(BUILD_DIR) $(EXECUTABLE)

.PHONY: all clean
