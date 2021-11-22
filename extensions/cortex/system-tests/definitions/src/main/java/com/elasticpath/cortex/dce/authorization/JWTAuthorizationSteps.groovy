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
import org.apache.commons.lang3.StringUtils

class JWTAuthorizationSteps {

	private static final String PASSWORD = "password"
	private static final String GIVEN_NAME = "testGivenName"
	private static final String FAMILY_NAME = "testFamilyName"
	private static final String DEFAULT_ISSUER = "am"
	private static final String DEFAULT_USER_ID = "usertestguid"
	private static final String CLAIM_SCOPE = "scope"
	private static final String CLAIM_PROFILE = "profile"
	private static final String CLAIM_METADATA = "metadata"

	//copied from https://github.elasticpath.net/commerce/enterprise-account-management/blob/master/devops/docker/compose/env/api.env#L10
	private static final String PRIMARY_JWT_PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCv3/4wTUHaA+OiHXEvNnH6rH3W4vdMYeu1Ndc9fGTOJUEkbjjBJBPFNiVcUTIgxZbxD9UodUoSae38yK/Jo57LmzA1zUQNFnqKAl8jwmnGBFuPsrNHbrUBONFtk6fUtTT1ViL3qKeWPpJ1Xqg/mwvAdnrdffTkgDRvWgywPKmBudbP4XdKIyY8boOylNDgc91wjfZKiFTtvnKwkOXG3SGJOnPpRx3WMiEe7cXUexT2+OzCBog6VrE+dKSAUGM4JLc/z/k+3K+7J7aM1v+rvuwuKlAz2ebzW4HBW57QDOe3OAqKfU5xemK6jRNYLFhi1GH07i54l+s/9IU3taNYCxE5AgMBAAECggEAOsgSnPI1YDiMjFJq0ezQi4e7R2b0YIMZ1Kb6GYLJ7lxlOVfdgDAeq3s9YW2B45ImsMsp4yvQZfcho6svlaUqHcLwigrhA77PlfDU+46u64/pSk3fvbAyrwXeeVEwdtdZq+XuKfgkiwK+0JuigB/B/cGbrwu6HeT7oXvnYb/kp4Z2eSdYtCEc+kbaWaLjkbpQysCjtiD2E37iRJvWIhILG9yge/zkKoR38Z7wmFy3lYKGOLwN5Jm368vxrbAcU/VqSrjaDPDR8iThryPVr7oq0sOXTVF4ti/qssXFsGgi7D1NYMknFCvpwYcLPKUGnnE9SD0wE9QUW0tNFKik2J/BAQKBgQDvOfXWWy46BgepBQt7mPMUoMtvPN6vPb1yBQaiJx0tVjozASEypIGkI7avU+8X7toZTfNpSgcBEN2ivaCgncUnYHjmiyno/M6TJM7S4BxRaAAzJjX72VOfARnHswkkmU8+0B0t8s1YLv7nvPMw2WSebStdK3w2+Gx9vzV3Z7daBQKBgQC8NOTgASrzvw1qSVaAm90hw2a6xc0IvogiedIh/Sc1rEWsJJQ+4e1rtNLUx9MMVupUWa5wRm2mBAf7qftgkZBjjWX6IzWANfJuiFFr9YY+nrn78azS47NaNg9wYRt409sIVnE9IwMVm6C4j0CYoUfL1Z3hvsEaA3fZeD8sHGecpQKBgFeFpAyFlXcDB8BtqRLSFXThkywiX/JLK0lQz2l51/HjTy2j5yppB0kvC8I/egUOjNqfK0PFIqTgL8Y/jEnXDu9mGzV1iqLEAyvQVp7OAW5DF3efLjby/uX4eB8YwGqUcGqxp0tNPpVZtEbCWVLw5iAUHr5aj3ppDwA+eWgzGOClAoGAG+8B1Vl3BYxislbGNvprPLqiwa/f+Z9F11AbZx4KtsWl5aHcOuUPdv/0ls49vcQcrX5ODK33Uj+g67JT9AZG1u/CpfiZ1Tisjck/xHVBgoPOeN1q2DYhhY8mhXh0Ol+/T6J81iUfLRLXLO1eGgk9/bvr3fiQ8op3PFjDj4l7u10CgYBETvqkN0+f4+ON6Av0nGjyjVDdeDNpjOT0tWnme6WMJeNqAqy0aWY4DXSdhS3fnPzdZ4BSppq/3ZV/SISvTzel/hy2/L1rYHdJ/rW7cbiudOYl/hj7JHgFwShIYXW/tNFlr0FGnP6fv8UA9xI2z8BdOF2Kxndo4p1Sm4BwIYSGkQ=="

	// Test private key for testing support of different public keys for jwt decryption.
	private static final String SECOND_JWT_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDZLvdas51YL3mJ/IuFm+vUn14fQx39dYpvjC1zcSLO1HGLuWVz+Jly155o9NdzecbWyhib7SRRunb8/Mg97XJOx0vnO8UM9C0lkv2K/fyxEl2MRccOop2j7s8ptNsRHZuz9TxbliWgL/8puQQUPsxYuoboRHw3zYMvG4Ags+dKSpu6FTe34c6ODDoakP7YnsUQPwAEfXyUVc7OpOyxlO6snrMplv8zKzlB6uD+aqbCkc0pzFYUSH7O/qtOVCRtUfNq4buZbAa+p1NE5j8clGhiO2laXR40agcJfHCjjiDRl2pILdyg96yRXEOcfKwCYdvdX1ozFNymEzm8m1HF5BEvAgMBAAECggEAWuCTMkncOg1LfxjEzPiMeuDgwpYUE32+mqR+VELv/LnadUvzjs5kbzCYIXTqPnCjajcQixZ0zxoGNiAULg2QejRcp3pUHvKBfap/G0ziEroLmOmNvKsA6ZrA5sO9svC/uVKRmgU+40OSMsQvV8VbPnl2f/RAQh1lmi2YtJZy3puKLo0VNLqaTy6LIE4FWKdaowyfuwHluOuoytTj+7zkTHAWgwu+PSrWdfkkHPUiz9SJ2acUJKEQFzPrnJGPsybNaEHY5w9ZsexQ7RttV38YJ6upP2EOuhPVpGbJphltEHNWgYmZYvDiL+xAZhgabUT+F/HUkwnuTeYCqHQJjYP7YQKBgQDsyscRhL8LcNSaV0/5t2QLO3AEsTYkV35o5LKgRFAdOjrR/3u52uakd0hWoLvKEk9VZvZ+0ZZtQW9KHNVzC0am9A6BFRcBd8GV/jwXd9nEnI75xKiXR5eLFFzIWyU3faQnHWAp6HAgJwKRfecmWT6dO9ce0XObGn9CRsqBgAYf3wKBgQDqzP7uxztN2zf1D2QHyLcuM1NF+SaIq6gGRRR3mxGRQTVj/LT5B7am4Cpp+Ma3Of9oERfcjAf+HVKP97VNLmpmmizdLMGGudxUsudCUwODh80oGGB7IiiRJXK4xRmKsqcDOzuHHsj6WVGeKFNLCpMrsL1tt9hqKhZWVtNX5jj4sQKBgAOxTdVqGoiVI9ucTPT59Sdvr9mC4fNDIgeEuXxiVFWAWPZ5Y3aMgVhDO++N5h/KlU4houprXksp4BBpzrJSlLF50NgWehtdwLkUE9/R0KWOQ3SFqpNhXG2gzamaMkRhQzQQ8zT0GPuHYDTgn8ArWByfmEU0wvbCPQXNkM7gLjhFAoGBALPLlcsyNG9B1m+JAYgUYnC2TKXgS2mMSHXEoA+WZrVbwY53C/1Fss/GB7RuE3xqpPEDZco0UEp+GaK0g9g+GqvGk2QpyWdpGxagiAV5m880RhzVzct8NKQwkNB73Tsf4lPPH54wLKXHj4AUUPO9fUSPaf6CMO3anJAwnFVKrYWBAoGANs4LorI60aSFK3+/1fPvRe+ELX94845ypteGeFqZ8/5FBu257Doyd1NwDNTVMSoWunVkJOryRQpDGE7j4jg4+E1eMjuIg26xpdLp9Eyg41N7138WTq2bWfP6BBTB5pbQ/otnINyzFgCpQzxvKSsRlQe+hMbUB4gjKyZBRuw2nG0="

	// Invalid private key for which public key does not exist in jwtTokenStrategy.config file.
	private static final String INVALID_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCSGeKU62wRhqdS6FVjdF6P8KlitatY7VzoBvI9n/xctUC7oRQw9N6f3g+tPunAgCT/HUW/9gk1hfjCN+MJ3c4iZJZkg/S8XLLfjrFDDUMILlSZizpV0vTi/OGcHC68C3eiqIF1q/zlho4LkowK3/dIHRcUIFr/YUj0RGsxmSYmV3fNVtN/lyE5uTnFrgyt3iZ4CDCihlTh+mjNJgdVAbK6wOFCDOYpUDhkQ9jbm7VXraiPvH3u0CyOKunz5vf55IJXi6xfzGGal56VACKSJTaNS61NPpbCo56jVR/s20HLCyMlqXibfaErkvcytZ4EDawqtoMmiiWtiFpq+9bZljQPAgMBAAECggEASljNysZ6efix0SjVuwSO0mS/bbHQ/jHb418VinKNc4fw5wozQi5505SnGZw4S3NqYEA/LE5BpXEFg5/48x5iNXJGbeQQeIIbrFPqtJpdEB6zgmctSltNruzMyio1Rg/301g4eO8KhKqSQiVTCn2qUpIrGlzUsbyJ6XOfsN+kYEHBqo+iFYd97lGGhboDK8lhAgKIHJVX6LVi+lLDovGtdQJEF1fESmahH6yfQ+4p4Fqi1dSKTMiE+LJuybgouKJh9TCvTffMC0IBWEDlcnqtLQErhM/Wc90qDDPrCq+1P6coxi8+MgHfj4JucBAIq/Ehdu7TrOfE9cxku9b4DyIloQKBgQDBvAnXzykUn4LqntQYUe7FJrx+YN0TgYLW32uLHvGmQT2ndlO7SaHZTP+F/b5bCyVU7qSsUM1NMTDW+g9WyjbOwtRV6vxieSQ2paByCQKPe/s4bEId7Kv6GCA5SnEmuwYOdABxP/+GfWLH+lPp1LxEUUgWhyI5bpaKh9VZnlpXNwKBgQDBDrI6DocMBTzEHIu+LqwVtklsWUrlbm2AVkuMs6pSNwoGrPFn5eBcvVmr69bzyZqeFZ85rjOeNsT9X8JVI902XuLPy8pQ72LxlP4omHyawey9PAfK1bmDN/9Pzv3rG1jDWNjRTr/oWErCfcVCDkvx3cIqo/RaAMHc18bMhn1F6QKBgQCAil3RhY7RMyCuwOEincIZpyDrjSNB3O1N2gFF1ispTyI0KStXGXy8a/0iDwCs8ZE8b/ZsqlP9QoUQLevdft+sgdJWi0oXoB7p7yysXNQJFY7SmemoZy6YCkffG0hGFVLoZgkhGwBUYH8ZBjuE6vRbM77ry49mobxaf9OL2ahBMwKBgB9cWctgjMOBHkvlLzYnssCcKfU11BrNA3Czk/Y5QVO1qSVIdraf+wBVqflpiDN17m37qS/fgncTApD+Oz1FZCvu4f1LZ4QaPwJrZ5YEn0ksc16SH9ntOgN81zaJs2m7uYGSLzZhCn+dyBcsAx0l4WRa163BkHIGaXeMfbi/qB3xAoGAD0KUxQZ3ZVVT0ZHLOp17wj48z3YEJz6LDxXmF4OT78Rpob11fLBUsQr2wdFe/ahziEaK70h3LT7V+KxYgSnwxyTpNLMyMJBjIyBIbn/Nodd3eTz2teFFT9e1KflTc4rPL9RBZAtTRE/VagByOB7T/3ppZfHVFFhJjmGW88uK0oA="

	private static final Map<String, String> privateKeyMap =
			["am"                : PRIMARY_JWT_PRIVATE_KEY,
			 "punchout_attrval"  : SECOND_JWT_PRIVATE_KEY,
			 "punchout_shared_id": SECOND_JWT_PRIVATE_KEY,
			 "invalid"           : INVALID_PRIVATE_KEY]

	@Given('^I login using jwt authorization with the following details$')
	static void setHeaderValue(final DataTable tokenDetails) {
		Map<String, String> jwtTokenMap = tokenDetails.asMap(String.class, String.class)
		String accountSharedId = jwtTokenMap.get("account_shared_id")
		String scope = jwtTokenMap.getOrDefault("scope", DEFAULT_SCOPE)
		String userId = replaceEmptyWithNull(jwtTokenMap.getOrDefault("user_id", null))
		String issuer = jwtTokenMap.getOrDefault("issuer", DEFAULT_ISSUER)
		String subject = replaceEmptyWithNull(jwtTokenMap.getOrDefault("subject", null))
		subject = (subject == null && userId == null) ? DEFAULT_USER_ID : subject

		int expirationInSeconds = jwtTokenMap.get("expiration_in_seconds") == null ? 300 : Integer.parseInt(jwtTokenMap.get("expiration_in_seconds"))

		String token = generateTokenWithMetadata(userId, subject, scope, accountSharedId, expirationInSeconds, issuer)

		Map<String, String> headers = new HashMap<String, String>()
		headers.put("Authorization", "Bearer " + token)
		client.setHeaders(headers)
	}

	@Given('^I login using jwt authorization with metadata and the following details inside$')
	static void setHeaderForTokenWithMetadata(final DataTable tokenDetails) {
		Map<String, String> jwtTokenMap = tokenDetails.asMap(String.class, String.class)
		String accountSharedId = jwtTokenMap.get("account_shared_id")
		String scope = jwtTokenMap.getOrDefault("scope", DEFAULT_SCOPE)
		String userId = jwtTokenMap.get("user_id")
		String issuer = jwtTokenMap.getOrDefault("issuer", DEFAULT_ISSUER)
		int expirationInSeconds = jwtTokenMap.get("expiration_in_seconds") == null ? 300 : Integer.parseInt(jwtTokenMap.get("expiration_in_seconds"))

		String metadataUserId = jwtTokenMap.getOrDefault("metadata_user_id", StringUtils.EMPTY)
		String firstName = jwtTokenMap.getOrDefault("metadata_first_name", StringUtils.EMPTY)
		String lastName = jwtTokenMap.getOrDefault("metadata_last_name", StringUtils.EMPTY)
		String username = jwtTokenMap.getOrDefault("metadata_username", StringUtils.EMPTY)
		String userEmail = jwtTokenMap.getOrDefault("metadata_user_email", StringUtils.EMPTY)
		String userCompany = jwtTokenMap.getOrDefault("metadata_user_company", StringUtils.EMPTY)

		String token = generateTokenWithMetadata(metadataUserId, firstName, lastName, username, userEmail, userCompany, userId,
				scope, accountSharedId, expirationInSeconds, issuer)

		Map<String, String> headers = new HashMap<String, String>()
		headers.put("Authorization", "Bearer " + token)
		client.setHeaders(headers)
	}

	private static String replaceEmptyWithNull(final String value) {
		return StringUtils.isEmpty(value) ? null : value
	}

	private static String generateTokenWithMetadata(final String metadataUserId, final String userId, final String scope,
													final String accountSharedId, final int expirationInSeconds, final String issuer) {
		String metadata = getMetadata(metadataUserId)

		return createToken(userId, scope, accountSharedId, metadata, expirationInSeconds, issuer)
	}

	private static String generateTokenWithMetadata(final String metadataUserId, final String firstName, final String lastName,
													final String username, final String userEmail, final String userCompany,
													final String userId, final String scope,
													final String accountSharedId, final int expirationInSeconds, final String issuer) {
		String metadata = getMetadata(metadataUserId, firstName, lastName, username, userEmail, userCompany)

		return createToken(userId, scope, accountSharedId, metadata, expirationInSeconds, issuer)
	}

	//inspiration from https://github.elasticpath.net/commerce/enterprise-account-management/blob/master/app-auth/src/main/java/com/elasticpath/eam/appauth/service/impl/AppTokenServiceImpl.java
	static PrivateKey getPrivateKey(final String issuer) throws Exception {
		String privateKey = PRIMARY_JWT_PRIVATE_KEY
		if (issuer != "am") {
			privateKey = privateKeyMap.get(issuer)
		}

		KeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey))
		KeyFactory keyFactory = KeyFactory.getInstance(SignatureAlgorithm.RS256.getFamilyName())
		return keyFactory.generatePrivate(privateKeySpec)
	}


	static String createToken(final String userId, final String scope, final String accountGuid,
							  final String metadata, final int expirationInSeconds, final String issuer) {
		Instant now = Instant.now()
		Date expiration = Date.from(now.plus(expirationInSeconds, ChronoUnit.SECONDS))
		Date issuedAt = Date.from(now)

		return Jwts.builder()
				.setIssuer(issuer)
				.setSubject(userId)
				.setAudience(scope)
				.setExpiration(expiration)
				.setIssuedAt(issuedAt)
				.claim(CLAIM_SCOPE, scope) // Multiple scopes are encoded into a single space delimited string
				.claim(CLAIM_PROFILE, accountGuid)
				.claim(CLAIM_METADATA, metadata)
				.signWith(getPrivateKey(issuer))
				.compact()
	}

	static String getMetadata(final String userId) {
		if (StringUtils.isEmpty(userId)) {
			return null
		}
		String json = Json.createObjectBuilder()
				.add("user-id", userId)
				.build()
				.toString()

		return Base64.getEncoder().encodeToString(json.getBytes())
	}

	static String getMetadata(final String userId, final String firstName, final String lastName, final String username,
							  final String userEmail, final String userCompany) {

		String json = Json.createObjectBuilder()
				.add("user-id", userId)
				.add("first-name", firstName)
				.add("last-name", lastName)
				.add("user-name", username)
				.add("user-email", userEmail)
				.add("user-company", userCompany)
				.build()
				.toString()

		return Base64.getEncoder().encodeToString(json.getBytes())
	}
}
