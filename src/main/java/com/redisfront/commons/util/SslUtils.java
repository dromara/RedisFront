package com.redisfront.commons.util;

import cn.hutool.core.io.resource.ClassPathResource;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SslUtils {

    public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String crtFile, final String keyFile, final String password) throws Exception {
        InputStream caInputStream = null;
        InputStream crtInputStream = null;
        InputStream keyInputStream = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            caInputStream = new ClassPathResource(caCrtFile).getStream();
            X509Certificate caCert = null;
            while (caInputStream.available() > 0) {
                caCert = (X509Certificate) cf.generateCertificate(caInputStream);
            }

            crtInputStream = new ClassPathResource(crtFile).getStream();
            X509Certificate cert = null;
            while (crtInputStream.available() > 0) {
                cert = (X509Certificate) cf.generateCertificate(crtInputStream);
            }

            keyInputStream = new ClassPathResource(keyFile).getStream();
            PEMParser pemParser = new PEMParser(new InputStreamReader(keyInputStream));
            Object object = pemParser.readObject();
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
            KeyPair key;
            if (object instanceof PEMEncryptedKeyPair) {
                System.out.println("Encrypted key - we will use provided password");
                key = converter.getKeyPair(((PEMEncryptedKeyPair) object).decryptKeyPair(decProv));
            } else {
                System.out.println("Unencrypted key - no password needed");
                key = converter.getKeyPair((PEMKeyPair) object);
            }
            pemParser.close();

            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            ks.setCertificateEntry("certificate", cert);
            ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[]{cert});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());

            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return context.getSocketFactory();

        } finally {
            if (null != caInputStream) {
                caInputStream.close();
            }
            if (null != crtInputStream) {
                crtInputStream.close();
            }
            if (null != keyInputStream) {
                keyInputStream.close();
            }
        }
    }
}
