# Hack Computer & Software (Nand2Tetris)

A bottom-up implementation of a general-purpose computer system, starting from basic logic gates up to a high-level compiler and operating system.

## Project Structure

* **Hardware (HDL):**
  * Logic gates
  * Sequential logic (16-bit RAM, Program Counter)
  * Hack CPU & Memory-Mapped I/O integration (Screen and Keyboard)
* **Software Stack:**
  * **Assembler:** Translates Hack Assembly into 16-bit binary machine instructions.
  * **VM Translator:** Compiles stack-based virtual machine code down to Hack Assembly.
  * **Compiler:** Code generator for the Jack programming language.
  * **OS:** Standard library handling graphics output, dynamic heap allocation, and math modules.
