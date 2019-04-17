package com.elasticpath.cortex.dce.authorization

import static com.elasticpath.cortex.dce.ClasspathFluentRelosClientFactory.getClient
import static com.elasticpath.cortex.dce.SharedConstants.DEFAULT_SCOPE

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.json.Json

import cucumber.api.DataTable
import cucumber.api.java.en.Given
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm

class JWTAuthorizationSteps {

	static PASSWORD = "password"
	static GIVEN_NAME = "testGivenName"
	static FAMILY_NAME = "testFamilyName"
	private static final String ISSUER = "am"
	private static final String CLAIM_SCOPE = "scope"
	private static final String CLAIM_ROLES = "roles"
	private static final String CLAIM_PROFILE = "profile"
	private static final String CLAIM_METADATA = "metadata"
	//copied from https://github.elasticpath.net/commerce/enterprise-account-management/blob/master/devops/docker/compose/env/api.env#L10
	static AM_AUTH_JWT_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCv3/4wTUHaA+OiHXEvNnH6rH3W4vdMYeu1Ndc9fGTOJUEkbjjBJBPFNiVcUTIgxZbxD9UodUoSae38yK/Jo57LmzA1zUQNFnqKAl8jwmnGBFuPsrNHbrUBONFtk6fUtTT1ViL3qKeWPpJ1Xqg/mwvAdnrdffTkgDRvWgywPKmBudbP4XdKIyY8boOylNDgc91wjfZKiFTtvnKwkOXG3SGJOnPpRx3WMiEe7cXUexT2+OzCBog6VrE+dKSAUGM4JLc/z/k+3K+7J7aM1v+rvuwuKlAz2ebzW4HBW57QDOe3OAqKfU5xemK6jRNYLFhi1GH07i54l+s/9IU3taNYCxE5AgMBAAECggEAOsgSnPI1YDiMjFJq0ezQi4e7R2b0YIMZ1Kb6GYLJ7lxlOVfdgDAeq3s9YW2B45ImsMsp4yvQZfcho6svlaUqHcLwigrhA77PlfDU+46u64/pSk3fvbAyrwXeeVEwdtdZq+XuKfgkiwK+0JuigB/B/cGbrwu6HeT7oXvnYb/kp4Z2eSdYtCEc+kbaWaLjkbpQysCjtiD2E37iRJvWIhILG9yge/zkKoR38Z7wmFy3lYKGOLwN5Jm368vxrbAcU/VqSrjaDPDR8iThryPVr7oq0sOXTVF4ti/qssXFsGgi7D1NYMknFCvpwYcLPKUGnnE9SD0wE9QUW0tNFKik2J/BAQKBgQDvOfXWWy46BgepBQt7mPMUoMtvPN6vPb1yBQaiJx0tVjozASEypIGkI7avU+8X7toZTfNpSgcBEN2ivaCgncUnYHjmiyno/M6TJM7S4BxRaAAzJjX72VOfARnHswkkmU8+0B0t8s1YLv7nvPMw2WSebStdK3w2+Gx9vzV3Z7daBQKBgQC8NOTgASrzvw1qSVaAm90hw2a6xc0IvogiedIh/Sc1rEWsJJQ+4e1rtNLUx9MMVupUWa5wRm2mBAf7qftgkZBjjWX6IzWANfJuiFFr9YY+nrn78azS47NaNg9wYRt409sIVnE9IwMVm6C4j0CYoUfL1Z3hvsEaA3fZeD8sHGecpQKBgFeFpAyFlXcDB8BtqRLSFXThkywiX/JLK0lQz2l51/HjTy2j5yppB0kvC8I/egUOjNqfK0PFIqTgL8Y/jEnXDu9mGzV1iqLEAyvQVp7OAW5DF3efLjby/uX4eB8YwGqUcGqxp0tNPpVZtEbCWVLw5iAUHr5aj3ppDwA+eWgzGOClAoGAG+8B1Vl3BYxislbGNvprPLqiwa/f+Z9F11AbZx4KtsWl5aHcOuUPdv/0ls49vcQcrX5ODK33Uj+g67JT9AZG1u/CpfiZ1Tisjck/xHVBgoPOeN1q2DYhhY8mhXh0Ol+/T6J81iUfLRLXLO1eGgk9/bvr3fiQ8op3PFjDj4l7u10CgYBETvqkN0+f4+ON6Av0nGjyjVDdeDNpjOT0tWnme6WMJeNqAqy0aWY4DXSdhS3fnPzdZ4BSppq/3ZV/SISvTzel/hy2/L1rYHdJ/rW7cbiudOYl/hj7JHgFwShIYXW/tNFlr0FGnP6fv8UA9xI2z8BdOF2Kxndo4p1Sm4BwIYSGkQ=="

	@Given('^I login using jwt authorization with the following details$')
	static void setHeaderValue(final DataTable tokenDetails) {
		Map<String, String> jwtTokenMap = tokenDetails.asMap(String.class, String.class)
		String roleString = jwtTokenMap.get(CLAIM_ROLES)
		String shopperGuid = jwtTokenMap.getOrDefault("shopper_guid", UUID.randomUUID().toString())
		String scope = jwtTokenMap.getOrDefault("scope", DEFAULT_SCOPE)
		String customerGuid = jwtTokenMap.getOrDefault("customer_guid", UUID.randomUUID().toString())
		String firstName = jwtTokenMap.getOrDefault("first_name", "first_name")
		String lastName = jwtTokenMap.getOrDefault("last_name", "last_name")
		int expirationInSeconds = jwtTokenMap.get("expiration_in_seconds") == null ? 300 : Integer.parseInt(jwtTokenMap.get("expiration_in_seconds"))

		String token = createToken(roleString, shopperGuid, scope, customerGuid, firstName, lastName, expirationInSeconds)

		Map<String, String> headers = new HashMap<String, String>()
		headers.put("Authorization", "Bearer " + token)
		client.setHeaders(headers)
	}

	//inspiration from https://github.elasticpath.net/commerce/enterprise-account-management/blob/master/app-auth/src/main/java/com/elasticpath/eam/appauth/service/impl/AppTokenServiceImpl.java
	static PrivateKey getPrivateKey() throws Exception {
		KeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(AM_AUTH_JWT_PRIVATE_KEY))
		KeyFactory keyFactory = KeyFactory.getInstance(SignatureAlgorithm.RS256.getFamilyName())
		return keyFactory.generatePrivate(privateKeySpec)
	}


	static String createToken(final String roleString, final String shopperGuid, final String scope, final String customerGuid,
							  final String firstName, final String lastName, final int expirationInSeconds) {
		List<String> roles = roleString.split(",").collect {a -> a.trim().toUpperCase()}

		Instant now = Instant.now()
		Date expiration = Date.from(now.plus(expirationInSeconds, ChronoUnit.SECONDS))
		Date issuedAt = Date.from(now)

		return Jwts.builder()
				.setIssuer(ISSUER)
				.setSubject(shopperGuid)
				.setAudience(scope)
				.setExpiration(expiration)
				.setIssuedAt(issuedAt)
				.claim(CLAIM_SCOPE, scope) // Multiple scopes are encoded into a single space delimited string
				.claim(CLAIM_ROLES, roles) // Roles are represented by a json list
				.claim(CLAIM_PROFILE, customerGuid)
				.claim(CLAIM_METADATA, getMetadata(firstName, lastName))
				.signWith(getPrivateKey())
				.compact()
	}

	static String getMetadata(final String firstName, final String lastName) {
		String json = Json.createObjectBuilder()
				.add("first-name", firstName)
				.add("last-name", lastName)
				.build()
				.toString()

		return Base64.getEncoder().encodeToString(json.getBytes())
	}
}
