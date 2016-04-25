
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CheckSum
{

    public static void main(String[] args) 
    {
        InputStream input;
        OutputStream output;
        
        int size = 0;
        byte bytes[] = null;
        short sum = 0;
        
        try 
        {
            // socket
            Socket socket = new Socket("codebank.xyz", 38103);
            // streams
            input = socket.getInputStream();
            output = socket.getOutputStream();
            // read number of bytes going to be sent
            size = input.read();
            System.out.println("Number of bytes to be sent: " + size);
            // declare array to hold bytes
            bytes = new byte[size];
            // read the size number of bytes
            input.read(bytes);
            System.out.print("Bytes recieved: ");
            // display all bytes
            for(int i = 0; i < size; i++)
                System.out.printf("%02X", bytes[i]);
            System.out.println();
            // get checksum
            sum = checksum(bytes);            
            // we got a short (16 bits) but we need 2 bytes so
            // shift to the right, cast it as a byte to only get the far right 8 bits
            // since  abyte is 8 bits, put it into something,
            // next byte we need the far 8 bits in the short, so we shift over by 8
            // cast to byte to get oly thsoe bytes, put it into something
            // send it over
            // declare array  to hold these results
            byte[] new_bytes = new byte[2];
            // used for shifting purposes
            byte[] shifts = {8,0}; 
            // get bytes to send back to server
            for(int i = 0; i < shifts.length; i++)
            {
                // shift first byte and place it into array
                new_bytes[i] = (byte) shiftByte2Right(sum, shifts[i]);
            }
            
            output.write(new_bytes);
            //System.out.println("\nResponse: " + (input.read() == 1 ? "good!" : "not good!"));
		System.out.println("\nResponse: " + input.read());
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(Ex3Client.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    public static short checksum(byte[] b)
    {
        // byte to int, BAD!!!, sign extension (if byte to int, fills extra ints, by the leftmost bit, so if it was 1, 24 bits are 1
        int sum = 0;
        int counter = 0;
        int temp = 0;
        
        while(counter != b.length)
        {
           if(((counter + 1) % 2 == 0) && (counter != 0))
           {
               // shift first byte by 4
               temp = shiftByte2Left(b[counter - 1], 8);
               // xor with previous byte to combine
               temp = xorBytes(b[counter], temp);
               sum += temp;
               // if a carry happened meaning, FFFF0000 is & with sum
               // all the 16 bits on the left side of sum is zeroed out since
               // & 0000 will give u zero, as for the FFFF side, same concept, 
               // if any bit after the 16 bits , means it is too big for that vairable
               // since we are simulating a short (16bits) if any bit where theFFFF is anded means
               // its greater then a short, so we check this by anding it by FFFF so that means if any
               // bit is  in that range, the value after the and is some number that is NOT zero,
               // so if it == 0, no overflow, any other number is a overflow
               if((sum & 0xFFFF0000) != 0)
               {
                   sum &= 0xFFFF;
                   sum++;
               }
           }
           // increase counter
           counter++;
        }
        // aray is odd           
        // if bytes is odd size, we have to deal with last byte
        // since the byets are in form of byte1byte2, but we only have one byte
        // it wont be 0000byte1 it will be byte10000 (we shift over the last byte by 8
        if((b.length % 2) != 0)
        {
            // shift first byte by 8
            temp = shiftByte2Left( b[b.length - 1], 8);
            // add to sum
            sum += temp;
            // check and handleoverflow
            if((sum & 0xFFFF0000) != 0)
               {
                   sum &= 0xFFFF;
                   sum++;
               }
        }
        // return checksum
        return (short)(~(sum & 0xFFFF));
    }
    
    static int xorBytes(int first, int second)
    {
        // xor both bytes
        // these operations convert them to ints
        // when converting, bytes -> int, if the last bit in your number is 1
        // all numbers that are being filled for that int is 1, 
        // you need to set a mask , for example 0xff will be a mask for 1 byte where each 
        // ff is 4 bits this will flip the bits back to 0
        // so in this case, first is shift, so it will have 24 bits of (possible) 
        // being 1's, then the 4 bitsm, then 4 0's sinze it was shifted
        // how ever, second is not shifted, so the 28 bits to the left may have turned onto
        // 1's which will mess up the xoring, so to fix first, we & it with a mask, where 
        // the mask has a F for the bits we WANT to keep, so since first has 24 bits as 1's,
        // the 4 bits we need and the 4 0's, we can use 0xF0 which means,
        // mask the 4 bits (ones we need), but dont and the other 4 after so for example
        // 111111111111111111111111 1010 0000 -> all the 24 1's will become 0, the 1010 
        // will stay the same, and the 0000 will stay the same thats why we use 0xF0
        // for second,  same concept except use 0xF (same as 0x0F) since the 4 bits we need
        // are to the far right, the rest are 1's so and them
        
        //comments above is used for 4 bits, but i needed 8 bits
        return ((first & 0xFF) ^ (second & 0xFF00));
    }
    // shitfs buts to left 
    static int shiftByte2Left(int original_byte, final int shift_size)
    {
        // shift byte to the left
        return ((original_byte & 0xFF) << shift_size);
    }
    
    static int shiftByte2Right(int original_byte, final int shift_size)
    {
        // shift byte to the left
        return (original_byte >> shift_size);
    }
    
}
