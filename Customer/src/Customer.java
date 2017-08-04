/*

Used 2 customers alice and tom. Their public and private keys are provided. 
Keys used in Customer Class:
	Public Key Alice: Pua.key
	Private Key Alice: Pra.key
	
	Public Key Tom: Put.key
	Private Key Tom: Prt.key
	
	Public Key Bank: Pub.key
	Public Key Purchasing order: Pup.key
*/


import java.security.*;
import java.util.*;
import javax.crypto.Cipher;
import java.net.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class Customer 
{
	public static void main(String args[]) throws NoSuchAlgorithmException,IOException,ClassNotFoundException,InvalidKeyException,SignatureException
	{
		Socket sock = new Socket("localhost", 6783); 
		Scanner scan = new Scanner(System.in);
			
		PrintWriter pw = new PrintWriter(sock.getOutputStream(), true);	
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream())); 	
		DataOutputStream pw1 = new DataOutputStream(sock.getOutputStream());
		
		
		
		System.out.println("\n Welcome Client");
		System.out.println("\n Please Enter your ID : ");
		String ClientID = scan.nextLine();
		
		System.out.println("\n Please Enter your Password : ");
		String Passwrd = scan.nextLine();
		String sPassword = encrypt(Passwrd);
		
		/* All the ids and passwords are stored in text file called password(for now) on purchasing sys side */	
		pw.println(ClientID);
		pw.println(sPassword);		
		while(true)
		{
			/* Validation for Id Password*/
			String choice = in.readLine();
			if(choice.matches("OK"))
			{
				break;
			}
			else if(choice.matches("Error"))
			{
				System.out.println("\n The Password is Incorrect!! \n Please Enter your Password again: ");
				Passwrd = scan.nextLine();
			    sPassword = encrypt(Passwrd);
			    pw.println(sPassword);
			}
		}
		
		/* if login is successfull Items are displayed from purchasing system. All Items are stored in item text file on Puschasing system side*/
		String current;
		while((current = in.readLine()) != null)
		{
			if(current.equals("EXIT"))
				break;
			System.out.println(current);
			
		}
		
		
		/* Choice is asked to a customer*/
		System.out.println("Please enter the item number : ");
		String item_no = scan.nextLine();
		
		System.out.println("Please enter the quantity : ");
		String quantity = scan.nextLine();
		
		System.out.println("Please enter the Credit card number : ");
		String credit_card = scan.nextLine();
		
		
		/* Item number and quantity are encrypted using Public key of Purchasing system tht is Pup.key and digital signature is attached of 
		the particular user for authentication and authorization purpose using private keys of customers accordingly. the algorithm here used is SHA1withRSA
		*/
		ObjectInputStream inputStream = null;		
		inputStream = new ObjectInputStream(new FileInputStream("Pup.key"));
		final PublicKey publicKey = (PublicKey) inputStream.readObject();
		final byte[] cipheritem = encrypt_item(item_no , publicKey);
		    
		final byte[] cipherquantity = encrypt_item(quantity, publicKey);
		inputStream.close();  
		
		pw1.writeInt(cipheritem.length);
		pw1.write(cipheritem);
		pw1.writeInt(cipherquantity.length);
		pw1.write(cipherquantity);
		
		byte[] dig_msg = new byte[cipheritem.length + cipherquantity.length];
		System.arraycopy(cipheritem, 0, dig_msg, 0, cipheritem.length);
		System.arraycopy(cipherquantity, 0, dig_msg, cipheritem.length, cipherquantity.length);
		
		Signature sign = Signature.getInstance("SHA1withRSA");
		ObjectInputStream dig = null;
		if(ClientID.equals("alice"))
			dig = new ObjectInputStream(new FileInputStream("Pra.key"));
		else if(ClientID.equals("tom"))
			dig = new ObjectInputStream(new FileInputStream("Prt.key"));
		final PrivateKey prkey = (PrivateKey) dig.readObject();
		sign.initSign(prkey);
	    sign.update(dig_msg);
	    byte[] signature =  sign.sign();
	    
	    pw1.writeInt(dig_msg.length);
	    pw1.write(dig_msg);
	    
	    pw1.writeInt(signature.length);
	    pw1.write(signature);
	    
		/* 
		The credit card is encrpted using public key of BANK and NOT purchasing system. 
		*/
		ObjectInputStream inputbank = null;
		inputbank = new ObjectInputStream(new FileInputStream("Pub.key"));
		final PublicKey publicKeybank = (PublicKey) inputbank.readObject();
		final byte[] cipherid = encrypt_item(ClientID , publicKeybank);
		final byte[] ciphercredit = encrypt_item(credit_card, publicKeybank);
		inputbank.close();
		   
		pw1.writeInt(cipherid.length);
		pw1.write(cipherid);
		pw1.writeInt(ciphercredit.length);
		pw1.write(ciphercredit);
		
		String choice1 = in.readLine();
		if(choice1.matches("OK"))
		{
			System.out.println("We will process your Order soon !! ");
		}
		else
		{
			System.out.println("Wrong Credit Card number!!");
		}
		
		scan.close();
		sock.close();			      
	}
	
	
	/*This Function is used to encrypt just the password*/
	public static String encrypt(String x) throws NoSuchAlgorithmException
	{
		java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
	    byte[] array = md.digest(x.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < array.length; ++i) 
	    {
	      sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
	    }
	    return sb.toString();		  
	}
	
	/*This Function is used to encrypt other entities like item number, quantity and creditcard info. */
	 public static byte[] encrypt_item(String text, PublicKey key) 
	 {
		byte[] cipherText = null;
		try 
		{
		  final Cipher cipher = Cipher.getInstance("RSA");
		  cipher.init(Cipher.ENCRYPT_MODE, key);
		  cipherText = cipher.doFinal(text.getBytes());
		} 
		catch (Exception e) 
		{
		  e.printStackTrace();
		}
		return cipherText;
	  }

}