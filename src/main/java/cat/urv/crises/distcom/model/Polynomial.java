package cat.urv.crises.distcom.model;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;


public class Polynomial implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public BigInteger[] coef;  // coefficients
    private int deg;     // degree of polynomial (0 for the zero polynomial)

    // a * x^b
    public Polynomial(BigInteger a, int b) {
        coef = new BigInteger[b+1];
        Arrays.fill(coef, BigInteger.ZERO);
        coef[b] = a;
        deg = degree();
    }

    // return the degree of this polynomial (0 for the zero polynomial)
    public int degree() {
        int d = 0;
        for (int i = 0; i < coef.length; i++)
            if (coef[i] != BigInteger.ZERO) d = i;
        return d;
    }

    // return c = a + b
    public Polynomial plus(Polynomial b) {
        Polynomial a = this;
        Polynomial c = new Polynomial(BigInteger.ZERO, Math.max(a.deg, b.deg));
        for (int i = 0; i <= a.deg; i++) 
        	c.coef[i] = c.coef[i].add(a.coef[i]);
        for (int i = 0; i <= b.deg; i++) 
        	c.coef[i] = c.coef[i].add(b.coef[i]);
        c.deg = c.degree();
        return c;
    }

    // return (a - b)
    public Polynomial minus(Polynomial b) {
        Polynomial a = this;
        Polynomial c = new Polynomial(BigInteger.ZERO, Math.max(a.deg, b.deg));
        for (int i = 0; i <= a.deg; i++) c.coef[i] = c.coef[i].add(a.coef[i]);
        for (int i = 0; i <= b.deg; i++) c.coef[i] = c.coef[i].subtract(b.coef[i]);
        c.deg = c.degree();
        return c;
    }

    // return (a * b)
    public Polynomial times(Polynomial b) {
        Polynomial a = this;
        Polynomial c = new Polynomial(BigInteger.ZERO, a.deg + b.deg);
        for (int i = 0; i <= a.deg; i++)
            for (int j = 0; j <= b.deg; j++)
                c.coef[i+j] = c.coef[i+j].add((a.coef[i].multiply(b.coef[j])));
        c.deg = c.degree();
        return c;
    }

    // return a(b(x))  - compute using Horner's method
    public Polynomial compose(Polynomial b) {
        Polynomial a = this;
        Polynomial c = new Polynomial(BigInteger.ZERO, 0);
        for (int i = a.deg; i >= 0; i--) {
            Polynomial term = new Polynomial(a.coef[i], 0);
            c = term.plus(b.times(c));
        }
        return c;
    }


    // do a and b represent the same polynomial?
    public boolean eq(Polynomial b) {
        Polynomial a = this;
        if (a.deg != b.deg) return false;
        for (int i = a.deg; i >= 0; i--)
            if (a.coef[i] != b.coef[i]) return false;
        return true;
    }


    // use Horner's method to compute and return the polynomial evaluated at x
    public BigInteger evaluate(BigInteger x) {
        BigInteger p = BigInteger.ZERO;
        for (int i = deg; i >= 0; i--)
            p = coef[i].add(x.multiply(p));
        return p;
    }

    // differentiate this polynomial and return it
    public Polynomial differentiate() {
        if (deg == 0) return new Polynomial(BigInteger.ZERO, 0);
        Polynomial deriv = new Polynomial(BigInteger.ZERO, deg - 1);
        deriv.deg = deg - 1;
        for (int i = 0; i < deg; i++)
            deriv.coef[i] = BigInteger.valueOf(i + 1).multiply(coef[i + 1]);
        return deriv;
    }
}