/*
 * Decompiled with CFR 0.150.
 */
package cw;

import com.motivewave.common.MWException;
import com.motivewave.common.i;
import com.motivewave.common.util.o;
import com.motivewave.common.util.x;
import com.motivewave.platform.common.Enums$Edition;
import com.motivewave.platform.common.Enums$MessageType;
import com.motivewave.platform.common.Enums$Module;
import com.motivewave.platform.common.Enums$ResponseCode;
import com.motivewave.platform.common.aa;
import com.motivewave.platform.common.af;
import com.motivewave.platform.common.ao;
import com.motivewave.platform.common.b;
import com.motivewave.platform.common.g;
import com.motivewave.platform.databean.StudyNamespace;
import com.motivewave.platform.service.ap;
import com.motivewave.platform.service.be;
import cw.c;
import cw.d;
import cw.e;
import cw.h;
import cw.l;
import cw.m;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public class p {
    static String a = com.motivewave.platform.common.aa.e("gvunw</,vyx,qqtfugx_zg.`no0");
    static String b = com.motivewave.platform.common.aa.e("gvun>1/tvy/ksvisdybti0cll1");
    static String c = com.motivewave.platform.common.aa.e("vyx,qqtfugx_zg.`no");
    static String d = com.motivewave.platform.common.aa.e("tre_xg/ntgsw2fo");
    static String e = com.motivewave.platform.common.aa.e("kkdcrue,ucmghctb-fp");
    static String f = com.motivewave.platform.common.aa.e("kkdcrue,tre_xg_pdtwggg.an");
    static String g = com.motivewave.platform.common.aa.e("kkdcrue,`eugzctb-fp");
    static String h = com.motivewave.platform.common.aa.e("kkdcrue,kqhgracinwe,hq");
    static String i = com.motivewave.platform.common.aa.e("kkdcrue,qgoc{0dl");
    static String j = com.motivewave.platform.common.aa.e("kkdcrue,qgmceue+cq");
    static String k = com.motivewave.platform.common.aa.e("otpdmne\\hf");
    static String l = com.motivewave.platform.common.aa.e("lcdfmpe\\hf");
    static String m = com.motivewave.platform.common.aa.e("ugsqmqn");
    static String n = com.motivewave.platform.common.aa.e("awjjh");
    static String o = com.motivewave.platform.common.aa.e("b{qfit");
    static String p = com.motivewave.platform.common.aa.e("bnjcrv_sdttgsp");
    static String q = com.motivewave.platform.common.aa.e("rkhlevuod");
    static String r = com.motivewave.platform.common.aa.e("tre_xg_^ucjjedlb");
    static String s = com.motivewave.platform.common.aa.e("ocdieie");
    static String t = com.motivewave.platform.common.aa.e("awjjhUentgoai");
    static String u = com.motivewave.platform.common.aa.e("ewmj");
    static String v = com.motivewave.platform.common.aa.e("atpiit");
    static String w = com.motivewave.platform.common.aa.e("rgstmee");
    static String x = com.motivewave.platform.common.aa.e("dfjrmqn");
    static String y = com.motivewave.platform.common.aa.e("tre_xgs");
    static String z = com.motivewave.platform.common.aa.e("sqlcr");
    static String A = com.motivewave.platform.common.aa.e("etfccvrf`n");
    static String B = com.motivewave.platform.common.aa.e("kkdcrueQxrf");
    static String C = com.motivewave.platform.common.aa.e("dzqgvgs");
    static String D = com.motivewave.platform.common.aa.e("ncobe");
    static String E = com.motivewave.platform.common.aa.e("lqespgs");
    static String F = com.motivewave.platform.common.aa.e("mcncwra`du");
    static String G = com.motivewave.platform.common.aa.e("mcncwra`d");
    static String H = com.motivewave.platform.common.aa.e("otpdmneFc");
    static String I = com.motivewave.platform.common.aa.e("dzqgvgd");
    static String J = com.motivewave.platform.common.aa.e("jgz");
    static String K = com.motivewave.platform.common.aa.e("ekmccvymd");
    static String L = com.motivewave.platform.common.aa.e("nu`leoe");
    static String M = com.motivewave.platform.common.aa.e("nu`titsfnp");
    static String N = com.motivewave.platform.common.aa.e("icw_cxeorkpl");
    static String O = com.motivewave.platform.common.aa.e("lgtqeie");
    static String P = com.motivewave.platform.common.aa.e("bqoriptQxrf");
    static String Q = com.motivewave.platform.common.aa.e("skuji");
    static String R = com.motivewave.platform.common.aa.e("s{qc");
    static String S = com.motivewave.platform.common.aa.e("bqoript");
    static String T = com.motivewave.platform.common.aa.e("gvnj");
    static String U = com.motivewave.platform.common.aa.e("rwdaius");
    static String V = com.motivewave.platform.common.aa.e("bqec");
    static String W = com.motivewave.platform.common.aa.e("kgbqi");
    static String X = com.motivewave.platform.common.aa.e("rwqnstt");
    static String Y = com.motivewave.platform.common.aa.e("dobgp");
    static String Z = com.motivewave.platform.common.aa.e("mcnc");
    static String aa = com.motivewave.platform.common.aa.e("048,400+0");
    static String ab = com.motivewave.platform.common.aa.e("bkuw");
    static String ac = com.motivewave.platform.common.aa.e("qghgsp");
    static String ad = com.motivewave.platform.common.aa.e("qghgspClcg");
    static String ae = com.motivewave.platform.common.aa.e("bqvlxty");
    static String af = com.motivewave.platform.common.aa.e("bqvlxty@nff");
    static String ag = com.motivewave.platform.common.aa.e("vuCsgmeq");
    static int a = 20000;
    private static long a = 0L;
    private static boolean a = false;

    public static m a(c c2) {
        Object object;
        Iterator iterator;
        Object object2;
        Node node;
        Node node2;
        String string = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put(J, com.motivewave.platform.common.aa.d(c2.a()));
        hashMap.put(o, string);
        hashMap.put(l, com.motivewave.platform.common.aa.d(cw.h.j()));
        hashMap.put(L, System.getProperty("os.name"));
        hashMap.put(M, System.getProperty("os.version"));
        hashMap.put(N, com.motivewave.common.i.b());
        hashMap.put(m, ao.a());
        hashMap.put(n, "479");
        hashMap.put(v, com.motivewave.platform.common.aa.b());
        m m2 = new m();
        if (com.motivewave.platform.common.aa.e(c)) {
            m2.a = Enums$ResponseCode.INVALID_LICENSE;
            return m2;
        }
        Element element = cw.p.a(e, hashMap);
        if (element == null) {
            m2.a = false;
            m2.a = Enums$ResponseCode.INVALID_LICENSE;
            return m2;
        }
        cw.h.bi();
        String string2 = com.motivewave.common.util.g.a(q, (Node)element);
        String string3 = com.motivewave.common.util.g.a(x, (Node)element);
        String string4 = com.motivewave.common.util.g.a(A, (Node)element);
        String string5 = com.motivewave.common.util.g.a(B, (Node)element);
        if (!com.motivewave.common.i.g(string5)) {
            cw.h.h(string5);
        }
        if ((node2 = com.motivewave.common.util.g.b(A, element)) != null && !com.motivewave.common.i.g(node2.getAttribute(C))) {
            cw.h.a(com.motivewave.common.i.b(node2.getAttribute(C)));
            String string6 = node2.getAttribute(I);
            if (!com.motivewave.common.i.g(string6)) {
                cw.h.f(com.motivewave.common.i.c((Object)string6));
            }
        }
        if ((node2 = com.motivewave.common.util.g.b(W, element)) != null) {
            cw.h.b(com.motivewave.common.i.b(node2.getAttribute(C)));
        }
        if ((node2 = com.motivewave.common.util.g.b(X, element)) != null) {
            cw.h.c(com.motivewave.common.i.b(node2.getAttribute(C)));
        }
        boolean bl2 = false;
        if (!com.motivewave.common.i.g(string4)) {
            bl2 = com.motivewave.common.i.c((Object)string4);
        }
        string = string + "|" + element.getAttribute(U) + "|" + string3 + "|" + string5;
        string = com.motivewave.common.i.a((Object)string5, (Object)"TRIAL") ? string + "|" + cw.h.a() : (com.motivewave.common.i.a((Object)string5, (Object)"LEASE") ? string + "|" + cw.h.b() : (com.motivewave.common.i.a((Object)string5, (Object)"OWN") ? string + "|" + cw.h.c() : string + "|0"));
        m2.a = com.motivewave.common.i.c((Object)element.getAttribute(U));
        m2.a = cw.p.a(element.getAttribute(V));
        node2 = com.motivewave.common.util.g.b(Y, element);
        if (node2 != null) {
            cw.h.b(com.motivewave.common.util.g.b(node2));
        }
        if ((node2 = com.motivewave.common.util.g.b(y, element)) != null) {
            cw.h.c(com.motivewave.common.util.g.b(node2));
        }
        if ((node2 = com.motivewave.common.util.g.b(Z, element)) != null) {
            cw.h.d(com.motivewave.common.util.g.b(node2));
            c2.a(com.motivewave.common.util.g.b(node2));
        }
        if ((node2 = com.motivewave.common.util.g.b(ag, element)) != null) {
            cw.e.a(com.motivewave.common.util.g.b(node2));
        }
        cw.h.be();
        cw.h.e(string);
        cw.h.g(string2);
        String string7 = element.getAttribute(D);
        if (!com.motivewave.common.i.g(string7)) {
            cw.h.i(com.motivewave.common.i.c((Object)string7));
        }
        if (!com.motivewave.common.i.g(string7 = element.getAttribute("iqfeed"))) {
            cw.h.j(com.motivewave.common.i.c((Object)string7));
        }
        if (!com.motivewave.common.i.g(string7 = element.getAttribute("stage5"))) {
            cw.h.l(com.motivewave.common.i.c((Object)string7));
        }
        if (!com.motivewave.common.i.g(string7 = element.getAttribute("cqg"))) {
            cw.h.k(com.motivewave.common.i.c((Object)string7));
        }
        if (!com.motivewave.common.i.g(string7 = element.getAttribute("backFillURL"))) {
            com.motivewave.platform.common.b.a().set("backFillURL", string7);
        }
        cw.h.f(com.motivewave.common.util.g.a("id", (Node)element));
        cw.h.e();
        if (m2.a && !cw.d.a(string, string2)) {
            com.motivewave.common.i.a.severe(com.motivewave.platform.common.af.c("LOG_INVALID_SERVER_SIGNATURE", new Object[0]));
            m2.b = false;
            return m2;
        }
        c2.a(com.motivewave.common.i.b(element.getAttribute(H)));
        cw.h.d(c2.a());
        cw.p.a(m2, element);
        if (bl2) {
            com.motivewave.common.i.a.info(com.motivewave.platform.common.af.c("LOG_LICENSE_EDITION_TRIAL", string3));
        } else {
            com.motivewave.common.i.a.info(com.motivewave.platform.common.af.c("LOG_LICENSE_EDITION", string3));
        }
        if (!com.motivewave.common.i.g(string3)) {
            try {
                if (com.motivewave.common.i.a((Object)string3, (Object)"TRADE")) {
                    string3 = "ORDER_FLOW";
                } else if (com.motivewave.common.i.a((Object)string3, (Object)"ITG_TRADE")) {
                    string3 = "ITG_ORDER_FLOW";
                }
                m2.a = Enums$Edition.valueOf(string3);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if ((node = com.motivewave.common.util.g.b(E, element)) != null) {
            object2 = com.motivewave.common.i.a(com.motivewave.common.util.g.b(node), '|');
            iterator = object2.iterator();
            while (iterator.hasNext()) {
                object = (String)iterator.next();
                try {
                    if (com.motivewave.common.i.a(object, (Object)"OFA_TRADE")) {
                        object = "OFA_ALGOX";
                    }
                    m2.a.add(Enums$Module.valueOf((String)object));
                }
                catch (Exception exception) {}
            }
            com.motivewave.common.i.a.info(com.motivewave.platform.common.af.c("LOG_LICENSE_MODULES", com.motivewave.common.i.a((Collection)m2.a, ",")));
        }
        if ((object2 = com.motivewave.common.util.g.b(F, element)) != null) {
            m2.b.clear();
            iterator = com.motivewave.common.util.g.c(G, (Node)object2).iterator();
            while (iterator.hasNext()) {
                object = (Element)iterator.next();
                if (object == null) continue;
                String string8 = object.getAttribute("name");
                boolean bl3 = true;
                if (!com.motivewave.common.i.g(object.getAttribute("enabled"))) {
                    bl3 = com.motivewave.common.i.c((Object)object.getAttribute("enabled"));
                }
                m2.b.add(new StudyNamespace(string8, bl3));
            }
        }
        if ((iterator = com.motivewave.common.util.g.b("cqg", element)) != null) {
            cw.h.g(com.motivewave.common.i.c((Object)iterator.getAttribute("enabled")));
            cw.h.i(com.motivewave.common.util.g.c("trialTitle", iterator));
            cw.h.j(com.motivewave.common.util.g.c("trialMsg", iterator));
            cw.h.m(com.motivewave.common.util.g.c("connectTitle", iterator));
            cw.h.n(com.motivewave.common.util.g.c("connectMsg", iterator));
            cw.h.k(com.motivewave.common.util.g.c("warningTitle", iterator));
            cw.h.l(com.motivewave.common.util.g.c("warningMsg", iterator));
        }
        return m2;
    }

    public static void a() {
        if (!ap.W()) {
            return;
        }
        try {
            String string = f + "?key=" + com.motivewave.common.util.x.a(com.motivewave.platform.common.aa.d(cw.c.c()));
            for (be be2 : ap.v()) {
                string = string + "&service=" + com.motivewave.common.util.x.a(be2.a().a().name());
            }
            com.motivewave.common.util.p.b(a + string, null, null);
        }
        catch (Exception exception) {
            com.motivewave.common.i.a.warning("updateService() error updating services: " + exception.getMessage());
        }
    }

    public static m b(c c2) {
        String string;
        String string2;
        String string3;
        Object object;
        Object object2;
        String string4 = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put(J, com.motivewave.platform.common.aa.d(c2.a()));
        hashMap.put(o, string4);
        hashMap.put(L, System.getProperty("os.name"));
        hashMap.put(M, System.getProperty("os.version"));
        hashMap.put(N, com.motivewave.common.i.b());
        hashMap.put(m, ao.a());
        hashMap.put(n, "479");
        hashMap.put(l, com.motivewave.platform.common.aa.d(cw.h.j()));
        m m2 = new m();
        if (com.motivewave.platform.common.aa.e(c)) {
            m2.a = Enums$ResponseCode.INVALID_LICENSE;
            return m2;
        }
        Element element = null;
        try {
            object2 = com.motivewave.common.util.p.a(a + g, null, hashMap, a);
            try {
                element = com.motivewave.common.util.g.a(((o)object2).b()).getDocumentElement();
            }
            catch (Exception exception) {
                object2 = com.motivewave.common.util.p.a(b + g, null, hashMap, a);
                element = com.motivewave.common.util.g.a(((o)object2).b()).getDocumentElement();
            }
        }
        catch (MWException mWException) {
            if (mWException.getCode() == 500) {
                com.motivewave.platform.common.h.a(com.motivewave.platform.common.af.a("TITLE_ERROR_CREATE_LICENSE", new Object[0]), com.motivewave.platform.common.af.a("MSG_ERROR_LICENSE_SRV_DOWN", new Object[0]));
                return null;
            }
            try {
                object = com.motivewave.common.util.p.a(b + g, null, hashMap, a);
                element = com.motivewave.common.util.g.a(((o)object).b()).getDocumentElement();
            }
            catch (MWException mWException2) {
                if (mWException2.getCode() == 500) {
                    com.motivewave.platform.common.h.a(com.motivewave.platform.common.af.a("TITLE_ERROR_CREATE_LICENSE", new Object[0]), com.motivewave.platform.common.af.a("MSG_ERROR_LICENSE_SRV_DOWN", new Object[0]));
                    return null;
                }
                com.motivewave.platform.common.h.a(com.motivewave.platform.common.af.a("TITLE_HTTP_ERROR", new Object[0]), com.motivewave.platform.common.g.E_HTTP_CONNECT_MW.a(cw.p.a(mWException2)));
                return null;
            }
        }
        if (element == null) {
            return null;
        }
        cw.p.a(c2);
        object2 = com.motivewave.common.util.g.a(q, (Node)element);
        string4 = string4 + "|" + element.getAttribute(U);
        m2.a = com.motivewave.common.i.c((Object)element.getAttribute(U));
        m2.a = cw.p.a(element.getAttribute(V));
        object = element.getAttribute(ab);
        if (!com.motivewave.common.i.g((String)object)) {
            cw.h.o((String)object);
        }
        if (!com.motivewave.common.i.g(string3 = element.getAttribute(ad))) {
            cw.h.q(string3);
        }
        if (!com.motivewave.common.i.g(string2 = element.getAttribute(ac))) {
            cw.h.p(string2);
        }
        if (!com.motivewave.common.i.g(string = element.getAttribute(af))) {
            cw.h.r(string);
        }
        if (!com.motivewave.common.i.g((String)(object = element.getAttribute(ae)))) {
            cw.h.s((String)object);
        }
        if (!com.motivewave.common.i.g((String)(object = element.getAttribute(ag)))) {
            cw.e.a((String)object);
        }
        if (m2.a && !cw.d.a(string4, (String)object2)) {
            m2.b = false;
            return m2;
        }
        c2.a(com.motivewave.common.i.b(element.getAttribute(H)));
        cw.p.a(m2, element);
        return m2;
    }

    public static boolean a(c c2) {
        Object object;
        String string = "" + System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put(J, com.motivewave.platform.common.aa.d(c2.a()));
        hashMap.put(o, string);
        hashMap.put(n, "479");
        hashMap.put(l, com.motivewave.platform.common.aa.d(cw.h.j()));
        if (com.motivewave.platform.common.aa.e(c)) {
            return false;
        }
        Element element = null;
        try {
            object = com.motivewave.common.util.p.a(a + h, null, hashMap, a);
            cw.e.a((o)object);
            if (!cw.e.c()) {
                object = com.motivewave.common.util.p.a(b + h, null, hashMap, a);
                cw.e.a((o)object);
            }
            try {
                element = com.motivewave.common.util.g.a(((o)object).b()).getDocumentElement();
            }
            catch (Exception exception) {
                object = com.motivewave.common.util.p.a(b + h, null, hashMap, a);
                cw.e.a((o)object);
                element = com.motivewave.common.util.g.a(((o)object).b()).getDocumentElement();
            }
        }
        catch (MWException mWException) {
            if (mWException.getCode() == 500) {
                return false;
            }
            try {
                o o2 = com.motivewave.common.util.p.a(b + h, null, hashMap, a);
                cw.e.a(o2);
                element = com.motivewave.common.util.g.a(o2.b()).getDocumentElement();
            }
            catch (MWException mWException2) {
                return false;
            }
        }
        if (element == null) {
            return false;
        }
        object = element.getAttribute(ag);
        if (!com.motivewave.common.i.g((String)object)) {
            cw.e.a((String)object);
        }
        if (!com.motivewave.common.i.g((String)(object = element.getAttribute("maxWS")))) {
            cw.e.a(com.motivewave.common.i.b((String)object));
        }
        if (!com.motivewave.common.i.g((String)(object = element.getAttribute("maxWSSize")))) {
            cw.e.a((long)com.motivewave.common.i.b((String)object));
        }
        if (!com.motivewave.common.i.g((String)(object = element.getAttribute("baseUrl")))) {
            cw.e.b((String)object);
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static m a() {
        if (a || System.currentTimeMillis() - a < 600000L) {
            return null;
        }
        a = true;
        try {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put(k, com.motivewave.platform.common.aa.d("" + cw.c.b()));
            hashMap.put(l, com.motivewave.platform.common.aa.d(cw.h.j()));
            hashMap.put(m, ao.a());
            hashMap.put(n, "479");
            m m2 = new m();
            Element element = cw.p.a(i, hashMap);
            if (element == null) {
                m2.a = false;
                m2.a = Enums$ResponseCode.INVALID_LICENSE;
                m m3 = m2;
                return m3;
            }
            m2.a = com.motivewave.common.i.c((Object)element.getAttribute(U));
            m2.a = cw.p.a(element.getAttribute(V));
            cw.p.a(m2, element);
            a = System.currentTimeMillis();
            m m4 = m2;
            return m4;
        }
        finally {
            a = false;
        }
    }

    public static m b() {
        if (!cw.c.a()) {
            return null;
        }
        if (cw.h.a() > 0 || !ap.W()) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(k, com.motivewave.platform.common.aa.d("" + cw.c.b()));
        hashMap.put(l, com.motivewave.platform.common.aa.d(cw.h.j()));
        hashMap.put(m, ao.a());
        hashMap.put(n, "479");
        m m2 = new m();
        Element element = cw.p.a(j, hashMap);
        if (element == null) {
            m2.a = false;
            m2.a = Enums$ResponseCode.INVALID_LICENSE;
            return m2;
        }
        m2.a = com.motivewave.common.i.c((Object)element.getAttribute(U));
        m2.a = cw.p.a(element.getAttribute(V));
        cw.p.a(m2, element);
        return m2;
    }

    private static void a(m m2, Element element) {
        Node node = com.motivewave.common.util.g.a(O, (Node)element);
        if (node != null && !node.getAttribute(R).equals("NONE")) {
            m2.a = new l();
            m2.a.a = com.motivewave.common.i.a((Object)node.getAttribute(P), (Object)T);
            m2.a.a = com.motivewave.common.util.g.b(Q, node);
            m2.a.b = com.motivewave.common.util.g.b(S, node);
            m2.a.a = Enums$MessageType.valueOf(node.getAttribute(R));
        }
    }

    private static Enums$ResponseCode a(String string) {
        try {
            return Enums$ResponseCode.valueOf(string);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static String a(Exception exception) {
        if (exception.getCause() != null) {
            return exception.getCause().getMessage();
        }
        return exception.getMessage();
    }

    private static Element a(String string, Map map) {
        if (com.motivewave.platform.common.aa.e(c)) {
            return null;
        }
        try {
            o o2 = com.motivewave.common.util.p.a(a + string, null, map, a);
            return com.motivewave.common.util.g.a(o2.b()).getDocumentElement();
        }
        catch (MWException mWException) {
            if (mWException.getCode() == 500) {
                throw mWException;
            }
            o o3 = com.motivewave.common.util.p.a(b + string, null, map, a);
            return com.motivewave.common.util.g.a(o3.b()).getDocumentElement();
        }
    }
}

