/*
 * Decompiled with CFR 0.150.
 */
package cw;

import cw.d;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;

public class n {
    private static PublicKey a = null;

    public static PublicKey a(byte[] arrby) {
        if (a != null) {
            return a;
        }
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(arrby);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
            a = keyFactory.generatePublic(x509EncodedKeySpec);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return a;
    }

    public static boolean a(String string, byte[] arrby) {
        try {
            Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
            signature.initVerify(d.a());
            signature.update(string.getBytes("UTF-8"));
            return signature.verify(arrby);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }
}

