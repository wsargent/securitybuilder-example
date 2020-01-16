package example

import java.io.InputStreamReader
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Duration
import java.util
import java.util.Collections

import com.tersesystems.securitybuilder._
import javax.net.ssl.SSLContext

import scala.compat.java8.FunctionConverters._

object Hello {

  // Parsing PEM encoded private keys
  def parsePemEncodedPrivateKey(): PKCS8EncodedKeySpec = {
    val reader = new InputStreamReader(getClass.getResourceAsStream("/private-key.pkcs8"))
    PKCS8EncodedKeySpecBuilder.builder.withReader(reader).withNoPassword.build
  }

  // Parsing PKCS1 formatted private keys
  def parsePKCS1Key(): PKCS8EncodedKeySpec = {
    // This gets mapped out to a PKCS8 keyspec as well
    val reader = new InputStreamReader(getClass.getResourceAsStream("/private-key.pkcs1"))
    PKCS8EncodedKeySpecBuilder.builder.withReader(reader).withNoPassword.build
  }

  // Parsing a chain of certs from a single PEM cert file
  def parseChainOfCertificates(): X509Certificate = {
    val inputStream = getClass.getResourceAsStream("/playframework.pem")
    CertificateBuilder.builder
      .withX509()
      .withReader(new InputStreamReader(inputStream))
      .build
  }

  // Providing convenience APIs to build an SSLContext
  def sslContext(): SSLContext = {
    val keyPairCreator      = KeyPairCreator.creator.withRSA.withKeySize(2048)
    val rootKeyPair         = keyPairCreator.create
    val intermediateKeyPair = keyPairCreator.create
    val eePair              = keyPairCreator.create

    val creator = X509CertificateCreator.creator.withSHA256withRSA.withDuration(Duration.ofDays(365))

    val issuer = "CN=letsencrypt.derp,O=Root CA"
    val chain = creator
      .withRootCA(issuer, rootKeyPair, 2)
      .chain(
        rootKeyPair.getPrivate,
        (rootCreator: X509CertificateCreator.PublicKeyStage) =>
          rootCreator
            .withPublicKey(intermediateKeyPair.getPublic)
            .withSubject("OU=intermediate CA")
            .withCertificateAuthorityExtensions(0)
            .chain(
              intermediateKeyPair.getPrivate,
              (intCreator: X509CertificateCreator.PublicKeyStage) =>
                intCreator
                  .withPublicKey(eePair.getPublic)
                  .withSubject("CN=tersesystems.com")
                  .withEndEntityExtensions
                  .chain
          )
      )
      .create
    val privateKeyStore                          = PrivateKeyStore.create("tersesystems.com", eePair.getPrivate, chain: _*)
    val certificates: util.List[X509Certificate] = Collections.singletonList(chain(2))
    val aliasFunction: X509Certificate => String = { cert =>
      "letsencrypt.org"
    }
    val trustStore = TrustStore.create(certificates, aliasFunction.asJava)

    SSLContextBuilder.builder.withTLS
      .withKeyManager(KeyManagerBuilder.builder.withSunX509.withPrivateKeyStore(privateKeyStore).build)
      .withTrustManager(TrustManagerBuilder.builder.withDefaultAlgorithm.withTrustStore(trustStore).build)
      .build
  }

  def main(args: Array[String]): Unit = {
    println(parsePemEncodedPrivateKey().toString)
    println(parsePKCS1Key().toString)
    println(parseChainOfCertificates().toString)
    println(sslContext())
  }

}
