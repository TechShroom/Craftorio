Rail Signal Model
=====
The model uses these textures: `top`, `side`, `light`, and `bottom`.
The rail signal is designed as follows:

It has an almost-flat top shaped as a 2x1x2 box, textured with `top`.
This is the first element, positioned from [7, 14, 7] to [9, 15, 9].

It has a pole at the bottom that is 2x1x2 and textured with `bottom`.
This is the second element, positioned from [7, 0, 7] to [9, 0, 9]. 

On each side it has 3 lights, green-yellow-red.
These are textured using `light` accompanied by the respective tint.

Each of these lights is a single __element__, a 2x1x2 box that is tinted accordingly.
These are the third through 14th elements (3 lights times 4 sides = 12).

The reset of the signal is a 4x4x14 box,
comprised of one element per side and textured with `side`.
These are the 15th through 18th elements (4 sides).