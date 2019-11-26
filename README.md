# GameEngine
This project is a 3D rigid-body physics engine written on top of a previous rendering engine that I built in Java. Once again, this is entirely written from scratch apart from the LWJGL API. The GPU accelerated collision engine uses a simple non-differential algorithm to determine point intersections, offering an improved performance over conventional approaches. This engine can model any 3D geometry with an estimated mass and inertia distribution given that the computer has adequate specifications. Here is a [demo](
https://youtu.be/uJYGmMbkCIs) of some simulations.

## Rendering Engine
The rendering engine used for this project is built on top of LWGJL. It is capable of rendering any obj-defined object with a respective texture map. There is no shadow implementation and simple form of directional lighting is used.

## Rigid-body Simulation Engine
### Terminology
* **Physics Entity** - A renderable entity that is used for physics calculations.
* **CurrE** - The current entity in which collisions are being detected for.
* **PhysE** - A physics entity that is being compared to the current entity.

## Pre-Collision Pipeline
The bulk of the complexity of this project comes from the physics simulaiton engine. The first step is to partition the world space into chunks in which the engine will check collisions in. This allows prevents the checking comparison betewen every physics entity in the world by limiting comparisons to be within a chunk. When two objects in the same chunk are being compared, the first step is to check radius bounds. When an object is loaded, the maximum radius to the center of the radius is calculated. By first comparing the radii between two entities, we can very quickly check if there is any posibility for collision. If the radii are sufficiently close, the first of three gpu-acclerated processes is used.

## GPU-Accelerated Collision Pipeline
* **Vertex Transformer** - Transforms the physics entity into the space of the current entity.
* **Vertex Validator** - Compares every point on the physics entity to determine if it is within the radius of the current entity.
* **Collision Detector** - Compares a single validated vertex from `Vertex Validator` to every plane of the current entity. This kernel has a more pipeline than the other two:
  - Determine if the plane in question faces the direction of the validated vertex.
  - Determine if the validated vertex is travelling in the direction of the plane in question.
  - Calculate the time of intersection between the velocity vector and the plane. 
  - Solve for the coordinates of intersection with the time.
  - Check if the coordinates of interesection lie within the plane in question.
  <a/>
If any of these processes fail, the kernel terminates, resulting in an optimized pipeline that limits the number of actual collisions it must calculate. If the validated vertex collides with a plane within the delta t of the program loop, a collision has been detected. 

## Post-Collision Calculation
The post-collision calculation for the most part simple, relying on concepts of impulse and conservation of linear and angular momentum to determine final velocities. However, converting rotors and rotation vectors between the various object spaces proved to be complicated and is a point of inefficiency in the program. This can be optimized in the future with hardware acceleration. 

## Differences over Other Designs
Unlike other designs, this implementation calculates the exact point of intersection instead of using a differential approach to approximate the point of intersection. This reduces the number of calculations needed to be made as the differential approach has to iteratively find the intersection. Furthermore, with the pre-collision pipeline optimizations and the optimizations in the `Collision Detector` kernel, this program can accurately model complex geometry collisions in real time. 

## Use of Rotors over Quaternions
To transform axis-angle representations of object rotations into various spaces for the physics engine pipeline. I used a rotor implementation to simplify the linear algebra in later parts of the program. I opted to use rotors because they are relatively easier to conceptually understand and have an identical run time to quaternions. This is inspired from this fantastic [website]( https://marctenbosch.com/quaternions/) by Marcten Bosch.
