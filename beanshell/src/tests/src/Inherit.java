/*
 * Some cases where inheritance is supposed to work and some where it doesn't.
 * This source corresponds with the `inherit.bsh' test script.
 */

import InheritanceTest.*;

public class Inherit {

	public static void main(String[] args) {
		A wa;
		B wb;
		A xa;
		B xb;
		X x;
		A ya;
		B yb;
		C yc;
		Y y;
		A za;
		B zb;
		C zc;

		wa = Accessor.getWbyA();
		wb = Accessor.getWbyB();

		xa = Accessor.getXbyA();
		xb = Accessor.getXbyB();
		x  = Accessor.getX();

		ya = Accessor.getYbyA();
		yb = Accessor.getYbyB();
		yc = Accessor.getYbyC();
		y  = Accessor.getY();

		za = Accessor.getZbyA();
		zb = Accessor.getZbyB();
		zc = Accessor.getZbyC();

		wa.a();  
		
		wb.a();
		wb.b(); 

		// Can't access W (package scope), doesn't work with Reflection either.
		//((W)wa).w();
		
		xa.a();
		
		xb.a();
		xb.b();

		x.a();
		x.b();
		x.x();
		
		ya.a();

		yb.a();
		yb.b();

		yc.a();
		yc.c();

		y.a();
		y.b();
		y.c();
		y.w();  // Won't work with reflection, but works here.
		y.x();
		y.y();

		za.a();

		zb.a();
		zb.b();

		zc.a();
		zc.c();

		// We can do this with reflection.  Won't compile in Java, though.
		//((Z)za).x();

		System.out.println("OK");
	}
}
