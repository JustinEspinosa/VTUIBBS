package test;

import vtui.bbs.util.domain.ApplePasswordService;


public class Draft {
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String username="test";
		String password="1234";

		ApplePasswordService sasl = new ApplePasswordService("scrooge.duck.home",3659);

			if(sasl.connect()){
				
				if(sasl.beginCASTEncryption()){
					String uid = sasl.getIdByName(username);
					if(uid!= null && sasl.authDHX(uid,password))
						if( sasl.changepass(uid, "1234") )
							System.out.println("Success!");
				}
			}
	}

}
