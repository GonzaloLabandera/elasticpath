/*
 * Copyright (c) Elastic Path Software Inc., 2005
 */

package com.elasticpath.domain.misc.impl;

/*
 * RandomGUID from http://www.javaexchange.com/aboutRandomGUID.html
 *
 * @version 1.2.1 11/05/02 @author Marc A. Mnich From www.JavaExchange.com, Open Software licensing 11/05/02 -- Performance enhancement from Mike
 *          Dubman. Moved InetAddr.getLocal to static block. Mike has measured a 10 fold improvement in run time. 01/29/02 -- Bug fix: Improper
 *          seeding of nonsecure Random object caused duplicate GUIDs to be produced. Random object is now only created once per JVM. 01/19/02 --
 *          Modified random seeding and added new constructor to allow secure random feature. 01/14/02 -- Added random function seeding with JVM run
 *          time
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

import org.apache.log4j.Logger;

import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.misc.RandomGuid;

/**
 * Globally unique identifier generator.
 * <p>
 * In the multitude of java GUID generators, I found none that guaranteed randomness. GUIDs are guaranteed to be globally unique by using ethernet
 * MACs, IP addresses, time elements, and sequential numbers. GUIDs are not expected to be random and most often are easy/possible to guess given a
 * sample from a given generator. SQL Server, for example generates GUID that are unique but sequencial within a given instance.
 * <p>
 * GUIDs can be used as security devices to hide things such as files within a filesystem where listings are unavailable (e.g. files that are served
 * up from a Web server with indexing turned off). This may be desireable in cases where standard authentication is not appropriate. In this
 * scenario, the RandomGuids are used as directories. Another example is the use of GUIDs for primary keys in a database where you want to ensure
 * that the keys are secret. Random GUIDs can then be used in a URL to prevent hackers (or users) from accessing records by guessing or simply by
 * incrementing sequential numbers.
 * <p>
 * There are many other possiblities of using GUIDs in the realm of security and encryption where the element of randomness is important. This class
 * was written for these purposes but can also be used as a general purpose GUID generator as well.
 * <p>
 * RandomGuid generates truly random GUIDs by using the system's IP address (name/IP), system time in milliseconds (as an integer), and a very large
 * random number joined together in a single String that is passed through an MD5 hash. The IP address and system time make the MD5 seed globally
 * unique and the random number guarantees that the generated GUIDs will have no discernable pattern and cannot be guessed given any number of
 * previously generated GUIDs. It is generally not possible to access the seed information (IP, time, random number) from the resulting GUIDs as the
 * MD5 hash algorithm provides one way encryption.
 * <p>
 * <b>Security of RandomGuid</b>: RandomGuid can be called one of two ways -- with the basic java Random number generator or a cryptographically
 * strong random generator (SecureRandom). The choice is offered because the secure random generator takes about 3.5 times longer to generate its
 * random numbers and this performance hit may not be worth the added security especially considering the basic generator is seeded with a
 * cryptographically strong random seed.
 * <p>
 * Seeding the basic generator in this way effectively decouples the random numbers from the time component making it virtually impossible to predict
 * the random number component even if one had absolute knowledge of the System time. Thanks to Ashutosh Narhari for the suggestion of using the
 * static method to prime the basic random generator.
 * <p>
 * Using the secure random option, this class complies with the statistical random number generator tests specified in FIPS 140-2, Security
 * Requirements for Cryptographic Modules, secition 4.9.1.
 * <p>
 * I converted all the pieces of the seed to a String before handing it over to the MD5 hash so that you could print it out to make sure it contains
 * the data you expect to see and to give a nice warm fuzzy. If you need better performance, you may want to stick to byte[] arrays.
 * <p>
 * I believe that it is important that the algorithm for generating random GUIDs be open for inspection and modification. This class is free for all
 * uses. NOTE: This class has been renamed and modified to make it comply with EP architecture and checkstyle rules
 *
 * @version 1.2.1 11/05/02
 * @author Marc A. Mnich
 */
public class RandomGuidImpl implements RandomGuid {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private static final Logger LOG = Logger.getLogger(RandomGuidImpl.class);

	private static final String VALUE_DASH = "-";

	private static final int VALUE_20 = 20;

	private static final int VALUE_16 = 16;

	private static final int VALUE_12 = 12;

	private static final int VALUE_8 = 8;

	private static final String COLON = ":";

	private static final int VALUE_0X10 = 0x10;

	private static final int VALUE_0X_FF = 0xFF;

	private static Random random;

	private static SecureRandom secureRandom;

	private static String ident;

	private String valueBeforeMD5 = "";

	private String valueAfterMD5 = "";

	/*
	 * Static block to take care of one time secureRandom seed. It takes a few seconds to initialize SecureRandom. You might want to consider
	 * removing this static block or replacing it with a "time since first loaded" seed to reduce this time. This block will run only once per JVM
	 * instance.
	 */
	static {
		secureRandom = new SecureRandom();
		long secureInitializer = secureRandom.nextLong();
		random = new Random(secureInitializer);
		try {
			ident = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage());
			if (LOG.isDebugEnabled()) {
				LOG.debug("Host not found", e);
			}
		}

	}

	/**
	 * Default constructor. With no specification of security option, this constructor defaults to lower security, high performance.
	 */
	public RandomGuidImpl() {
		getRandomGuid(false);
	}

	/**
	 * Constructor with security option. Setting secure true enables each random number generated to be cryptographically strong. Secure false
	 * defaults to the standard Random function seeded with a single cryptographically strong random number.
	 *
	 * @param secure set to true to create cryptographically strong random generator that takes longer to compute
	 */
	public RandomGuidImpl(final boolean secure) {
		getRandomGuid(secure);
	}

	/**
	 * Method to generate the random GUID.
	 */
	private void getRandomGuid(final boolean secure) {
		MessageDigest md5;

		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new EpDomainException("MD5 algorithm not supported.", e);
		}

		try {
			long time = System.currentTimeMillis();
			long rand = 0;

			if (secure) {
				rand = secureRandom.nextLong();
			} else {
				rand = random.nextLong();
			}

			// This StringBuffer can be a long as you need; the MD5
			// hash will always return 128 bits. You can change
			// the seed to include anything you want here.
			// You could even stream a file through the MD5 making
			// the odds of guessing it at least as great as that
			// of guessing the contents of the file!
			StringBuilder sbValueBeforeMD5 = new StringBuilder();
			sbValueBeforeMD5.append(ident);
			sbValueBeforeMD5.append(COLON);
			sbValueBeforeMD5.append(Long.toString(time));
			sbValueBeforeMD5.append(COLON);
			sbValueBeforeMD5.append(Long.toString(rand));

			valueBeforeMD5 = sbValueBeforeMD5.toString();
			md5.update(valueBeforeMD5.getBytes(StandardCharsets.UTF_8));

			byte[] array = md5.digest();
			StringBuilder strbuf = new StringBuilder();
			for (byte anArray : array) {
				int currByte = anArray & VALUE_0X_FF;
				if (currByte < VALUE_0X10) {
					strbuf.append('0');
				}

				strbuf.append(Integer.toHexString(currByte));
			}
			valueAfterMD5 = strbuf.toString();
		} catch (Exception e) {
			LOG.error("Error generating GUID", e);
		}
	}

	/**
	 * Produces a string representation of the RandomGUID.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		String raw = valueAfterMD5.toUpperCase(Locale.ENGLISH);
		StringBuilder strbuf = new StringBuilder();
		strbuf.append(raw.substring(0, VALUE_8));
		strbuf.append(VALUE_DASH);
		strbuf.append(raw.substring(VALUE_8, VALUE_12));
		strbuf.append(VALUE_DASH);
		strbuf.append(raw.substring(VALUE_12, VALUE_16));
		strbuf.append(VALUE_DASH);
		strbuf.append(raw.substring(VALUE_16, VALUE_20));
		strbuf.append(VALUE_DASH);
		strbuf.append(raw.substring(VALUE_20));
		return strbuf.toString();
	}
}