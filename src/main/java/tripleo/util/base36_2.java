///*
// * https://github.com/kashyaprpuranik/AlgoCode/blob/master/spoj/base36.java
// *
// * Author       : Kashyap R Puranik
// * email        : kashyap.r.puranik (at) gmail (dot) com
// * copyright    : 2008 - 2009
// * date         : 23 - 08 -09
// *
// * file name    : .cpp
// * version      : 1.0.1
// */
//package tripleo.util;
//
//import java.util.Scanner;
//import java.io.*;
//import java.math.BigInteger;
//import java.util.Stack;
//
//public class base36_2 {
//
//	// This function converts a number from base 10 to the input base
//	// String is the output string and base is the base of the number
//	// num is the input number
//	String baseConvert( BigInteger num, BigInteger base)
//	{
//		BigInteger x;                        // A temporary variable
//		Stack lifo = new Stack();                         // A stack for LIFO storage
//		String answer="";                          // The required string
//
//		while ( num != java.math.BigInteger.ZERO )
//		{
//			x = num.mod(base);
//			num = num.divide(base);
//			if (x.compareTo(BigInteger.valueOf(9)) <= 0)
//				lifo.push(x.add(BigInteger.valueOf('0')));
//			else
//				lifo.push(x.add(BigInteger.valueOf('A'-10)));
//		}
//		while(!lifo.empty()){answer.concat(""+lifo.pop());}
//
//		return answer;
//	}
//
//	// This function converts a number of given base to base 10 and returns it
//	// String num is the input string and base is the base of the number
//	BigInteger baseUnconvert(String num, BigInteger base)
//	{
//		BigInteger result = 0;                   	// The required answer
//		BigInteger pow = 1;                      	// pow = base ^ i
//		BigInteger temp;                                   // temporary number
//		BigInteger len = num.size();                       // length of input string
//		int i;                                      // for loop counter
//
//		for ( i = len - 1 ; i >= 0 ; i -- )
//		{
//			temp = (num[i]<='9')?(num[i]-'0'):(num[i]-'A'+10);
//			result = result.add(temp.multiply(pow));
//			pow =pow.multiply(base);
//		}
//		return (result);
//	}
//
//	// Control flow starts here
//	public static void main(String[] args) {
//
//		int num, i;
//		Scanner in1 = new Scanner(System.in);
//
//		num=in1.nextInt();						// Get the number of test cases
//
//		for ( i = 0 ; i < num ; i ++ )
//		{
//			try
//			{
//				BufferedReader in2 = new BufferedReader(new InputStreamReader(System.in));
//				String num1 = in2.readLine();
//				String num2 = in2.readLine();
//				String sum = convert(( baseUnconvert(num1, 36).add(baseUnconvert(num2, 36))), 36);
//				System.out.println(sum);
//			}
//			catch(IOException e)
//			{
//				System.out.println("IOException has been caught");
//			}
//		}
//		in1.close();
//		// Prints name and age to the console
//	}
//}