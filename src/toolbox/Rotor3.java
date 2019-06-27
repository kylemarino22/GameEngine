package toolbox;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class Rotor3 {

    public float a;
    public float xy;
    public float xz;
    public float yz;

    //Default Rotor
    public Rotor3 () {
        a = 1;
        xy = xz = yz = 0;
    }

    //Vector to Vector
    public Rotor3 (Vector3f from, Vector3f to) {

        //scalar
        a = 1 + Vector3f.dot(to, from);

        Bivector3 minusBivector = outer(to, from);
        xy = minusBivector.xy;
        xz = minusBivector.xz;
        yz = minusBivector.yz;
        normalize();
    }

    //Plane-Angle implementation
    public Rotor3 (Vector3f direction, Vector3f radius, float theta ) {
        Vector3f normRadius = radius.normalise(null);
        Vector3f perpDirection = Vector3f.sub(direction, Maths.scale(normRadius, Vector3f.dot(direction, normRadius)), null);

        Vector3f perpNormal = perpDirection.normalise(null);

        Bivector3 rotPlane = outer(normRadius, perpNormal);
        a = (float) Math.cos(theta/2.0f);
        float sina = (float) Math.sin(theta/2.0f);

        xy = -sina * rotPlane.xy;
        xz = -sina * rotPlane.xz;
        yz = -sina * rotPlane.yz;
        normalize();

    }

    //Rotate ---------------------------------------------------
    public Vector3f rotate (Vector3f p) {
        //Qp step
        Vector3f u = new Vector3f();
        u.x = a * p.x + p.y * xy + p.z * xz;
        u.y = a * p.y - p.x * xy + p.z * yz;
        u.z = a * p.z - p.x * xz - p.y * yz;

        float trivec = - p.x * yz + p.y * xz - p.z * xy;

        //p*Q step
        Vector3f v = new Vector3f();
        v.x = a * u.x + u.y    * xy + u.z    * xz - trivec * yz;
        v.y = a * u.y - u.x    * xy + trivec * xz + u.z    * yz;
        v.z = a * u.z - trivec * xy - u.x    * xz - u.y    * yz;

        return v;

    }
    // ---------------------------------------------------------


    //multiply rotors ----------------------------------------------------------
    public Rotor3 multiply (Rotor3 q) {
        Rotor3 r = new Rotor3();

        r.a  = this.a  * q.a - this.xy * q.xy - this.xz * q.xz - this.yz * q.yz;
        r.xy = this.xy * q.a + this.a  * q.xy + this.yz * q.xz - this.xz * q.yz;
        r.xz = this.xz * q.a + this.a  * q.xz - this.yz * q.xy + this.xy * q.yz;
        r.yz = this.yz * q.a + this.a  * q.yz + this.xz * q.xy - this.xy * q.xz;

        return r;
    }
    // -------------------------------------------------------------------------





    //normalize rotor -----------------------------

    //Length Squared
    private double lengthSquared () {
        return a * a + xy * xy + xz * xz + yz * yz;
    }

    //Length
    private double length () {
        return Math.sqrt(lengthSquared());
    }

    //normalize
    private void normalize () {
        double len = length();
        a /= len;
        xy /= len;
        xz /= len;
        yz /= len;
    }
    // --------------------------------------------


    //outer product ---------------------------------
    public Bivector3 outer (Vector3f a, Vector3f b) {
        return new Bivector3(
                a.x * b.y - a.y * b.x,
                a.x * b.z - a.z * b.x,
                a.y * b.z - a.z * b.y
        );
    }
    // -----------------------------------------------

    //Bivector Class ------------------------------------
    public class Bivector3 {

        public float xy;
        public float xz;
        public float yz;

        public Bivector3 (float xy, float xz, float yz) {
            this.xy = xy;
            this.xz = xz;
            this.yz = yz;
        }
    }
    // --------------------------------------------------
}
