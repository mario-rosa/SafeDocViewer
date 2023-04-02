package it.unibas.tesi.mobilereader.controllo;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import it.unibas.tesi.mobilereader.modello.Modello;

public final class Encrypt {

    public static String decrypt(String encrypted) {
        String aesKey = (String) Modello.getBean("securityKey");
        String initVector = (String) Modello.getBean("initVector");
        try {
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(initVector));
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(aesKey), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decryptMessage(String message, String serverPublicKeyPEM){
        String clientPrivateKeyPEM = (String)Modello.getBean("clientPrivateKeyPEM");

        try {
            byte[] encryptedMessageBytes = Base64.getDecoder().decode(message);
            PrivateKey privateKey = readPrivateKeyFromPem(clientPrivateKeyPEM);
            PublicKey publicKey = readPublicKeyFromPem(serverPublicKeyPEM);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedWithPrivateKey = cipher.doFinal(encryptedMessageBytes);

            Cipher cipher2 = Cipher.getInstance("RSA");
            cipher2.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decryptedWithPublicKey = cipher2.doFinal(decryptedWithPrivateKey);
            return formatResult(base64ToAscii(decryptedWithPublicKey));
        } catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String formatResult(String res){//test format
        return res.substring(res.indexOf("{")).trim();
    }

    public static String base64ToAscii(byte[] s){
        return StandardCharsets.US_ASCII.decode(java.nio.ByteBuffer.wrap(s)).toString();
    }
    public static PrivateKey readPrivateKeyFromPem(String key) throws Exception {
        String formatKey = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("\r", "")
                .replace("\n", "");
        PKCS8EncodedKeySpec pkcs8Spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(formatKey));
        KeyFactory kF = KeyFactory.getInstance("RSA");
        return kF.generatePrivate(pkcs8Spec);
    }
    public static PublicKey readPublicKeyFromPem(String key) throws Exception {
        String formatKey = key.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\r", "")
                .replace("\n", "");
        X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(Base64.getDecoder().decode(formatKey));
        KeyFactory kF = KeyFactory.getInstance("RSA");
        return kF.generatePublic(x509Spec);
    }
    public static void generateKeys() {
        try {
            // Generate a key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(3000);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Get the public and private keys
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

            // Convert the keys to PEM format
            String publicKeyPEM = toPEM(publicKeyBytes, "PUBLIC KEY");
            String privateKeyPEM = toPEM(privateKeyBytes, "PRIVATE KEY");
            // Store the keys
            Modello.putBean("clientPublicKeyPEM", publicKeyPEM);
            Modello.putBean("clientPrivateKeyPEM", privateKeyPEM);
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }

    public static String toPEM(byte[] keyBytes, String type) {
        String encoded = Base64.getEncoder().encodeToString(keyBytes);
        return "-----BEGIN " + type + "-----\n" + encoded + "\n-----END " + type + "-----";
    }

    public static String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String h = Integer.toHexString(0xFF & b);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    //---Altri metodi di cifratura---
    public static String sha1( String toHash )
    {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = convertToHex( bytes );
        }
        catch( NoSuchAlgorithmException | UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        return hash;
    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public static String sha384(String toHash )
    {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-384" );
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = convertToHex( bytes );
        }
        catch( NoSuchAlgorithmException | UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        return hash;
    }

    public static String sha512(String toHash) {
        MessageDigest md = null;
        byte[] hash = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            hash = md.digest(toHash.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return convertToHex(hash);
    }

    public static String convertToHex(byte[] raw) {
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
