import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * CRC calculating and verification
 * @author Andrew Gomes (NID: an289864)
 * @date 6/29/2017
 * [CIS 3360 | Summer 2017 | Professor: Joshua Lazar]
 */
public class CRC {
	
	/**
	 * A list of raw text lines that are 64 characters each.
	 */
	private static ArrayList<String> formattedLines = new ArrayList<>();
	
	/**
	 * The entry point.
	 * @param args - the arguments passed in.
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		if (args.length < 2) {
			System.out.println("Not enough arguments entered!\nFirst argument must be c or v to indicate mode.\nSecond argument should be the file's name.");
			return;
		}
		
		String mode = args[0].toLowerCase();
		switch (mode) {
		
		//Calculate CRC
		case "c":
			File f = new File("./" + args[1]);
			
			if (f == null || !f.exists()) {
				System.out.println("Input file doesn't exist!");
				System.exit(1);
				return;
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(f));
			//Set up a list of the lines that were read-in
			ArrayList<String> rawLines = new ArrayList<>();
			for (Object o : reader.lines().toArray()) {
				String line = (String) o;
				rawLines.add(line);
			}
			//Prints the raw file, 64 characters to a line
			//Also sets up the list of formatted 64 char lines that we will use.
			System.out.println("Raw input from file (64 chars per line):\n");
			for (String s : rawLines) {
				print64(s);
			}
			//Adds padding of "." to the 64 char lines.
			pad();
			//Perform the CRC calc on the formatted lines, which are now padded
			System.out.println("\nCRC 16 calculation progress:\n");
			calculate();
			break;
		//Verify CRC
		case "v":
			
			
			break;
		default:
			System.out.println("You must enter either c or v to indicate the mode of operation.");
			System.exit(1);
			break;
		}
	}
	
	/**
	 * XOR's the array of character bytes.
	 * @param byteArray - the input array
	 * 
	 * Polynomial:
	 * x16+x10+x8+x7+x3+1
	 * 1000 0010 1101 1100 01 -> 0x10589 hex
	 * 
	 * @return the result
	 */
	private static int xor(byte[] byteArray) {
		int crc = 0;
        for (byte b : byteArray) {
            for (int i = 0x80; i != 0; i >>= 1) {
                if ((crc & 32768) != 0) {
                    crc = (crc << 1) ^ 0x10589;
                } else {
                    crc = crc << 1;
                } if ((b & i) != 0) {
                    crc ^= 0x10589;
                }
            }
        }
        crc &= 0xFFFF;
		return crc;
	}
	
	/**
	 * Calculates the CRC with the formatted lines
	 * The last line is formatted differently from the rest.
	 */
	private static void calculate() {
		String last = "";
		int totalXor = 0;
		for (int i = 0; i < formattedLines.size(); i++) { //
			String s = formattedLines.get(i);
			
			int xor = xor(s.getBytes()) + totalXor;
			totalXor += xor;
			
			//Last line has last 8 characters removed to make room for the 8 digit hex.
			System.out.println((i == formattedLines.size() - 1) ? (s + "0000" + (Integer.toHexString(xor)) + " - 0000" + (Integer.toHexString(xor))) : (s + " - 0000" + (Integer.toHexString(xor))));
			if (i == formattedLines.size() - 1) {
				last = "0000" + (Integer.toHexString(xor));
			}
		}
		
		System.out.println("\nCRC16 result : " + last);
	}
	
	/**
	 * Prints the string, 64 characters to a line.
	 * @param str the string to print.
	 */
	private static void print64(String str) {
		int end = 64;
		for (int i = 0; i < (str.length() / 64) + 1; i++) {
			int start = end - 64;
			if (end > str.length()) {
				end = str.length();
			}
			if (str.length() < 64) {
				start = 0;
			}
			String sub = str.substring(start, end);
			System.out.println(sub);
			formattedLines.add(sub);
			end += 64;
		}
	}
	
	/**
	 * Pads the formatted 64 character lines to add a period.
	 */
	private static void pad() {
		int totalChars = 0;
		for (String s : formattedLines) {
			totalChars += s.length();
		}
		boolean modified = false;
		while (totalChars < 504) {
			//Pad existing lines with periods
			for (int i = 0; i < formattedLines.size(); i++) {
				String s = formattedLines.get(i);
				while (s.length() < 64) {
					s += ".";
					totalChars++;
					modified = true;
				}
				if (modified) {
					formattedLines.set(i, s);
				}
			}
			//Create entirely new lines of periods if the input is still < 504
			String newLine = "";
			for (int i = 0; i < 64; i++) {
				newLine += ".";
			}
			formattedLines.add(newLine);
			totalChars += 64;
		}
		//Removes the last 8 chars of the last line to allow for the ending hex code
		String last = formattedLines.get(formattedLines.size() - 1);
		last = last.substring(0, last.length() - 8);
		formattedLines.set(formattedLines.size() - 1, last);
	}
}