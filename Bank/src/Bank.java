/*

Keys used in Bank Class:
	Public Key Bank: Pub.key
	Private Key Bank: Prb.key	
	Public Key Purchasing order: Pup.key
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class Bank 
{
	public static void main(String argv[]) throws Exception,IOException
	{
  		  ServerSocket listen = new ServerSocket(6787);		
  		  Socket psconn = listen.accept();		  
  		  
  		  PrintWriter pspw = new PrintWriter(psconn.getOutputStream(), true); 
  		  DataInputStream psin1 = new DataInputStream(psconn.getInputStream());		
  		  
		  /*Accepting creditcard and price from psystem.*/  
  		  int length_cipherid = psin1.readInt();
  		  byte[] cipher_id =new byte[length_cipherid];
  		  psin1.readFully(cipher_id);
  		  
  		  int length_ciphercredit = psin1.readInt();
  		  byte[] cipher_credit =new byte[length_ciphercredit];
  		  psin1.readFully(cipher_credit);
  		  
  		  int length_cipherprice = psin1.readInt();
  		  byte[] cipher_tot_price =new byte[length_cipherprice];
  		  psin1.readFully(cipher_tot_price);
  		  
  		  ObjectInputStream outputitem = null;
  		  outputitem = new ObjectInputStream(new FileInputStream("Prb.key"));
  		  
	      final PrivateKey privateKeypsystm = (PrivateKey) outputitem.readObject();
	      final String decry_id = decrypt_item(cipher_id, privateKeypsystm);
	      final String decry_credit = decrypt_item(cipher_credit, privateKeypsystm);
	      outputitem.close();
	      
	      /*System.out.println("Encrypted id: " +cipher_id.toString());
	      System.out.println("Encrypted creditcard: " +cipher_credit.toString());
	      System.out.println("Decrypted id " + decry_id);
	      System.out.println("Decrypted creditcard " + decry_credit);*/
	      
	      BufferedReader buffer = null;
	      buffer = new BufferedReader(new FileReader("balance"));
	      
	      String readln = "";
	      while((readln = buffer.readLine()) != null)
	      {
	    	  String check[] = readln.split(",");
	    	  if(check[0].equalsIgnoreCase(decry_id))
	    	  {
	    		  if(check[1].equals(decry_credit))
	    		  {
	    			  pspw.println("OK");	    			  
	    			  ObjectInputStream outputprice = null;
	    			  outputprice = new ObjectInputStream(new FileInputStream("Pup.key"));
	    			  final PublicKey publickeypsystm = (PublicKey) outputprice.readObject();			 
	    		      final String decry_price = decrypt_price(cipher_tot_price, publickeypsystm);	    		      
	    		      
	    		      int new_balance = Integer.parseInt(check[2]) - Integer.parseInt(decry_price); //Updating balance
	    		      System.out.println("Balance : "+new_balance);
	    			    		      	    		      
	    		      BufferedReader reader = new BufferedReader(new FileReader ("balance"));
	    		      String         line = null;
	    		      StringBuilder  stringBuilder = new StringBuilder();
	    		      String         ls = System.getProperty("line.separator");
	    	    
	    	          while((line = reader.readLine()) != null)
	    	          {
	    	              stringBuilder.append(line);
	    	              stringBuilder.append(ls);
	    	          }
	    	          String file = stringBuilder.toString();
	    	          	    	      
	    	          StringBuilder sb = new StringBuilder();
	    	          sb.append(check[0]);
	    	          sb.append(',');
	    	          sb.append(check[1]);
	    	          sb.append(',');
	    	          sb.append(check[2]);
	    	          String part = sb.toString();
	    	          
	    	          StringBuilder sb1 = new StringBuilder();
	    	          sb1.append(check[0]);
	    	          sb1.append(',');
	    	          sb1.append(check[1]);
	    	          sb1.append(',');
	    	          sb1.append(String.valueOf(new_balance));
	    	          String replace_part = sb1.toString();
	    	          
	    	          if(file.contains(part))
	    	          {
	    	        	  file = file.replace(part, replace_part);
	    	          }
	    	          
	    	          BufferedWriter writer = new BufferedWriter(new FileWriter("balance"));
	    	          writer.write(file);
	    	          writer.close();
	    	          reader.close();	    		      
	    		     
	    		      outputprice.close();
	    		  }
	    		  else
	    		  {
	    			  pspw.println("Error");
	    		  }
	    		  
	    	  }
	    	 
	      }	
	      buffer.close();          
         listen.close();	     
	}	  
	/*used to decrpt creditcard and id using banks private key*/
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
	 
	/*Used to Decrpyt price using purchasing system public key*/ 
	public static String decrypt_price(byte[] text, PublicKey key) 
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
	 
}