# Three Address Code (TAC)

For this project, the intermediate representation (IR) we are using is three address code. These instructions have at most 3 operands, the destination, left operand, and right operand. There is also some operation being performed.

## Instruction Types

### Assign

These instructions all have the destination, left operand, and right operand values.

- Add
- Adda
- And
- Ash
- Cmp
- Div
- Lsh
- Mod
- Move
- Mul
- Or
- Pow
- Sub
- Xor

### Jump

These instructions have a comparison and a destination to jump to if the condition is false.

- Beq
- Bge
- Bgt
- Ble
- Blt
- Bne

### Print

These instructions are for printing to the screen. They take in 1 argument.

- Write
- WriteB
- WriteNL
