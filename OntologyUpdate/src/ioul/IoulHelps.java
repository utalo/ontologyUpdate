package ioul;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IoulHelps {
	/**
	 * reads in input and checks if Y/y or N/n	 * 
	 * @return Y or N
	 */
	public static char readchar() {
		BufferedReader in;
		String read;
		in = new BufferedReader(new InputStreamReader(System.in));
		try {
			read = in.readLine();
			if (read.equals("Y") | read.equals("y")) {
				return 'Y';
			} else {
				return 'N';
			}
		} catch (IOException e) {
			System.out.println("The number could not be parsed.");
			e.printStackTrace();
		}
		return 'N';
	}
	
	/**
	 * reads a number from console and checks if it is in the interval [start, end]
	 * if (stringAllowed = true) strings as input are also allowed	  
	 */
	public static String readNum(String question, int start, int end, boolean stringAllowed) throws IOException {

		BufferedReader in;
		String read = "";
		in = new BufferedReader(new InputStreamReader(System.in));
		boolean correctInput=false;

		int number = -1;
		while (correctInput == false){
			System.out.println(question);
			read = in.readLine();
			try {
				number = Integer.parseInt(read);
				if (number >=start && number <= end) {
					correctInput = true;
					return read;
				}
			} catch (NumberFormatException ex) {
				if (stringAllowed== true) {
					correctInput = true;
					read = "?"+read;
					return read;
				}
			}			
			System.out.println("Wrong input, try again!");			
		}
		return read;
	}
}
