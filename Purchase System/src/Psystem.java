/*
Keys used in Psystem Class:

	Public Key Alice: Pua.key	
	Public Key Tom: Put.key	
	Public Key Purchasing order: Pup.key
	
	Private Key Purchasing order: Prp.key	
*/

import java.io.*; 
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import javax.crypto.Cipher; 

public class Psystem 
{
	public static void main(String argv[]) throws Exception
	{
		  ServerSocket listen = new ServerSocket(6783);		
		  Socket conn = listen.accept();		  
		 
		  PrintWriter pw = new PrintWriter(conn.getOutputStream(), true);
		  BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
		  DataInputStream in1 = new DataInputStream(conn.getInputStream());
			 
		  Socket sbank = new Socket("localhost", 6787); 
		  BufferedReader bin = new BufferedReader(new InputStreamReader(sbank.getInputStream())); 
		  DataOutputStream bpw1 = new DataOutputStream(sbank.getOutputStream());

		  /* Gets the ID and password user enters and checks it against the users and passwords in 'Password' text file. If the user is 
		  valid shows the items ,quantity to the customer. This is saved in items.txt file*/
		  String ClientID = in.readLine();
		  String sPassword = in.readLine();
		  
		  BufferedReader buff = null;
		  try
		  {
			  buff = new BufferedReader(new FileReader("Password"));
			  String currentline;
			  
			  while(true) 
			  {
  				  currentline = buff.readLine();
  				  if(currentline == null)
  				  {
  					  buff.close();
  					  buff = new BufferedReader(new FileReader("Password"));
  				  continue;
  			    }
  			  
    			  String idpass[] = currentline.split(" ");
    			  if(idpass[0].equals(ClientID))
    			  {
    				  if(idpass[1].equals(sPassword))
    				  {
    					  pw.println("OK");
    					  System.out.println("Password Matched");
    					  break;
    				  }
    				  else
    				  {
    					  pw.println("Error");
    					  System.out.println("Wrong Password!!");
    					  sPassword = in.readLine();
    					  continue;
    				  }
    			  }
    			  else
    			  {
    				  continue;
    			  }
					 
			  }
			buff.close(); 
			  
			buff = new BufferedReader(new FileReader("item"));
			while(true)
			{
				currentline = buff.readLine();
				if(currentline == null)
				{
					pw.println("EXIT");
					break;
				}
				pw.println(currentline);
				 
			}
			buff.close();
			  
			int length_cipheritem = in1.readInt();
			byte[] cipher_itm =new byte[length_cipheritem];
			in1.readFully(cipher_itm);
			  
			int length_cipherquantity = in1.readInt();
			byte[] cipher_qnty =new byte[length_cipherquantity];
			in1.readFully(cipher_qnty);
			  
			int length_dig_sig = in1.readInt();
			byte[] cipher_dg =new byte[length_dig_sig];
			in1.readFully(cipher_dg);
			  
			int length_signature = in1.readInt();
			byte[] cipher_sig =new byte[length_signature];
			in1.readFully(cipher_sig);
			  
			 /* verifying the digital signature of users by their public keys*/
			Signature sign = Signature.getInstance("SHA1withRSA");
			ObjectInputStream dig = null;
			if(ClientID.equals("alice"))
				dig = new ObjectInputStream(new FileInputStream("Pua.key"));
			else if(ClientID.equals("tom"))
				dig = new ObjectInputStream(new FileInputStream("Put.key"));
			final PublicKey pbkey = (PublicKey) dig.readObject();
			sign.initVerify(pbkey);
			sign.update(cipher_dg);
			if(sign.verify(cipher_sig))
		    {
		    	System.out.println("Digital Signature Verified...!!! ");
		    }
		  
		  int length_cipherid = in1.readInt();
		  byte[] cipher_id =new byte[length_cipherid];
		  in1.readFully(cipher_id);
		  
		  int length_ciphercredit = in1.readInt();
		  byte[] cipher_credit =new byte[length_ciphercredit];
		  in1.readFully(cipher_credit);
		
		  
		  /*Decrpting item and quantity using private key of purchasing system*/
		  ObjectInputStream outputitem = null;
		  outputitem = new ObjectInputStream(new FileInputStream("Prp.key"));
	      final PrivateKey privateKeypsystm = (PrivateKey) outputitem.readObject();
	   
	      final String decry_item = decrypt_item(cipher_itm, privateKeypsystm);
	      final String decry_qnty = decrypt_item(cipher_qnty, privateKeypsystm);
	      outputitem.close();
	      
	      /*System.out.println("Encrypted item: " +cipher_itm.toString());
		  System.out.println("Encrypted quantity: " +cipher_qnty.toString());
		  System.out.println("Decrypted item: " + decry_item);
		  System.out.println("Decrypted qnty " + decry_qnty);*/
		  
		  
		  buff = new BufferedReader(new FileReader("item"));
		  int tot_Price;
		  StringBuilder sb1 = new StringBuilder();
		  String part="";
		  String replace_part="";
		  int update;
		  while(true) 
		  {
			  currentline = buff.readLine();
			  if(currentline == null)
			  {
				  buff.close();
				  buff = new BufferedReader(new FileReader("item"));
				  continue;
			  }
		  
			  String idpass[] = currentline.split(",");
			  
			  if(idpass[0].equals(decry_item))
			  {
				  String price = " ";
				  if(idpass[2].startsWith("$")) 	//getting price 
					  {
						  price = idpass[2].substring(1);
						  System.out.println(price);
					  }
					 
					  tot_Price = Integer.parseInt(price) * Integer.parseInt(decry_qnty); //Calucating total price by multiplying quantity
					  System.out.println(tot_Price);
					  					  
	    	          StringBuilder sb = new StringBuilder();
	    	          sb.append(idpass[0]);
	    	          sb.append(',');
	    	          sb.append(idpass[1]);
	    	          sb.append(',');
	    	          sb.append(idpass[2]);
	    	          sb.append(',');
	    	          sb.append(idpass[3]);
	    	          part = sb.toString();
	    	          
	    	         
	    	          sb1.append(idpass[0]);
	    	          sb1.append(',');
	    	          sb1.append(idpass[1]);
	    	          sb1.append(',');
	    	          sb1.append(idpass[2]);
	    	          sb1.append(',');
	    	         // replace_part = sb1.toString();
	    	          
	    	          update = Integer.parseInt(idpass[3]) - Integer.parseInt(decry_qnty);				//updating quantity
					 		  
					  break;
			  }
		  }
		  
		  
			  ObjectInputStream outprice = null;
			  outprice = new ObjectInputStream(new FileInputStream("Prp.key"));
		      final PrivateKey privateKeyps = (PrivateKey) outprice.readObject();
		      final byte[] cipherprice = encrypt_price(String.valueOf(tot_Price), privateKeyps);
		      outprice.close();
		      
			  /*sending creditcard and total price details to bank*/
		      bpw1.writeInt(cipher_id.length);
		      bpw1.write(cipher_id);
		      bpw1.writeInt(cipher_credit.length);
		      bpw1.write(cipher_credit);
		      bpw1.writeInt(cipherprice.length);
		      bpw1.write(cipherprice);
		      
		      String choice_bank = bin.readLine();
			  
		      if(choice_bank.matches("OK"))
		      {
		    	  pw.println("OK");
		    	  sb1.append(String.valueOf(update));
		    	  replace_part = sb1.toString();
		    	  
		    	  BufferedReader reader = new BufferedReader(new FileReader ("item"));
    		      String         line = null;
    		      StringBuilder  stringBuilder = new StringBuilder();
    		      String         ls = System.getProperty("line.separator");
    	    
    	          while((line = reader.readLine()) != null)
    	          {
    	              stringBuilder.append(line);
    	              stringBuilder.append(ls);
    	          }
    	          String file = stringBuilder.toString();
    	          
    	          if(file.contains(part))
    	          {
    	        	  file = file.replace(part, replace_part);
    	          }
    	          
    	          BufferedWriter writer = new BufferedWriter(new FileWriter("item"));
    	          writer.write(file);
    	          writer.close();
    	          reader.close();
   	  
		      }
		      else
		      {
		    	  pw.println("Error");
		      }
			  
		  } 
		  catch (IOException e) 
		  {
			  e.printStackTrace();
		  }
		   
	}
	
	/*Decrypt items using private key of purchasing sys*/
	public static String decrypt_item(byte[] text, PrivateKey key) 
	{
			byte[] dectyptedText = null;
			try 
			{
				  final Cipher cipher = Cipher.getInstance("RSA");
				  cipher.init(Cipher.DECRYPT_MODE, key);
				  dectyptedText = cipher.doFinal(text);	
			} 
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			
			return new String(dectyptedText);
	 }
	
	/*Encrption using private key of purchasing system*/
	public static byte[] encrypt_price(String text, PrivateKey key) 
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