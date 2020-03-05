package ca.powerj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

class LCrypto {
	private boolean success = true;
	private String fileName = "";
	private PBEParameterSpec pbeParamSpec = null;
	private SecretKey pbeKey = null;
	private Cipher pbeCipher = null;

	LCrypto(String path) {
		fileName = path + "bin" + System.getProperty("file.separator") + "powerj.bin";
	}

	private byte[] decrypt(byte[] ciphertext) {
		byte[] cleartext = null;
		try {
			pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
			cleartext = pbeCipher.doFinal(ciphertext);
		} catch (InvalidKeyException e) {
			success = false;
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			success = false;
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			success = false;
			e.printStackTrace();
		} catch (BadPaddingException e) {
			success = false;
			e.printStackTrace();
		}
		return cleartext;
	}

	private byte[] encrypt(byte[] cleartext) {
		// Initialize PBE Cipher with key and parameters
		byte[] ciphertext = null;
		try {
			pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);
			// Encrypt the cleartext
			ciphertext = pbeCipher.doFinal(cleartext);
		} catch (InvalidKeyException e) {
			success = false;
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			success = false;
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			success = false;
			e.printStackTrace();
		} catch (BadPaddingException e) {
			success = false;
			e.printStackTrace();
		}
		return ciphertext;
	}

	String[] getData() {
		init();
		String[] data = null;
		byte[] ciphertext = read();
		if (ciphertext != null) {
			byte[] cleartext = decrypt(ciphertext);
			String strText = new String(cleartext);
			data = strText.split("\t");
		}
		return data;
	}

	private void init() {
		final int count = 1000;
		final byte[] salt = { (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c, (byte) 0x7e, (byte) 0xc8, (byte) 0xee,
				(byte) 0x99 };
		final String key = "PBEWithMD5AndDES";
		try {
			pbeParamSpec = new PBEParameterSpec(salt, count);
			SecretKeyFactory keyFac = SecretKeyFactory.getInstance(key);
			pbeCipher = Cipher.getInstance(key);
			char[] chr = key.toCharArray();
			PBEKeySpec pbeKeySpec = new PBEKeySpec(chr);
			pbeKey = keyFac.generateSecret(pbeKeySpec);
		} catch (NoSuchAlgorithmException e) {
			success = false;
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			success = false;
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			success = false;
			e.printStackTrace();
		}
	}

	private byte[] read() {
		File file = null;
		FileInputStream inputStream = null;
		byte[] ciphertext = null;
		try {
			file = new File(fileName);
			if (file.exists()) {
				inputStream = new FileInputStream(fileName);
				ciphertext = new byte[inputStream.available()];
				inputStream.read(ciphertext);
				inputStream.close();
			}
		} catch (FileNotFoundException e) {
			success = false;
			e.printStackTrace();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
		return ciphertext;
	}

	private void save(byte[] ciphertext) {
		File file = null;
		FileOutputStream fos = null;
		try {
			file = new File(fileName);
			if (!file.exists())
				file.createNewFile();
			if (file.exists()) {
				fos = new FileOutputStream(file);
				fos.write(ciphertext);
			}
			fos.close();
		} catch (IOException e) {
			success = false;
			e.printStackTrace();
		}
	}

	boolean setData(String[] data) {
		String text = data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] + "\t" + data[4] + "\t" + data[5];
		init();
		byte[] cleartext = text.getBytes();
		byte[] ciphertext = encrypt(cleartext);
		save(ciphertext);
		return success;
	}
}