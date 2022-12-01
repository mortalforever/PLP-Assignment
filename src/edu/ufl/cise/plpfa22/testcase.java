package edu.ufl.cise.plpfa22;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class testcase implements Runnable{
	int a;
	int b;
	public testcase() {
		super();
	}
	class p implements Runnable {
		
		@Override
		public void run () {
			new q().run();
		}
		

	}
	
	class q implements Runnable {
		@Override
		public void run () {
			System.out.println(1);
		}
	}
	public static void main(String[] args) {
		new testcase().run();
	}
	
	@Override
	public void run() {
		new p().run();
	}

}
