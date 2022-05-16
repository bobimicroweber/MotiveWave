/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  org.apache.commons.codec.binary.Hex
 */
package cw;

import com.motivewave.common.MWException;
import com.motivewave.common.i;
import com.motivewave.platform.common.Enums$Edition;
import com.motivewave.platform.common.Enums$Module;
import com.motivewave.platform.common.Enums$ResponseCode;
import com.motivewave.platform.common.af;
import com.motivewave.platform.common.ao;
import com.motivewave.platform.databean.StudyNamespace;
import cw.b;
import cw.c;
import cw.h;
import cw.m;
import cw.n;
import cw.p;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.codec.binary.Hex;

public class d {
    private static byte[] a = new byte[]{48, -126, 1, -72, 48, -126, 1, 44, 6, 7, 42, -122, 72, -50, 56, 4, 1, 48, -126, 1, 31, 2, -127, -127, 0, -3, 127, 83, -127, 29, 117, 18, 41, 82, -33, 74, -100, 46, -20, -28, -25, -10, 17, -73, 82, 60, -17, 68, 0, -61, 30, 63, -128, -74, 81, 38, 105, 69, 93, 64, 34, 81, -5, 89, 61, -115, 88, -6, -65, -59, -11, -70, 48, -10, -53, -101, 85, 108, -41, -127, 59, -128, 29, 52, 111, -14, 102, 96, -73, 107, -103, 80, -91, -92, -97, -97, -24, 4, 123, 16, 34, -62, 79, -69, -87, -41, -2, -73, -58, 27, -8, 59, 87, -25, -58, -88, -90, 21, 15, 4, -5, -125, -10, -45, -59, 30, -61, 2, 53, 84, 19, 90, 22, -111, 50, -10, 117, -13, -82, 43, 97, -41, 42, -17, -14, 34, 3, 25, -99, -47, 72, 1, -57, 2, 21, 0, -105, 96, 80, -113, 21, 35, 11, -52, -78, -110, -71, -126, -94, -21, -124, 11, -16, 88, 28, -11, 2, -127, -127, 0, -9, -31, -96, -123, -42, -101, 61, -34, -53, -68, -85, 92, 54, -72, 87, -71, 121, -108, -81, -69, -6, 58, -22, -126, -7, 87, 76, 11, 61, 7, -126, 103, 81, 89, 87, -114, -70, -44, 89, 79, -26, 113, 7, 16, -127, -128, -76, 73, 22, 113, 35, -24, 76, 40, 22, 19, -73, -49, 9, 50, -116, -56, -90, -31, 60, 22, 122, -117, 84, 124, -115, 40, -32, -93, -82, 30, 43, -77, -90, 117, -111, 110, -93, 127, 11, -6, 33, 53, 98, -15, -5, 98, 122, 1, 36, 59, -52, -92, -15, -66, -88, 81, -112, -119, -88, -125, -33, -31, 90, -27, -97, 6, -110, -117, 102, 94, -128, 123, 85, 37, 100, 1, 76, 59, -2, -49, 73, 42, 3, -127, -123, 0, 2, -127, -127, 0, -60, -61, 13, 15, -43, 58, 112, 83, 107, -58, 89, -35, 44, 63, 13, 2, -91, 54, 55, 1, 120, -21, 92, -91, 72, -114, 47, 1, -45, -3, -43, 72, -68, 92, 32, 19, -83, -10, 15, 73, -99, 81, 36, -35, -58, 30, -9, -1, -76, -44, -61, -122, -112, -20, 15, 126, 77, 77, -42, -89, 102, -109, -127, 86, 12, -85, 65, 91, -70, 29, 85, -80, -119, -122, 74, 4, 35, 31, 116, 21, 87, 53, 4, 33, 103, 46, -55, -117, -120, -112, 107, 49, 69, -77, 12, -77, 81, 0, 107, -89, 15, -16, -50, 110, 85, 106, 7, 53, -80, 38, 125, -34, -52, 5, -78, -96, 47, -50, -76, -109, -29, -30, -96, 55, 55, 113, -26, 46};

    public static boolean a(c c2) {
        m m2 = p.b(c2);
        if (m2 == null && (m2 = p.b(c2)) == null) {
            return false;
        }
        if (!m2.b) {
            String string = m2.a == null ? "" : m2.a.b;
            i.a.warning(af.c("LOG_ERROR_ACTIVATE_LICENSE", new Object[]{c2.a(), m2.a, string}));
            MWException mWException = new MWException(af.a("MSG_BAD_SERVER_SIGNATURE", new Object[0]));
            mWException.setShortMessage(af.a("TITLE_BAD_SERVER_SIGNATURE", new Object[0]));
            throw mWException;
        }
        if (!m2.a) {
            String string = af.a("TITLE_ERROR_ACTIVATE_LICENSE", new Object[0]);
            String string2 = af.a("MSG_ERROR_ACTIVATE_LICENSE", new Object[0]);
            h.T();
            if (m2.a != null) {
                if (!i.g(m2.a.b)) {
                    string2 = m2.a.b;
                }
                if (!i.g(m2.a.a)) {
                    string = m2.a.a;
                }
            }
            i.a.warning(af.c("LOG_ERROR_ACTIVATE_LICENSE", new Object[]{c2.a(), m2.a, string2}));
            MWException mWException = new MWException(string2);
            mWException.setShortMessage(string);
            throw mWException;
        }
        return true;
    }

    public static boolean b(c c2) {
        m m2 = p.a(c2);
        if (m2 == null) {
            c2.a(Enums$Edition.NONE);
            h.a(Enums$Edition.NONE);
            h.a(new ArrayList());
            h.b(new ArrayList());
            h.T();
            return true;
        }
        if (i.a((Object)m2.a, new Object[]{Enums$ResponseCode.UNAUTHORIZED_VERSION})) {
            MWException mWException = new MWException(af.a("MSG_UNAUTHORIZED_VERSION", i.d(h.c())));
            mWException.setShortMessage(af.a("TITLE_UNAUTHORIZED_VERSION", new Object[0]));
            throw mWException;
        }
        if (!m2.b) {
            MWException mWException = new MWException(af.a("MSG_BAD_SERVER_SIGNATURE", new Object[0]));
            mWException.setShortMessage(af.a("TITLE_BAD_SERVER_SIGNATURE", new Object[0]));
            throw mWException;
        }
        if (i.a((Object)m2.a, new Object[]{Enums$ResponseCode.INVALID_LICENSE, Enums$ResponseCode.INVALID_PROFILE})) {
            String string = af.a("MSG_INVALID_LICENSE", new Object[0]);
            h.an();
            if (m2.a != null && !i.g(m2.a.b)) {
                string = m2.a.b;
            }
            throw new MWException(string);
        }
        if (i.a((Object)m2.a, new Object[]{Enums$ResponseCode.TRAIL_EXPIRED})) {
            h.f(true);
            h.e();
            h.ao();
            String string = af.a("TITLE_TRIAL_PERIOD_OVER", new Object[0]);
            String string2 = af.a("MSG_TRIAL_PERIOD_OVER", new Object[0]);
            if (m2.a != null) {
                if (!i.g(m2.a.b)) {
                    string2 = m2.a.b;
                }
                if (!i.g(m2.a.a)) {
                    string = m2.a.a;
                }
            }
            MWException mWException = new MWException(string2);
            mWException.setShortMessage(string);
            throw mWException;
        }
        if (i.a((Object)m2.a, new Object[]{Enums$ResponseCode.INVALID_VERSION})) {
            MWException mWException = new MWException(af.a("MSG_UNAUTHORIZED_VERSION", i.d(h.c())));
            mWException.setShortMessage(af.a("TITLE_UNAUTHORIZED_VERSION", new Object[0]));
            throw mWException;
        }
        if (i.a((Object)h.s(), (Object)"LEASE") && h.b() > 0L && h.b() < System.currentTimeMillis()) {
            MWException mWException = new MWException(af.a("MSG_LEASE_PERIOD_OVER", new Object[0]));
            mWException.setShortMessage(af.a("TITLE_LEASE_PERIOD_OVER", new Object[0]));
            throw mWException;
        }
        if (m2.a != null && !i.g(m2.a.b)) {
            h.ar();
        }
        if (!m2.a) {
            String string = af.a("MSG_UNABLE_TO_VALIDATE_LICENSE", new Object[0]);
            if (m2.a != null && !i.g(m2.a.b)) {
                string = m2.a.b;
            }
            throw new MWException(string);
        }
        if (m2.a == null) {
            throw new MWException(af.a("MSG_INVALID_SERVER_RESPONSE", "INVALID_EDITION", ao.a()));
        }
        c2.a(m2.a);
        if (!i.a((Collection)m2.a)) {
            c2.a().clear();
            for (Object object : m2.a) {
                c2.a((Enums$Module)((Object)object));
            }
        }
        if (!i.a((Collection)m2.b)) {
            c2.b().clear();
            for (Object object : m2.b) {
                c2.a((StudyNamespace)object);
            }
        }
        h.a(0);
        h.a(m2.a);
        h.a(m2.a);
        h.b(m2.b);
        h.e();
        return true;
    }

    public static PublicKey a() {
        return n.a(a);
    }

    public static boolean a(String string, String string2) {
        try {
            if (i.g(string2) || i.g(string)) {
                return false;
            }
            return n.a(string, Hex.decodeHex((char[])string2.toCharArray()));
        }
        catch (Exception exception) {
            i.a.warning("verifySignature() " + exception.getMessage());
            return false;
        }
    }

    public static String a(String string) {
        if (i.g(string)) {
            return "";
        }
        return b.a(string, h.a());
    }

    public static String b(String string) {
        try {
            if (i.g(string)) {
                return "";
            }
            return b.b(string, h.a());
        }
        catch (Exception exception) {
            return "";
        }
    }
}

