package com.coap.elements.auth;

/**
 * A principal representing an authenticated peer's <em>RawPublicKey</em>.
 */
public class RawPublicKeyIdentity implements Principal {

	private static final int BASE_64_ENCODING_OPTIONS = Base64.ENCODE | Base64.URL_SAFE | Base64.NO_PADDING;
	private String niUri;
	private final PublicKey publicKey;

	/**
	 * Creates a new instance for a given public key.
	 * 
	 * @param key the public key
	 * @throws NullPointerException if the key is <code>null</code>
	 */
	public RawPublicKeyIdentity(PublicKey key) {
		if (key == null) {
			throw new NullPointerException("Public key must not be null");
		} else {
			this.publicKey = key;
			createNamedInformationUri(publicKey.getEncoded());
		}
	}

	/**
	 * Creates a new instance for a given ASN.1 subject public key info structure.
	 * <p>
	 * The given subject info is expected to represent an EC public key.
	 * 
	 * @param subjectInfo the ASN.1 encoded X.509 subject public key info.
	 * @throws NullPointerException if the subject info is <code>null</code>
	 * @throws GeneralSecurityException if the JVM does not support the key
	 *             algorithm used by the public key.
	 */
	public RawPublicKeyIdentity(byte[] subjectInfo) throws GeneralSecurityException {
		this(subjectInfo, null);
	}

	/**
	 * Creates a new instance for a given ASN.1 subject public key info structure.
	 * 
	 * @param subjectInfo the ASN.1 encoded X.509 subject public key info.
	 * @param keyAlgorithm the algorithm name to verify, that the subject public
	 *            key uses this key algorithm, or to support currently not
	 *            supported key algorithms for serialization/deserialization. If
	 *            {@code null}, use the to key algorithm provided by the ASN.1
	 *            DER encoded subject public key.
	 * @throws NullPointerException if the subject info is <code>null</code>
	 * @throws GeneralSecurityException if the JVM does not support the given key algorithm.
	 */
	public RawPublicKeyIdentity(byte[] subjectInfo, String keyAlgorithm) throws GeneralSecurityException {
		if (subjectInfo == null) {
			throw new NullPointerException("SubjectPublicKeyInfo must not be null");
		} else {
			String specKeyAlgorithm = null;
			try {
				specKeyAlgorithm = Asn1DerDecoder.readSubjectPublicKeyAlgorithm(subjectInfo);
			} catch (IllegalArgumentException ex) {
				throw new GeneralSecurityException(ex.getMessage());
			}
			X509EncodedKeySpec spec = new X509EncodedKeySpec(subjectInfo);
			if (keyAlgorithm != null) {
				if (specKeyAlgorithm == null) {
					// use the provided key algorithm
					specKeyAlgorithm = keyAlgorithm;
				} else if (!Asn1DerDecoder.equalKeyAlgorithmSynonyms(specKeyAlgorithm, keyAlgorithm)) {
					throw new GeneralSecurityException(String.format("Provided key algorithm %s doesn't match %s!",
							keyAlgorithm, specKeyAlgorithm));
				}
			} else if (specKeyAlgorithm == null) {
				throw new GeneralSecurityException("Key algorithm could not be determined!");
			}
			this.publicKey = KeyFactory.getInstance(specKeyAlgorithm).generatePublic(spec);
			createNamedInformationUri(subjectInfo);
		}
	}

	private void createNamedInformationUri(byte[] subjectPublicKeyInfo) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(subjectPublicKeyInfo);
			byte[] digest = md.digest();
			String base64urlDigest = Base64.encodeBytes(digest, BASE_64_ENCODING_OPTIONS);
			StringBuilder b = new StringBuilder("ni:///sha-256;").append(base64urlDigest);
			niUri = b.toString();
		} catch (NoSuchAlgorithmException | IOException e) {
			// should not happen because SHA-256 is a mandatory message digest algorithm for any Java 7 VM
			// no Base64 encoding of InputStream is done
		}
	}

	/**
	 * Gets the <em>Named Information</em> URI representing this raw public key.
	 * 
	 * The URI is created using the SHA-256 hash algorithm on the key's
	 * <em>SubjectPublicKeyInfo</em> as described in
	 * <a href="http://tools.ietf.org/html/rfc6920#section-2">RFC 6920, section 2</a>.
	 * 
	 * @return the named information URI
	 */
	@Override
	public final String getName() {
		return niUri;
	}

	/**
	 * Gets the raw public key.
	 * 
	 * @return the key
	 */
	public final PublicKey getKey() {
		return publicKey;
	}

	/**
	 * Gets the key's ASN.1 encoded <em>SubjectPublicKeyInfo</em>.
	 * 
	 * @return the subject info
	 */
	public final byte[] getSubjectInfo() {
		return publicKey.getEncoded();
	}

	/**
	 * Gets a string representation of this principal.
	 * 
	 * Clients should not assume any particular format of the returned string
	 * since it may change over time.
	 *  
	 * @return the string representation
	 */
	@Override
	public String toString() {
		return new StringBuilder("RawPublicKey Identity [").append(niUri).append("]").toString();
	}

	/**
	 * Creates a hash code based on the key's ASN.1 encoded <em>SubjectPublicKeyInfo</em>.
	 * 
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((publicKey == null) ? 0 : Arrays.hashCode(getSubjectInfo()));
		return result;
	}

	/**
	 * Checks if this instance is equal to another object.
	 * 
	 * @return <code>true</code> if the other object is a <code>RawPublicKeyIdentity</code>
	 *           and has the same <em>SubjectPublicKeyInfo</em> as this instance
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof RawPublicKeyIdentity)) {
			return false;
		} else {
			RawPublicKeyIdentity other = (RawPublicKeyIdentity) obj;
			if (publicKey == null) {
				if (other.publicKey != null) {
					return false;
				}
			} else if (!Arrays.equals(getSubjectInfo(), other.getSubjectInfo())) {
				return false;
			}
			return true;
		}
	}
}
